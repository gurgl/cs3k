package se.bupp.cs3k.server

import com.esotericsoftware.kryonet.{Listener, Connection, Server}
import model.NonPersisentGameOccassion
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


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-13
 * Time: 22:44
 * To change this template use File | Settings | File Templates.
 */

object ServerLobby {
  def main(args:Array[String]) {
    new ServerLobby(0,2).start
  }
  lazy val remoteIp = {
    val stackOverflowURL = "http://automation.whatismyip.com/n09230945.asp"
    val requestProperties = Map(
      "User-Agent" -> "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:12.0) Gecko/20100101 Firefox/12.0"
    )
    val connection = new URL(stackOverflowURL).openConnection
    requestProperties.foreach({
      case (name, value) => connection.setRequestProperty(name, value)
    })

    var response = Source.fromInputStream(connection.getInputStream).getLines.mkString("\n")
    response
  }
}

class ServerLobby(val seqId:Int, val numOfPlayers:Int) {

  val log = Logger.getLogger(classOf[ServerLobby])

  var gameReservationService:GameReservationService = _

  var queue = mutable.Queue.empty[(Connection,AbstractPlayerIdentifier)]
  var queueItemInfo = mutable.Map[Long,AnyRef]()

  var launchQueue = mutable.Queue.empty[List[Connection]]

  val server = new Server();


  def stop() {
    server.stop();
    server.close();

    GameServerPool.pool.servers.foreach( s => s._2.getWatchdog.destroyProcess())
  }

  def start = {

    server.start()
    server.bind(12345 + seqId);

    val kryo = server.getKryo()



    import scala.collection.JavaConversions.asScalaBuffer
    LobbyProtocol.getTypes.toList.foreach(kryo.register(_))

    server.addListener(new Listener() {

      override def disconnected(p1: Connection) {
        super.disconnected(p1)
        queue.dequeueFirst(_._1.getID == p1.getID) match {
          case None => println("Removing UNKNOWN disconnect ")
          case Some(c) =>  println("Removing disconnect")
            server.sendToAllTCP(new ProgressUpdated(queue.size))
            //queue = mutable.Queue.empty[Connection].dequeue++ queue.drop(i)
        }
      }

      override def received (connection:Connection , ob:Object) {
        ob match {
          case request:LobbyJoinRequest =>

              connection.sendTCP(new LobbyJoinResponse(numOfPlayers))

              val api = request.userIdOpt.map(new RegisteredPlayerIdentifier(_)).getOrElse {
                if (request.name == "") throw new RuntimeException("YO") ; new AnonymousPlayerIdentifier(request.name)
              }
              queue += Pair(connection, api)
              server.sendToAllTCP(new ProgressUpdated(queue.size))
              if (queue.size >= numOfPlayers) {
                launchServerInstance()
                ()
            }
          case _ =>
        }
      }
    })
  }

  def launchServerInstance() {

    var party = List[(Connection,AbstractPlayerIdentifier)]()



    println("quueu size " + queue.size)

    (0 until numOfPlayers).foreach { s =>
      val c = queue.dequeue()
      party = c :: party
    }


    val occassionId = gameReservationService.allocateOccassion()

    GameServerPool.pool.spawnServer(GameServerPool.tankGameSettings2, new NonPersisentGameOccassion(occassionId))

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
              case i:AnonymousPlayerIdentifier => GameServerPool.tankGameSettings2.jnlpUrl(reservationId, i.getName)
              case i:RegisteredPlayerIdentifier  => GameServerPool.tankGameSettings2.jnlpUrl(reservationId, i.getUserId)
            }
            c.sendTCP(new StartGame(ServerLobby.remoteIp, 54555 + seqId, 54777 + seqId,jnlpUrl.toExternalForm))
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
