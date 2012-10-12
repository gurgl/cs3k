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

import org.apache.wicket.protocol.ws.api.SimpleWebSocketConnectionRegistry
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
import se.bupp.cs3k.server.{ServerLobby, ApiPlayer, web}
import se.bupp.cs3k.Greeting
import org.apache.wicket.request.{Response, Request}


//import akka.actor.{Props, Actor, ActorSystem}
//import akka.event.Logging
//import akka.util.duration._
import se.bupp.cs3k.Greeting






@Component("mySBean")
class MyBeanImpl extends MyBean {

  @PersistenceContext(unitName="MyPersistenceUnit")
  var em:EntityManager = _

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  def insert(a:ApiPlayer) {
    em.persist(a)
  }
  def findUser(s:String) = {
    var q: TypedQuery[ApiPlayer] = em.createQuery[ApiPlayer]("from ApiPlayer p where p.username = :name",classOf[ApiPlayer])
    q.setParameter("name",s)
    import scala.collection.JavaConversions.asScalaBuffer
    q.getResultList.headOption.getOrElse(null)
  }

  import scala.collection.JavaConversions.asScalaBuffer

  //@Transactional()
  def read() {
    var q: Query = em.createQuery("from ApiPlayer")
    val res = q.getResultList.mkString(",")
    println(res)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  def store() {
    //instance.useBeanFactory()

    em.persist(new ApiPlayer("Tja" + System.currentTimeMillis()))
  }
}

class WicketApplication extends WebApplication {

  import WicketApplication._
  //var eventSystem: EventSystem = _


  val logger = LoggerFactory.getLogger(classOf[WicketApplication])


  override def newSession(request: Request, response: Response) = new WiaSession(request)

  def getHomePage() = classOf[TheHomePage]

  //@transient var lobby : ServerLobby = _
  @transient var lobby2Player:ServerLobby = _
  @transient var lobby4Player:ServerLobby = _
  var gameResource:AbstractResource = _
  var lobbyResource: ByteArrayResource = _
  override def init() {
    super.init()

    val authStrat = new WiaAuthorizationStrategy();
    val securitySettings = getSecuritySettings();
    securitySettings.setAuthorizationStrategy(authStrat);
    securitySettings.setUnauthorizedComponentInstantiationListener(authStrat);

    getComponentInstantiationListeners.add(new SpringComponentInjector(this));
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

    gameResource = new AbstractResource {

      override def newResourceResponse(p1: Attributes) = {

        val query = p1.getParameters().get("slotId").toString;


        // generate an image containing the query argument
        /*final BufferedImage img = new BufferedImage(100, 100,
          BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE);
        g2.drawString(query, img.getWidth() / 2, img.getHeight() / 2);*/

        // return the image as a PNG stream

        var response = new ResourceResponse
        response.setContentType("application/x-java-jnlp-file")
        response.setLastModified(Time.now())
        response.setFileName("start_the_game.jnlp")
        response.setWriteCallback(new WriteCallback {
          def writeData(p2: Attributes) {
            try {

              val lobbyJnlpFile = new ContextRelativeResource("/game_deploy_dir_tmp/tanks/Game.jnlp")
              val jnlpXML: String = new Scanner(lobbyJnlpFile.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next

              println(p1.getRequest.getClientUrl.toString)
              val jnlpXMLModified = jnlpXML.replace("<resources>", "<resources>" +
                "<property name=\"gamePortUDP\" value=\"54777\"/>"+
                "<property name=\"gamePortTCP\" value=\"54555\"/>" +
                "<property name=\"gameHost\" value=\"" + ServerLobby.remoteIp + "\"/>")
                .replace("http://localhost:8080/game_deploy_dir_tmp/tanks", "http://" + ServerLobby.remoteIp +":8080/game_deploy_dir_tmp/tanks")
                .replace("Game.jnlp", "http://" + ServerLobby.remoteIp +":8080/" + p1.getRequest.getClientUrl.toString)


              //var writer: PrintWriter = new PrintWriter(p2.getResponse.getOutputStream)
              //writer.print(jnlpXMLModified)
              p2.getResponse.write(jnlpXMLModified)
              //ImageIO.write(img, "png", output);

            }
            catch { case ex:IOException => ex.printStackTrace()/* never swallow exceptions! */ }

          }
        })
        //AbstractResource.this.configureResponse(response, attributes);
        response

      }
    }

    getSharedResources().add(resourceKey, gameResource)

    mountResource("/start_game", new SharedResourceReference(classOf[Application], resourceKey))



    val lobbyJnlpFile = new ContextRelativeResource("./Test.jnlp")
    val jnlpXML: String = new Scanner(lobbyJnlpFile.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next
    val jnlpXML2 = jnlpXML.replace("<resources>", "<resources><property name=\"lobbyPort\" value=\"12345\"/>")
      .replace("http://localhost:8080/", "http://" + ServerLobby.remoteIp +":8080/")
      .replace("Test.jnlp", "http://" + ServerLobby.remoteIp +":8080/lobby2.jnlp")

    lobbyResource= new ByteArrayResource("application/x-java-jnlp-file", jnlpXML2.getBytes, "lobby2.jnlp")
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

object WicketApplication {

  def get = WebApplication.get().asInstanceOf[WicketApplication]
  val resourceKey = "JNLP_GENERATOR"
  val resourceKey2 = "JNLP_GENERATOR_lobby"
}

trait MyBean {
  def read()

  def insert(a:ApiPlayer)
  def findUser(s:String) : ApiPlayer

  def store()
}