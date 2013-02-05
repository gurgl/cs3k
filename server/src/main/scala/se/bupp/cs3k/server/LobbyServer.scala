package se.bupp.cs3k.server

import com.esotericsoftware.kryonet.{KryoSerialization, Listener, Connection, Server}
import model._
import model.AnonUser
import model.AnonUser
import model.NonPersisentGameOccassion
import model.NonPersisentGameOccassion
import model.RegedUser
import model.RegedUser
import model.RunningGame
import model.RunningGame
import se.bupp.cs3k._
import api.user.{RegisteredPlayerIdentifierWithInfo, RegisteredPlayerIdentifier, AnonymousPlayerIdentifier, AbstractPlayerIdentifier}

import collection.mutable
import java.util.{Timer, HashMap}
import org.apache.commons.exec.{DefaultExecutor, ExecuteWatchdog, DefaultExecuteResultHandler, CommandLine}
import java.util.concurrent.{TimeUnit, Executors}
import java.net.URL
import io.Source
import server.ServerAllocator._
import server.ServerAllocator.Allocate
import server.ServerAllocator.AllocateAccept
import server.ServerAllocator.DropInterrest
import server.ServerAllocator.Free
import service.GameReservationService

import org.apache.log4j.Logger
import com.esotericsoftware.minlog.Log
import com.esotericsoftware.kryo.serializers.{BeanSerializer, TaggedFieldSerializer, JavaSerializer}
import com.esotericsoftware.kryo.Kryo
import service.gameserver.{GameProcessTemplate, GameServerPool, GameServerRepository}
import se.bupp.cs3k.server.model.Model._
import scala.Some
import concurrent.{Promise, Future, future, promise}
import collection.immutable.Queue


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-13
 * Time: 22:44
 * To change this template use File | Settings | File Templates.
 */

object LobbyServer {



  var seqId = Cs3kConfig.LOBBY_SERVER_PORT_RANGE.head

  def createContinousForNonPersistedGameOcassionsInstance(numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) = {
    if (seqId + 1 >= Cs3kConfig.LOBBY_SERVER_PORT_RANGE.last) throw new RuntimeException("Range ended")
    val lobbyServer = new LobbyServer(seqId, new NonTeamLobbyHandler(numOfPlayers, gameAndRulesId))
    seqId = seqId + 1
    lobbyServer
  }

}


object AbstractLobbyHandler {
  val log = Logger.getLogger(this.getClass)
  var gameReservationService:GameReservationService = _


  def sendStartGameInstructionsToParty(party:List[(Connection,AbstractUser)], gameSessionId : GameSessionId, runningGame: RunningGame) {
    val scheduler = Executors.newScheduledThreadPool(1);
    log.info("creating delayed game launch announcer task " + party.size)
    val beeper = new Runnable() {
      def  run() {
        log.info("In delayed game launch announcer " + party.size)
        party.foreach { case (c,pi) =>

          try {
            val reservationId = gameReservationService.reserveSeat(gameSessionId, pi, None)

            log.info("Reserving seat and sending sending start game instructions")
            // TODO Associate reservation with either ID or name (depending on pi subclass)
            var jnlpUrl: URL = runningGame.processSettings.jnlpUrl(reservationId, None)
            c.sendTCP(new StartGame(jnlpUrl.toExternalForm))
          } catch {
            case e:Exception => e.printStackTrace()
          }
          c.close()
        }
      }
    }
    val beeperHandle = scheduler.schedule(beeper, Cs3kConfig.LOBBY_GAME_LAUNCH_ANNOUNCEMENT_DELAY,  TimeUnit.SECONDS);
  }
}

object ServerAllocator {
  case class Allocate()
  case class Free()
  case class Init(val numOf:Int)


  case class AllocateDenyQueued()
  case class DropInterrest()
  case class AllocateAccept()

  var serverAllocator = new ServerAllocator(Cs3kConfig.MAX_NUM_OF_GAME_SERVER_PROCESSES)

}


class ServerAllocator(val numOfTotalSlots:Int) {
  var queue = Queue.empty[Promise[Int]]

  import scala.concurrent.ExecutionContext.Implicits.global

  var numOfSlotsAllocated = 0

  private def allocation() = {
    val r = numOfSlotsAllocated
    numOfSlotsAllocated = numOfSlotsAllocated + 1
    r
  }
  def allocate() : Future[Int] = {
    if (numOfSlotsAllocated < numOfTotalSlots) {
      val f = allocation()
      future {
        f
      }
    } else {
      val p = promise[Int]
      queue = queue.enqueue(p)
      p.future
    }
  }
  def deallocate() {
    if (queue.size > 0) {
      queue = queue.dequeue match {
        case (p, queueNew) =>
          p success allocation()

          queueNew
      }
    } else {
      numOfSlotsAllocated = numOfSlotsAllocated - 1
    }
  }
}

abstract class AbstractLobbyHandler[T] {
  import AbstractLobbyHandler._


  var queue = mutable.Queue.empty[(Connection,T)]


  def allocate() = {
    ServerAllocator.serverAllocator.allocate()
  }

  def evaluateQueue() : Unit

  def customize(u:AbstractUser) : T

  def playerJoined(request: LobbyJoinRequest, connection: Connection) {
    val api = request.userIdOpt.map(new RegedUser(_)).getOrElse {
      if (request.name == "") throw new RuntimeException("YO")
      new AnonUser(request.name)
    }
    queue += Pair(connection, customize(api))

    evaluateQueue()
  }

  def launchServerInstance(settings:GameProcessTemplate, party: List[(Connection, AbstractUser)]) {


    log.info("New queue size " + queue.size)

    val gameSessionId = gameReservationService.allocateGameSession()
    var runningGame: RunningGame = GameServerPool.pool.spawnServer(settings, new NonPersisentGameOccassion(gameSessionId))

    sendStartGameInstructionsToParty(party, gameSessionId, runningGame)
  }

  def removeConnection(p1: Connection): Boolean = {
    queue.dequeueFirst(_._1.getID == p1.getID) match {
      case None => log.info("Removing UNKNOWN disconnect ")
      false
      case Some(c) => log.info("Removing disconnect")
      true
      //queue = mutable.Queue.empty[Connection].dequeue++ queue.drop(i)
    }
  }
}

class TeamLobbyHandler(val numOfTeams:Int, val numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) extends AbstractLobbyHandler[AbstractUser] {

  import AbstractLobbyHandler._

  def customize(u: AbstractUser) = null

  def evaluateQueue() {}

}


class NonTeamLobbyHandler(val numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) extends AbstractLobbyHandler[AbstractUser] {
  import AbstractLobbyHandler._

  var launchQueue = mutable.Queue.empty[List[Connection]]


  def customize(u: AbstractUser) = null

  def createParty = {
    var party = List[(Connection,AbstractUser)]()

    log.info("Removing " + numOfPlayers + " players from a queue of " + queue.size)

    (0 until numOfPlayers).foreach { s =>
      val c = queue.dequeue()
      party = c :: party
    }
    party
  }

  def evaluateQueue() {
    if (queue.size >= numOfPlayers) {

      GameServerRepository.findBy(gameAndRulesId) match {
        case Some(gameServerSettings) =>
          allocate()
          val party = createParty
          launchServerInstance(gameServerSettings,party)


        case None => log.error("Unknown game server setting : " + gameAndRulesId)
      }
      ()
    }
  }
}

class LobbyServer(val portId:Int, var lobbyHandler:NonTeamLobbyHandler) {



  val log = Logger.getLogger(classOf[LobbyServer])



  val kryo: Kryo = new Kryo
  kryo.setDefaultSerializer(classOf[BeanSerializer[_]])
  import scala.collection.JavaConversions.asScalaBuffer
  LobbyProtocol.getTypes.toList.foreach( c => kryo.register(c) )


  val kryoSerialization: KryoSerialization = new KryoSerialization(kryo)

  val server = new Server(16384, 2048, kryoSerialization) 

  def stop() {
    server.stop();
    server.close();

    GameServerPool.pool.servers.foreach( s => s._2.getWatchdog.destroyProcess())
  }

  def start = {

    server.start()

    server.bind(portId);

    server.addListener(new Listener() {

      override def disconnected(p1: Connection) {
        super.disconnected(p1)
         if (lobbyHandler.removeConnection(p1)) {
          server.sendToAllTCP(new ProgressUpdated(lobbyHandler.queue.size))
        }
      }

      override def received (connection:Connection , ob:Object) {
        ob match {
          case request:LobbyJoinRequest =>
            log.info("LobbyJoinRequest received : " + request)
            connection.sendTCP(new LobbyJoinResponse(lobbyHandler.numOfPlayers))

            lobbyHandler.playerJoined(request, connection)

            server.sendToAllTCP(new ProgressUpdated(lobbyHandler.queue.size))

          case e => log.info("uknown rec" + e)
        }
      }
    })
  }
}
