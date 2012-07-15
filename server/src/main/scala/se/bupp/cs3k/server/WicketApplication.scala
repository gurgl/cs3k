/**
 *
 */
package se.bupp.cs3k.server

import org.apache.wicket.protocol.http.WebApplication
import org.slf4j.LoggerFactory
import org.apache.wicket.Application
import org.apache.wicket.protocol.ws.api.SimpleWebSocketConnectionRegistry
//import akka.actor.{Props, Actor, ActorSystem}
//import akka.event.Logging
//import akka.util.duration._
import se.bupp.cs3k.Greeting



/**
 * @author kjozsa
 */

object WicketApplication {

  def get = WebApplication.get().asInstanceOf[WicketApplication]
}


class WicketApplication extends WebApplication {

  //var eventSystem: EventSystem = _

  val logger = LoggerFactory.getLogger(classOf[WicketApplication])

  def getHomePage() = classOf[TheHomePage]

  //@transient var lobby : ServerLobby = _
  var lobby2Player:ServerLobby = _
  var lobby4Player:ServerLobby = _
  override def init() {
    super.init()
    new Greeting("asdf")
    //eventSystem = new EventSystem(this)
    try {
    lobby2Player = new ServerLobby(0, 2)
    lobby2Player.start
    lobby4Player = new ServerLobby(1, 4)
    lobby4Player.start
    } catch {
      case e:Exception => e.printStackTrace()
    }
  }

  override def onDestroy() {
    //eventSystem.shutdown()
    lobby2Player.stop();
    lobby4Player.stop();
    super.onDestroy()
  }

  //def getEventSystem = eventSystem
}


