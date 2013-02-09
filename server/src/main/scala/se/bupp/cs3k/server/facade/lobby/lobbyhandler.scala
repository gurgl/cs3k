package se.bupp.cs3k.server.facade.lobby

import org.slf4j.{LoggerFactory, Logger}
import se.bupp.cs3k.server.service.GameReservationService
import com.esotericsoftware.kryonet.{Listener, Server, KryoSerialization, Connection}
import se.bupp.cs3k.server.model._
import java.util.concurrent.{TimeUnit, Executors}
import java.net.URL
import se.bupp.cs3k._
import se.bupp.cs3k.server.{Cs3kConfig}
import se.bupp.cs3k.server.model.Model._
import collection.parallel.mutable
import se.bupp.cs3k.server.service.gameserver.{GameServerRepository, GameServerPool, GameProcessTemplate}
import scala.Some
import se.bupp.cs3k.server.model.RegedUser
import se.bupp.cs3k.server.model.RunningGame
import se.bupp.cs3k.server.model.AnonUser
import se.bupp.cs3k.server.service.resourceallocation.ServerAllocator


import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.BeanSerializer
import server.model.AnonUser
import server.model.NonPersisentGameOccassion
import scala.Some
import server.model.RegedUser
import server.model.RunningGame
import util.{Failure, Success}
import collection.immutable.Queue

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-06
 * Time: 03:29
 * To change this template use File | Settings | File Templates.
 */
object AbstractLobbyQueueHandler {
  val log = LoggerFactory.getLogger(this.getClass)
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


abstract class AbstractLobbyQueueHandler[T](gameAndRulesId: GameServerRepository.GameAndRulesId) {



  val gameServerSettings =  GameServerRepository.findBy(gameAndRulesId).getOrElse(throw new RuntimeException("Not found " + gameAndRulesId))

  type Info = T

  import AbstractLobbyQueueHandler._
  var queue = scala.collection.mutable.Queue.empty[(Connection,T)]
  var queueMembersWithLobbyAssignments = List.empty[(AbstractUser,Int)]


  //def removePartyFromQueue(party:List[(Connection,AbstractUser)]) : List[(Connection,AbstractUser)]

  def allocate() = {
    //ServerAllocator.serverAllocator.allocate()
  }

  def evaluateQueue() : Unit



  def customize(u:AbstractUser) : T

  def playerJoined(request: LobbyJoinRequest, connection: Connection) {
    addPlayer(request, connection)

    evaluateQueue()
  }


  def addPlayer(request: LobbyJoinRequest, connection: Connection) {
    val api = request.userIdOpt.map(new RegedUser(_)).getOrElse {
      if (request.name == "") throw new RuntimeException("YO")
      new AnonUser(request.name)
    }
    queue += Pair(connection, customize(api))
  }

  def launchServerInstance(settings:GameProcessTemplate, party: List[(Connection, AbstractUser)], processToken:ProcessToken) = {


    log.info("New queue size " + queue.size)

    val gameSessionId = gameReservationService.allocateGameSession()
    var runningGame: RunningGame = GameServerPool.pool.spawnServer(settings, new NonPersisentGameOccassion(gameSessionId),processToken)

    sendStartGameInstructionsToParty(party, gameSessionId, runningGame)
    runningGame
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



class TeamLobbyQueueHandler(val numOfTeams:Int, val numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) extends AbstractLobbyQueueHandler[AbstractUser](gameAndRulesId) {

  import AbstractLobbyQueueHandler._

  def customize(u: AbstractUser) = u

  def evaluateQueue() {

  }

}






class NonTeamLobbyQueueHandler(val numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) extends AbstractLobbyQueueHandler[AbstractUser](gameAndRulesId) {
  import AbstractLobbyQueueHandler._


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

  def removePartyFromQueue(party:List[(Connection,AbstractUser)])  {
    queue =  queue.filter( p => party.exists(_ == p))
  }

  def evaluateQueue() {
    //if (queue.size >= numOfPlayers) {

      /*GameServerRepository.findBy(gameAndRulesId) match {
        case Some(gameServerSettings) =>
      */
    val party = createParty
    import scala.concurrent.ExecutionContext.Implicits.global

    val launcher = (pt:ProcessToken) => {
      removePartyFromQueue(party)
      val runningGame = launchServerInstance(gameServerSettings, party, pt)
      runningGame.done
    }
    val allocation = ServerAllocator.serverAllocator.allocate(launcher)

    allocation onComplete {
      case Success(i) =>
        // TODO : Send lobby created

        //launchServerInstance(gameServerSettings,party)
      case Failure(t) =>
        // TODO : Send lobby destroyed
        log.error("Allocation went bad - reinserting party " + t)
    }
        //case None => log.error("Unknown game server setting : " + gameAndRulesId)
  }
}
