package se.bupp.cs3k

import com.sun.javaws.progress.Progress
import java.util
import java.net.URL

//import akka.actor.{ActorLogging, Actor}


class LobbyJoinResponse(var participantsRequired:Int) {
  def this() = this(0)
}

class LobbyJoinRequest() {
  //def this() = this("",0)
}


class ProgressUpdated(val progress:Int) {
  def this() = this(0)
}
class StartGame(val host:String, val tcpPort:Int, val udpPort:Int,val jnlpURL:String) {
  def this() = this("",0,0,"")
}


object LobbyProtocol {
  val getTypes = new util.ArrayList(
    util.Arrays.asList(
      classOf[LobbyJoinResponse],
      classOf[LobbyJoinRequest],
      classOf[ProgressUpdated],
      classOf[StartGame]
    )
  )
}

case class Greeting(who: String) //extends Serializable

/*class GreetingActor extends Actor with ActorLogging {
  def receive = {
    case Greeting(who) ⇒ log.info("Hello " + who)
  }
}*/
