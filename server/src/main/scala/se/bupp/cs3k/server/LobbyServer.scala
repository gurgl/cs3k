package se.bupp.cs3k.server

import com.esotericsoftware.kryonet.{KryoSerialization, Listener, Connection, Server}
import model.{RunningGame, NonPersisentGameOccassion}
import se.bupp.cs3k._
import api.user.{PlayerIdentifierWithInfo, RegisteredPlayerIdentifier, AnonymousPlayerIdentifier, AbstractPlayerIdentifier}

import collection.mutable
import java.util.{Timer, HashMap}
import org.apache.commons.exec.{DefaultExecutor, ExecuteWatchdog, DefaultExecuteResultHandler, CommandLine}
import java.util.concurrent.{TimeUnit, Executors}
import java.net.URL
import io.Source
import service.GameReservationService
import scala.Some
import org.apache.log4j.Logger
import com.esotericsoftware.minlog.Log
import com.esotericsoftware.kryo.serializers.{BeanSerializer, TaggedFieldSerializer, JavaSerializer}
import com.esotericsoftware.kryo.Kryo
import service.gameserver.{GameProcessTemplate, GameServerPool, GameServerRepository}


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-13
 * Time: 22:44
 * To change this template use File | Settings | File Templates.
 */

object LobbyServer {


  def main(args:Array[String]) {
    new LobbyServer(0,2, null).start
  }


}

class LobbyServer(val seqId:Int, val numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) {

  val log = Logger.getLogger(classOf[LobbyServer])

  var gameReservationService:GameReservationService = _

  var queue = mutable.Queue.empty[(Connection,AbstractPlayerIdentifier)]
  var queueItemInfo = mutable.Map[Long,AnyRef]()

  var launchQueue = mutable.Queue.empty[List[Connection]]
  val kryo: Kryo = new Kryo
  kryo.setDefaultSerializer(classOf[BeanSerializer[_]])
  import scala.collection.JavaConversions.asScalaBuffer
  LobbyProtocol.getTypes.toList.foreach( c => kryo.register(c) )
  //Log.set(Log.LEVEL_TRACE)

  val kryoSerialization: KryoSerialization = new KryoSerialization(kryo)

  val server = new Server(16384, 2048, kryoSerialization) 

  def stop() {
    server.stop();
    server.close();

    GameServerPool.pool.servers.foreach( s => s._2.getWatchdog.destroyProcess())
  }

  def start = {

    server.start()

    server.bind(12345 + seqId);


    server.addListener(new Listener() {

      override def disconnected(p1: Connection) {
        super.disconnected(p1)
        queue.dequeueFirst(_._1.getID == p1.getID) match {
          case None => log.info("Removing UNKNOWN disconnect ")
          case Some(c) =>  log.info("Removing disconnect")
            server.sendToAllTCP(new ProgressUpdated(queue.size))
            //queue = mutable.Queue.empty[Connection].dequeue++ queue.drop(i)
        }
      }

      override def received (connection:Connection , ob:Object) {
        ob match {
          case request:LobbyJoinRequest =>
            log.info("LobbyJoinRequest received : " + request)
            connection.sendTCP(new LobbyJoinResponse(numOfPlayers))


            val api = request.userIdOpt.map(new RegisteredPlayerIdentifier(_)).getOrElse {
              if (request.name == "") throw new RuntimeException("YO")
              new AnonymousPlayerIdentifier(request.name)
            }
            queue += Pair(connection, api)
            server.sendToAllTCP(new ProgressUpdated(queue.size))
            if (queue.size >= numOfPlayers) {
              GameServerRepository.findBy(gameAndRulesId) match {
                case Some(gameServerSettings) =>  launchServerInstance(gameServerSettings)
                case None => log.error("Unknown game server setting : " + gameAndRulesId)
              }
              ()
            }
          case e => log.info("uknown rec" + e)
        }
      }
    })
  }

  def launchServerInstance(settings:GameProcessTemplate) {

    var party = List[(Connection,AbstractPlayerIdentifier)]()



    log.info("quueu size " + queue.size)

    (0 until numOfPlayers).foreach { s =>
      val c = queue.dequeue()
      party = c :: party
    }


    val occassionId = gameReservationService.allocateOccassion()


    var runningGame: RunningGame = GameServerPool.pool.spawnServer(settings, new NonPersisentGameOccassion(occassionId))

    val scheduler = Executors.newScheduledThreadPool(1);

    log.info("creating start task " + party.size)
    val beeper = new Runnable() {
      def  run() {
        log.info("in start task " + party.size)
        party.foreach { case (c,pi) =>

          try {
            val reservationId = gameReservationService.reserveSeat(occassionId, pi)

            log.info("Reserving seat and sending sending start game")
            var jnlpUrl: URL = pi match {
              case i:AnonymousPlayerIdentifier => runningGame.processSettings.jnlpUrl(reservationId, i.getName)
              case i:RegisteredPlayerIdentifier  => runningGame.processSettings.jnlpUrl(reservationId, i.getUserId)
            }
            c.sendTCP(new StartGame(Cs3kConfig.REMOTE_IP, 54555 + seqId, 54777 + seqId,jnlpUrl.toExternalForm))
          } catch {
            case e:Exception => e.printStackTrace()
          }
          c.close()
        }
      }
    }
    val beeperHandle = scheduler.schedule(beeper, 2,  TimeUnit.SECONDS);


  }
}
