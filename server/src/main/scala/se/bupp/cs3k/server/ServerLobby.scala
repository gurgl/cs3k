package se.bupp.cs3k.server

import com.esotericsoftware.kryonet.{Listener, Connection, Server}
import se.bupp.cs3k.Tjena


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-13
 * Time: 22:44
 * To change this template use File | Settings | File Templates.
 */

object ServerLobby {
  def main(args:Array[String]) {
    new ServerLobby().start
  }
}

class ServerLobby() {


  val server = new Server();

  def start = {

    server.start()
    server.bind(12345);

    val kryo = server.getKryo()

    kryo.register(classOf[Tjena])

    server.addListener(new Listener() {
      override def received (connection:Connection , ob:Object) {
        ob match {
          case response:Tjena => System.out.println(response.a);
            connection.sendTCP(new Tjena("välkommen",connection.getID))
          case _ =>
        }
      }
    })



  }
}
