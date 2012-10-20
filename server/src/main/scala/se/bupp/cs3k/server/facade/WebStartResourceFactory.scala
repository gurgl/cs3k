package se.bupp.cs3k.server.facade

import org.apache.wicket.request.resource.{ContextRelativeResource, AbstractResource}
import org.apache.wicket.request.resource.IResource.Attributes
import org.apache.wicket.request.resource.AbstractResource.{WriteCallback, ResourceResponse}
import org.apache.wicket.util.time.Time
import java.util.Scanner
import se.bupp.cs3k.server.ServerLobby
import java.io.IOException
import org.springframework.stereotype.Component
import se.bupp.cs3k.api.Ticket
import com.fasterxml.jackson.databind.ObjectMapper


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-18
 * Time: 00:53
 * To change this template use File | Settings | File Templates.
 */

@Component
class WebStartResourceFactory {

  def createGameJnlpHandler = new GameJnlpHandler
  def createLobbyJnlpHandler = new LobbyJnlpHandler

  var objectMapper:ObjectMapper = new ObjectMapper()

  class GameJnlpHandler extends AbstractResource {


    //override def getCachingStrategy = super.getCachingStrategy.

    override def newResourceResponse(p1: Attributes) = {


      val reservationIdStr = p1.getParameters().get("slot_id").toString;

      var response = new ResourceResponse
      response.setContentType("application/x-java-jnlp-file")
      response.setLastModified(Time.now())
      response.disableCaching()
      response.setFileName("start_game.jnlp")
      response.setWriteCallback(new WriteCallback {
        def writeData(p2: Attributes) {
          try {

            val launchGameJnlp = new ContextRelativeResource("/game_deploy_dir_tmp/tanks/Game.jnlp")
            val jnlpXML: String = new Scanner(launchGameJnlp.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next


            var ticket = new Ticket(reservationIdStr.toLong)

            val pi = objectMapper.writeValueAsString(ticket)

            println(p1.getRequest.getClientUrl.toString)
            val jnlpXMLModified = jnlpXML.replace("<resources>", "<resources>" +
              "<property name=\"playerInfo\" value=\""+ pi + "\"/>" +
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


  class LobbyJnlpHandler extends AbstractResource {

    override def newResourceResponse(p1: Attributes) = {

      val playerNameOpt = Option.apply(p1.getParameters().get("player_name").toString)
      //val playerNameOpt = Option.apply(p1.getParameters().get("playerName").toString)

      var response = new ResourceResponse
      response.setContentType("application/x-java-jnlp-file")
      response.setLastModified(Time.now())
      response.setFileName("lobby2.jnlp")
      response.setWriteCallback(new WriteCallback {
        def writeData(p2: Attributes) {
          try {

            val lobbyJnlpFile = new ContextRelativeResource("./Test.jnlp")
            //val launchGameJnlp = new ContextRelativeResource("/game_deploy_dir_tmp/tanks/Game.jnlp")
            val jnlpXML: String = new Scanner(lobbyJnlpFile.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next


            /*
          val jnlpXML: String = new Scanner(lobbyJnlpFile.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next
          val jnlpXML2 = jnlpXML.replace("<resources>", "<resources><property name=\"lobbyPort\" value=\"12345\"/>")
            .replace("http://localhost:8080/", "http://" + ServerLobby.remoteIp +":8080/")
            .replace("Test.jnlp", "http://" + ServerLobby.remoteIp +":8080/lobby2.jnlp")
            */
            //lobbyResource= new ByteArrayResource("application/x-java-jnlp-file", jnlpXML2.getBytes, "lobby2.jnlp")
            //getSharedResources().add(resourceKey2, lobbyResource)


            //println(p1.getRequest.getClientUrl.toString)

            val resourcesNew = "<resources>" +
              "<property name=\"lobbyPort\" value=\"12345\"/>" +
              "<property name=\"lobbyHost\" value=\"" + ServerLobby.remoteIp  + "\"/>" +
              playerNameOpt.map(a => "<property name=\"playerName\" value=\"" + a + "\"/>").getOrElse("")

            println("playerNameOpt " + playerNameOpt)
            val jnlpXMLModified = jnlpXML
              .replace("http://localhost:8080/", "http://" + ServerLobby.remoteIp +":8080/")
              .replace("Test.jnlp", "http://" + ServerLobby.remoteIp +":8080/lobby2.jnlp")
              .replace("<resources>", resourcesNew)




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
}
