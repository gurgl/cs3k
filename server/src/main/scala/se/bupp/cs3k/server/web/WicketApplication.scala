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
import org.apache.wicket.Application

import org.apache.wicket.util.resource.{IResourceStream, AbstractResourceStreamWriter}
import java.io.{PrintWriter, IOException, OutputStream}
import org.apache.wicket.request.resource._
import java.util.Scanner
import se.bupp.cs3k.Greeting
import org.apache.wicket.request.resource.IResource.Attributes
import org.apache.wicket.request.resource.AbstractResource.{WriteCallback, ResourceResponse}
import org.apache.wicket.util.time.Time
import org.apache.wicket.markup.html.link.ResourceLink
import org.apache.wicket.spring.injection.annot.{SpringBean, SpringComponentInjector}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}
import javax.persistence._
import org.springframework.transaction.annotation.{Propagation, Transactional}
import se.bupp.cs3k.server.{LobbyServer, web}
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.Greeting
import org.apache.wicket.request.{Response, Request}
import se.bupp.cs3k.server.facade.{WebStartResourceFactory}
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.support.{FileSystemXmlApplicationContext, ClassPathXmlApplicationContext}
import se.bupp.cs3k.server.service.GameReservationService
import se.bupp.cs3k.server.service.GameReservationService._
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.context.WebApplicationContext
import se.bupp.cs3k.server.model.{Ticket, GameOccassion}
import se.bupp.cs3k.server.model.Model._



//import akka.actor.{Props, Actor, ActorSystem}
//import akka.event.Logging
//import akka.util.duration._
import se.bupp.cs3k.Greeting




object WicketApplication {

  def get = WebApplication.get().asInstanceOf[WicketApplication]
  val resourceKey = "JNLP_GENERATOR"
  val resourceKey2 = "JNLP_GENERATOR_lobby"
}






@Component("mySBean")
@Transactional
class MyBean {

  @PersistenceContext(unitName="MyPersistenceUnit")
  var em:EntityManager = _

  //@Transactional(propagation = Propagation.REQUIRES_NEW)
  def insert(a:User) {
    em.persist(a)
  }





  import scala.collection.JavaConversions.asScalaBuffer

  //@Transactional()
  def read() {
    var q: Query = em.createQuery("from User")
    val res = q.getResultList.mkString(",")
    //println(res)
  }

  //@Transactional(propagation = Propagation.REQUIRES_NEW)
  def store() {
    //instance.useBeanFactory()

    em.persist(new User("Tja" + System.currentTimeMillis()))
  }
}

class WicketApplication extends WebApplication {

  import WicketApplication._
  //var eventSystem: EventSystem = _

  val logger = LoggerFactory.getLogger(classOf[WicketApplication])

  var isDevMode = {

    var testModeProp: String = System.getProperty("cs3k.prod-mode")
    logger.info("cs3k.prod-mode = " + String.valueOf(testModeProp))
    Option(testModeProp).map(t => !(t.toBoolean)).getOrElse(true)
  }

  var webStartResourceFactory:WebStartResourceFactory = _



  override def newSession(request: Request, response: Response) = new WiaSession(request)

  def getHomePage() = classOf[TheHomePage]

  //@transient var lobby : LobbyServer = _
  @transient var lobby2Player:LobbyServer = _
  @transient var lobby4Player:LobbyServer = _
  var gameResource:AbstractResource = _
  var lobbyResource: AbstractResource = _
  override def init() {
    super.init()

    val authStrat = new WiaAuthorizationStrategy();
    val securitySettings = getSecuritySettings();
    securitySettings.setAuthorizationStrategy(authStrat);
    securitySettings.setUnauthorizedComponentInstantiationListener(authStrat);

    getComponentInstantiationListeners.add(new SpringComponentInjector(this));


    /*val appContext = new FileSystemXmlApplicationContext(
      "/WEB-INF/applicationContext.xml"
    )*/

    var requiredWebApplicationContext: WebApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext)
    // of course, an ApplicationContext is just a BeanFactory
    val beanFactory = requiredWebApplicationContext.asInstanceOf[BeanFactory]

    var gameReservationService = beanFactory.getBean(classOf[GameReservationService])

    webStartResourceFactory = beanFactory.getBean(classOf[WebStartResourceFactory])
    //eventSystem = new EventSystem(this)
    try {
      lobby2Player = new LobbyServer(0, 2)
      lobby2Player.gameReservationService = gameReservationService
      lobby2Player.start
      lobby4Player = new LobbyServer(1, 4)
      lobby4Player.start
    } catch {
      case e:Exception => e.printStackTrace()
    }

    gameResource = webStartResourceFactory.createGameJnlpHandler

    getSharedResources().add(resourceKey, gameResource)

    mountResource("/start_game.jnlp", new SharedResourceReference(classOf[Application], resourceKey))



    /*val lobbyJnlpFile = new ContextRelativeResource("./Test.jnlp")
    val jnlpXML: String = new Scanner(lobbyJnlpFile.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next
    val jnlpXML2 = jnlpXML.replace("<resources>", "<resources><property name=\"lobbyPort\" value=\"12345\"/>")
      .replace("http://localhost:8080/", "http://" + LobbyServer.remoteIp +":8080/")
      .replace("Test.jnlp", "http://" + LobbyServer.remoteIp +":8080/lobby2.jnlp")
      */
    lobbyResource= webStartResourceFactory.createLobbyJnlpHandler //new ByteArrayResource("application/x-java-jnlp-file", jnlpXML2.getBytes, "lobby2.jnlp")
    getSharedResources().add(resourceKey2, lobbyResource)

    mountResource("/lobby2.jnlp", new SharedResourceReference(classOf[Application], resourceKey2))

  }

  override def onDestroy() {
    //eventSystem.shutdown()
    lobby2Player.stop();
    lobby4Player.stop();
    super.onDestroy()
  }

  //def getEventSystem = eventSystem
}

/**
 * @author kjozsa
 */

