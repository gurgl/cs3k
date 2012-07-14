/**
 *
 */
package se.bupp.cs3k.server

import org.apache.wicket.protocol.http.WebApplication
import org.slf4j.LoggerFactory
import org.apache.wicket.Application
import org.apache.wicket.protocol.ws.api.SimpleWebSocketConnectionRegistry
import akka.actor.{Props, Actor, ActorSystem}
import akka.event.Logging
import akka.util.duration._
import se.bupp.cs3k.Greeting



/**
 * @author kjozsa
 */

object WicketApplication {

  def get = WebApplication.get().asInstanceOf[WicketApplication]
}


class WicketApplication extends WebApplication {

  var eventSystem: EventSystem = _

  val logger = LoggerFactory.getLogger(classOf[WicketApplication])

  def getHomePage() = classOf[WebSocketDemo]

  //@transient var lobby : ServerLobby = _
  var lobby:ServerLobby = _
  override def init() {
    super.init()
    new Greeting("asdf")
    eventSystem = new EventSystem(this)
    lobby = new ServerLobby()
    lobby.start
  }

  override def onDestroy() {
    eventSystem.shutdown()
    super.onDestroy()
  }

  def getEventSystem = eventSystem
}


import akka.util.duration._
import akka.actor.{Props, Actor, ActorSystem}
import org.apache.wicket.Application
import org.apache.wicket.protocol.ws.api.SimpleWebSocketConnectionRegistry
import akka.event.Logging


