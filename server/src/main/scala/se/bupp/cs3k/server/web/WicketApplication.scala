package se.bupp.cs3k.server.web

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-12
 * Time: 17:46
 * To change this template use File | Settings | File Templates.
 */


import org.apache.wicket.protocol.http.WebApplication
import org.slf4j.LoggerFactory
import org.apache.wicket.{RuntimeConfigurationType, Application}

import org.apache.wicket.util.resource.{FileResourceStream, IResourceStream, AbstractResourceStreamWriter}
import java.io.{File, PrintWriter, IOException, OutputStream}
import org.apache.wicket.request.resource._
import java.util.Scanner
import page.ApplicationPage
import se.bupp.cs3k.{server, Greeting}
import org.apache.wicket.request.resource.IResource.Attributes
import org.apache.wicket.request.resource.AbstractResource.{WriteCallback, ResourceResponse}
import org.apache.wicket.util.time.Time
import org.apache.wicket.markup.html.link.ResourceLink
import org.apache.wicket.spring.injection.annot.{SpringBean, SpringComponentInjector}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}
import javax.persistence._
import org.springframework.transaction.annotation.{Propagation, Transactional}
import se.bupp.cs3k.server._
import facade.lobby.{LobbyServer}
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.Greeting
import org.apache.wicket.request.{Response, Request}
import se.bupp.cs3k.server.facade.{WebStartResourceFactory}
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.support.{FileSystemXmlApplicationContext, ClassPathXmlApplicationContext}
import service.{RankingService, GameReservationService}
import se.bupp.cs3k.server.service.GameReservationService._
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.context.WebApplicationContext
import se.bupp.cs3k.server.model.Model._
import java.net.URI


//import akka.actor.{Props, Actor, ActorSystem}
//import akka.event.Logging
//import akka.util.duration._
import se.bupp.cs3k.Greeting




object WicketApplication {

  def get = WebApplication.get().asInstanceOf[WicketApplication]
  val gameResourceKey = "JNLP_GENERATOR_game"
  val lobbyResourceKey = "JNLP_GENERATOR_lobby"

  class FolderContentResource(var rootFolder:File ) extends IResource {

    override def respond(attributes:Attributes ) {
      val parameters = attributes.getParameters();
      val fileName = parameters.get(0).toString();
      val file = new File(rootFolder, fileName);
      val fileResourceStream = new FileResourceStream(file);
      val resource = new ResourceStreamResource(fileResourceStream);
      resource.respond(attributes);
    }
  }
}


class WicketApplication extends WebApplication {

  import WicketApplication._
  //var eventSystem: EventSystem = _

  val logger = LoggerFactory.getLogger(classOf[WicketApplication])

  var isDevMode = {

    var testModeProp: String = "false"//System.getProperty("cs3k.prod-mode")
    logger.info("cs3k.prod-mode = " + String.valueOf(testModeProp))
    Option(testModeProp).map(t => !(t.toBoolean)).getOrElse(true)
  }


  override def getConfigurationType = RuntimeConfigurationType.DEPLOYMENT

  var webStartResourceFactory:WebStartResourceFactory = _



  override def newSession(request: Request, response: Response) = new WiaSession(request)

  def getHomePage() = classOf[ApplicationPage]

  //@transient var lobby : LobbyServer = _
  var gameResource:AbstractResource = _
  var lobbyResource: AbstractResource = _

  override def init() {
    super.init()

    new Init

    val authStrat = new WiaAuthorizationStrategy();
    val securitySettings = getSecuritySettings();
    securitySettings.setAuthorizationStrategy(authStrat);
    securitySettings.setUnauthorizedComponentInstantiationListener(authStrat);

    getComponentInstantiationListeners.add(new SpringComponentInjector(this));

    var requiredWebApplicationContext: WebApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext)
    val beanFactory = requiredWebApplicationContext.asInstanceOf[BeanFactory]

    Init.setupSpringDeps(beanFactory)

    val webStartResourceFactory = beanFactory.getBean(classOf[WebStartResourceFactory])


    lobbyResource= webStartResourceFactory.createLobbyJnlpHandler //new ByteArrayResource("application/x-java-jnlp-file", jnlpXML2.getBytes, "lobby2.jnlp")

    getSharedResources().add(lobbyResourceKey, lobbyResource)

    mountResource("/lobby2.jnlp", new SharedResourceReference(classOf[Application], lobbyResourceKey))

    var gameRoot = new URI("file:///c:/dev/workspace/opengl-tanks/client/target/scala-2.10/webstart/")
    gameResource = webStartResourceFactory.createGameJnlpHandler(gameRoot)

    getSharedResources().add(gameResourceKey, gameResource)
    mountResource("/deploy-launcher/tanks/start_game.jnlp", new SharedResourceReference(classOf[Application], gameResourceKey))


    var tankCodeBaseResourceKey = "tanks"


    getSharedResources().add(tankCodeBaseResourceKey, new FolderContentResource(new File(gameRoot)));
    mountResource("deploy/tanks", new SharedResourceReference(tankCodeBaseResourceKey));


  }

  override def onDestroy() {
    //eventSystem.shutdown()
    println("WicketApp onDestroy")
    Init.cleanup()
    super.onDestroy()
  }

  //def getEventSystem = eventSystem
}

/**
 * @author kjozsa
 */

