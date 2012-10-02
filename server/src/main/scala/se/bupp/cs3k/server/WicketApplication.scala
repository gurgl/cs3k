/**
 *
 */
package se.bupp.cs3k.server

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
    val resourceKey = "JNLP_GENERATOR"


    getSharedResources().add(resourceKey, new AbstractResource {

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
    })

    mountResource("/start_game", new SharedResourceReference(classOf[Application], resourceKey))
  }

  override def onDestroy() {
    //eventSystem.shutdown()
    lobby2Player.stop();
    lobby4Player.stop();
    super.onDestroy()
  }

  //def getEventSystem = eventSystem
}


