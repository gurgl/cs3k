package se.bupp.cs3k.server.facade

import org.apache.wicket.request.resource.{ContextRelativeResource, AbstractResource}
import org.apache.wicket.request.resource.IResource.Attributes
import org.apache.wicket.request.resource.AbstractResource.{WriteCallback, ResourceResponse}
import org.apache.wicket.util.time.Time
import java.util.Scanner
import se.bupp.cs3k.server.{GameServerPool, ServerLobby}
import java.io.IOException
import org.springframework.stereotype.Component
import se.bupp.cs3k.api.{AbstractGamePass, Ticket}
import com.fasterxml.jackson.databind.ObjectMapper
import se.bupp.cs3k.server.service.GameReservationService._
import se.bupp.cs3k.server.service.GameReservationService
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.server.model.RunningGame
import org.apache.log4j.Logger
import xml.Utility.Escapes
import xml.Utility


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-18
 * Time: 00:53
 * To change this template use File | Settings | File Templates.
 */

@Component
class WebStartResourceFactory {

  val log = Logger.getLogger(classOf[WebStartResourceFactory])

  def createGameJnlpHandler = new GameJnlpHandler
  def createLobbyJnlpHandler = new LobbyJnlpHandler

  var objectMapper:ObjectMapper = new ObjectMapper()

  @Autowired
  var gameReservationService:GameReservationService = _

  class GameJnlpHandler extends AbstractResource {


    //override def getCachingStrategy = super.getCachingStrategy.

    override def newResourceResponse(p1: Attributes) = {


      val reservationIdOpt:Option[SeatId] = Option(p1.getParameters().get("seat_id").toOptionalLong)
      val publicGameIdOpt = Option(p1.getParameters().get("public_game_id").toOptionalLong)
      val playerNameOpt = Option(p1.getParameters().get("player_name").toOptionalString)

      log.info("reservationIdOpt " + reservationIdOpt)

      var response: AbstractResource.ResourceResponse = (reservationIdOpt, publicGameIdOpt, playerNameOpt) match {
        case (None, Some(publicGameId), Some(playerName)) => null
        case (Some(reservationId), _, _) =>
          gameReservationService.findReservation(reservationId) match {
            case Some((occassionId,_)) =>

              log.info("Reservation found " + occassionId)
              var testar: Option[RunningGame] = GameServerPool.pool.findRunningGame(occassionId)
              // Occassions are either created in lobby or pre persisted.
              log.info("testar " + testar)
              val runningGameOpt = testar.orElse {
                // Not a lobby game, should exist a unstarded preconfigured game
                log.info("No running game found - try to launch")
                gameReservationService.findGame(occassionId).map( g =>
                  // TODO: Fix me - hardcoded below
                  GameServerPool.pool.spawnServer(GameServerPool.tankGameSettings2, g)
                )
              }
              runningGameOpt match {
                case Some(rg) =>
                  val gamePass = gameReservationService.createGamePass(reservationId, occassionId)
                  produceGameJnlp(p1, gamePass, rg)
                case None => null
              }
           case None =>
             log.error("No reservationId of " + reservationId + " found")
             null
          }
        case _ => null
      }


      if (response == null) {
        response = new AbstractResource.ResourceResponse
        log.info("Resorting to fail response")

        response.setContentType("plain/text")
        response.setLastModified(Time.now())
        response.disableCaching()
        response.setFileName("error.txt")
        response.setWriteCallback(new WriteCallback {
          def writeData(p2: Attributes) {
            p2.getResponse.write("Erronous request")
          }
        })
      }

      response
    }

    def produceGameJnlp(p1: Attributes, gamePass:AbstractGamePass, game: RunningGame): AbstractResource.ResourceResponse = {
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

            val pi = objectMapper.writeValueAsString(gamePass)


            //var gamePass = new Ticket(reservationId)
            val props = game.processSettings.props
            println(p1.getRequest.getClientUrl.toString)
            val jnlpXMLModified = jnlpXML.replace("<resources>", "<resources>" +
              "<property name=\"playerInfo\" value=\"" + Utility.escape(pi) + "\"/>" +
              "<property name=\"gamePortUDP\" value=\"" + props("gamePortUDP") + "\"/>" +
              "<property name=\"gamePortTCP\" value=\"" + props("gamePortTCP") + "\"/>" +
              "<property name=\"gameHost\" value=\"" + props("gameHost") + "\"/>")
              .replace("http://localhost:8080/game_deploy_dir_tmp/tanks", "http://" + ServerLobby.remoteIp + ":8080/game_deploy_dir_tmp/tanks")
              .replace("Game.jnlp", "http://" + ServerLobby.remoteIp + ":8080/" + p1.getRequest.getClientUrl.toString)


            //var writer: PrintWriter = new PrintWriter(p2.getResponse.getOutputStream)
            //writer.print(jnlpXMLModified)
            p2.getResponse.write(jnlpXMLModified)
            //ImageIO.write(img, "png", output);

          }
          catch {
            case ex: IOException => ex.printStackTrace() /* never swallow exceptions! */
          }

        }
      })
      //AbstractResource.this.configureResponse(response, attributes);
      response
    }
  }


  class LobbyJnlpHandler extends AbstractResource {

    override def newResourceResponse(p1: Attributes) = {

      val playerNameOpt = Option.apply(p1.getParameters().get("player_name").toString)
      val userIdOpt = Option.apply(p1.getParameters().get("user_id").toString)

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
              userIdOpt.map(a => "<property name=\"userId\" value=\"" + a + "\"/>").getOrElse(
                playerNameOpt.map(a => "<property name=\"playerName\" value=\"" + a + "\"/>").getOrElse("")
              )

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
