package se.bupp.cs3k.server

import com.esotericsoftware.kryonet.{Listener, Connection, Server}
import se.bupp.cs3k.{StartGame, LobbyProtocol, ProgressUpdated, Tjena}
import collection.mutable


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-13
 * Time: 22:44
 * To change this template use File | Settings | File Templates.
 */

object ServerLobby {
  def main(args:Array[String]) {
    new ServerLobby(2).start
  }
}

class ServerLobby(val numOfPlayers:Int) {



  var queue = mutable.Queue.empty[Connection]

  val server = new Server();

  def stop() {
    server.stop();
    server.close();
  }

  def start = {

    server.start()
    server.bind(12345);

    val kryo = server.getKryo()



    import scala.collection.JavaConversions.asScalaBuffer
    LobbyProtocol.getTypes.toList.foreach(kryo.register(_))

    server.addListener(new Listener() {
      override def received (connection:Connection , ob:Object) {
        ob match {
          case response:Tjena => System.out.println(response.a);
            connection.sendTCP(new Tjena("vilkommen", numOfPlayers))
            queue += connection
            server.sendToAllTCP(new ProgressUpdated(queue.size))
            if (queue.size >= numOfPlayers) {

              (0 until numOfPlayers).foreach { s =>
                val c = queue.dequeue()
                println("quueu size " + queue.size)
                c.sendTCP(new StartGame("localhost", 54555, 54777))
                c.close()

              }
              ()
            }
          case _ =>
        }
      }
    })



  }
}
