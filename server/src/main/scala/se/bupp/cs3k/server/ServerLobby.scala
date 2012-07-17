package se.bupp.cs3k.server

import com.esotericsoftware.kryonet.{Listener, Connection, Server}
import se.bupp.cs3k.{StartGame, LobbyProtocol, ProgressUpdated, Tjena}
import collection.mutable
import java.util.{Timer, HashMap}
import org.apache.commons.exec.{DefaultExecutor, ExecuteWatchdog, DefaultExecuteResultHandler, CommandLine}
import java.util.concurrent.{TimeUnit, Executors}


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
}

class ServerLobby(val seqId:Int, val numOfPlayers:Int) {




  var queue = mutable.Queue.empty[Connection]
  var launchQueue = mutable.Queue.empty[List[Connection]]

  val server = new Server();


  def stop() {
    server.stop();
    server.close();
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
        queue.dequeueFirst(_.getID == p1.getID) match {
          case None => println("Removing UNKNOWN disconnect ")
          case Some(c) =>  println("Removing disconnect")
            server.sendToAllTCP(new ProgressUpdated(queue.size))
            //queue = mutable.Queue.empty[Connection].dequeue++ queue.drop(i)
        }
      }

      override def received (connection:Connection , ob:Object) {
        ob match {
          case response:Tjena => System.out.println(response.gameJnlpUrl);
            connection.sendTCP(new Tjena(GameServerPool.tankGameSettings2.clientJNLPUrl, numOfPlayers))
            queue += connection
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

    var party = List[Connection]()

    println("quueu size " + queue.size)

    (0 until numOfPlayers).foreach { s =>
      val c = queue.dequeue()
      party = c :: party
    }

    GameServerPool.pool.spawnServer(GameServerPool.tankGameSettings2)
    val scheduler = Executors.newScheduledThreadPool(1);

    val beeper = new Runnable() {
      def  run() {

        party.foreach { c =>
          c.sendTCP(new StartGame("localhost", 54555 + seqId, 54777 + seqId))
          c.close()
        }
      }
    }
    val beeperHandle = scheduler.schedule(beeper, 2,  TimeUnit.SECONDS);

  }
}
