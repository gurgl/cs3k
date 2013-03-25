package se.bupp.cs3k.server.facade

import lobby.LobbyServer
import org.apache.wicket.request.resource.{ContextRelativeResource, AbstractResource}
import org.apache.wicket.request.resource.IResource.Attributes
import org.apache.wicket.request.resource.AbstractResource.{WriteCallback, ResourceResponse}
import org.apache.wicket.util.time.{Duration, Time}
import java.util.Scanner
import se.bupp.cs3k.server.{Cs3kConfig}
import java.io.{File, IOException}
import org.springframework.stereotype.Component
import se.bupp.cs3k.api._
import com.fasterxml.jackson.databind.ObjectMapper

import se.bupp.cs3k.server.service.GameReservationService
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.server.model.{AbstractUser, AnonUser, RegedUser, RunningGame}
import org.apache.log4j.Logger
import user.{RegisteredPlayerIdentifier, AnonymousPlayerIdentifier, AbstractPlayerIdentifier}
import xml.Utility.Escapes
import xml.Utility
import se.bupp.cs3k.server.model.Model._
import scala.Some
import scala.Left
import scala.Some
import scala.Right
import se.bupp.cs3k.server.Util.eitherSuccess

import org.apache.wicket.request.http.WebResponse
import org.apache.wicket.request.resource.caching.NoOpResourceCachingStrategy
import se.bupp.cs3k.server.service.dao.UserDao
import org.slf4j.LoggerFactory
import se.bupp.cs3k.server.service.gameserver.{GameServerPool, GameServerRepository}
import java.net.{URI, URLEncoder}


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-18
 * Time: 00:53
 * To change this template use File | Settings | File Templates.
 */

object  WebStartResourceFactory {
  var JNLP_MIME = "application/x-java-jnlp-file"
  val log = LoggerFactory.getLogger(this.getClass)

  class LobbyJnlpHandler extends AbstractResource {

    override def getCachingStrategy = NoOpResourceCachingStrategy.INSTANCE

      override def newResourceResponse(p1: Attributes) = {

      val playerNameOpt = Option.apply(p1.getParameters().get("player_name").toOptionalString).map(p=> p.asInstanceOf[String])
      val userIdOpt:Option[UserId] = Option.apply(p1.getParameters().get("user_id").toOptionalLong).map(p=> p.asInstanceOf[Long])
      val lobbyIdOpt:Option[String] = Option.apply(p1.getParameters().get("lobby_id").toOptionalString)

      //val playerNameOpt = Option.apply(target.getParameters().get("playerName").toString)

      var response = new ResourceResponse {}

      response.setContentType(JNLP_MIME)
      //response.disableCaching()
      //response.setLastModified(Time.now())
      //response.setCacheDuration(Duration.NONE)
      response.setFileName("lobby2.jnlp")
      //response.setContentDisposition()
      //response.setCacheScope(null)

      val (port,clientMode, lobbyId) = {
        val (lobbyId,lobbyServer) = lobbyIdOpt.map( l => LobbyServer.publicLobbies.find( _._1 == l).get).getOrElse(LobbyServer.publicLobbies.head)
        (lobbyServer.portId, lobbyServer.lobbyHandler.clientMode, lobbyId)

      }

      response.setWriteCallback(new WriteCallback {
        def writeData(p2: Attributes) {
          try {

            val lobbyJnlpFile = new ContextRelativeResource("./lobbyX.jnlp")
            //val launchGameJnlp = new ContextRelativeResource("/game_deploy_dir_tmp/tanks/Game.jnlp")

            val jnlpXML: String = new Scanner(lobbyJnlpFile.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next

            val resourcesNew = "<resources>" +
              "<property name=\"javaws.lobbyPort\" value=\"" + port + "\"/>" +
              "<property name=\"javaws.lobbyMode\" value=\"" + clientMode + "\"/>" +
              "<property name=\"javaws.lobbyHost\" value=\"" + Cs3kConfig.REMOTE_IP  + "\"/>" +
              userIdOpt.map(a => "<property name=\"javaws.userId\" value=\"" + a + "\"/>").getOrElse(
                playerNameOpt.map(a => "<property name=\"javaws.playerName\" value=\"" + a + "\"/>").getOrElse("")
              )


            log.debug("Lobby req : ")
            log.debug("userIdOpt " + userIdOpt)
            log.debug("playerNameOpt " + playerNameOpt)
            log.debug("lobbyId " + lobbyId + " : " + lobbyIdOpt)


            val jnlpXMLModified = jnlpXML
              .replace(Cs3kConfig.Lobby.jnlpCodeBasePlaceholder, Cs3kConfig.remoteURL)
              .replace("lobbyX.jnlp",  Cs3kConfig.remoteURL + "lobby2.jnlp?" +
              userIdOpt.map(a => "user_id=" + a ).getOrElse(
                playerNameOpt.map(a => "player_name=" + a).getOrElse("")
              ) + "&lobby_id="+URLEncoder.encode(lobbyId,"ASCII")
            ).replace("<resources>", resourcesNew)

            p2.getResponse.write(jnlpXMLModified)
          }
          catch { case ex:IOException => ex.printStackTrace()/* never swallow exceptions! */ }

        }
      })
      //AbstractResource.this.configureResponse(response, attributes);
      response

    }
  }

  class GameJnlpHandler(val factory:WebStartResourceFactory, codeBaseFSRoot:URI) extends AbstractResource {

    //override def getCachingStrategy = super.getCachingStrategy.

    override def newResourceResponse(p1: Attributes) = {


      val reservationIdOpt:Option[GameServerReservationId] = Option(p1.getParameters().get("reservation_id").toOptionalLong).map(p=> p.asInstanceOf[Long])
      val serverIdOpt = Option(p1.getParameters().get("server_id").toOptionalLong).map(p=> p.asInstanceOf[Long])

      val gameOccasionIdOpt:Option[GameOccassionId] = Option(p1.getParameters().get("game_occassion_id").toOptionalLong).map(p=> p.asInstanceOf[Long])

      val playerNameOpt = Option(p1.getParameters().get("player_name").toOptionalString)
      val userIdOpt:Option[UserId] = Option(p1.getParameters().get("user_id").toOptionalLong).map(p=> p.asInstanceOf[Long])


      factory.log.info("playerNameOpt " + playerNameOpt)
      factory.log.info("reservationIdOpt " + reservationIdOpt)

      val serverAndPassOrFail = factory.getServerAndCredentials(userIdOpt, reservationIdOpt, serverIdOpt, gameOccasionIdOpt, playerNameOpt)

      serverAndPassOrFail match {
        case Right((rg, gamePass)) => factory.produceGameJnlp(p1, gamePass, rg, codeBaseFSRoot)
        case Left(message) =>
          var response = new AbstractResource.ResourceResponse
          factory.log.info("Resorting to fail response : " + message)

          response.setContentType("application/html")
          response.setLastModified(Time.now())
          response.disableCaching()
          response.setFileName("error.txt")
          response.setWriteCallback(new WriteCallback {
            def writeData(p2: Attributes) {
              p2.getResponse.write("Erronous request : " + message)
            }
          })
          response
      }

    }
  }
}


@Component
class WebStartResourceFactory {

  import WebStartResourceFactory._

  val log = Logger.getLogger(classOf[WebStartResourceFactory])

  def createGameJnlpHandler(path:URI) = new GameJnlpHandler(this,path)

  def createLobbyJnlpHandler = new LobbyJnlpHandler

  var objectMapper:ObjectMapper = new ObjectMapper()

  @Autowired
  var gameReservationService:GameReservationService = _

  @Autowired
  var userDao:UserDao = _


  def produceGameJnlp(attributes: Attributes, gamePass:AbstractGamePass, game: RunningGame, codeBaseFSRoot:URI): AbstractResource.ResourceResponse = {
    var response = new ResourceResponse
    response.setContentType(JNLP_MIME)
    //response.setLastModified(Time.now())
    //response.disableCaching()
    response.setFileName("start_game.jnlp")
    response.setWriteCallback(new WriteCallback {
      def writeData(p2: Attributes) {
        try {

          var codeBaseRoot = "deploy/tanks/"


          val launchGameJnlp = new File(new File(codeBaseFSRoot),"Game.jnlp");
          val jnlpXML: String = new Scanner(launchGameJnlp).useDelimiter("\\A").next

          val pi = objectMapper.writeValueAsString(gamePass)


          //var gamePass = new Ticket(reservationId)
          val props = game.processSettings.props
          log.debug(attributes.getRequest.getClientUrl.toString)

          val jnlpXMLModified = jnlpXML.replace("<resources>", "<resources>" +
            "<property name=\"playerInfo\" value=\"" + Utility.escape(pi) + "\"/>" +

            // Game specific
            "<property name=\"gamePortUDP\" value=\"" + props("gamePortUDP") + "\"/>" +
            "<property name=\"gamePortTCP\" value=\"" + props("gamePortTCP") + "\"/>" +
            "<property name=\"gameHost\" value=\"" + props("gameHost") + "\"/>")
            .replace(Cs3kConfig.TankGame.jnlpCodeBasePlaceholder, Cs3kConfig.remoteURL + codeBaseRoot)
            .replace("Game.jnlp", Cs3kConfig.remoteURL + Utility.escape(attributes.getRequest.getUrl.toString))


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



  def getServerAndCredentials(userIdOpt:Option[UserId], reservationIdOpt: Option[GameServerReservationId], serverIdOpt: Option[Long], gameOccasionIdOpt:Option[GameOccassionId], playerNameOpt:Option[String]) = {
    import se.bupp.cs3k.server.Util.eitherSuccess
    val serverAndPassValidation:Either[String,(RunningGame,AbstractGamePass)] = {
      val userOpt = (userIdOpt, playerNameOpt) match {
      case (None, Some(name)) =>
        Some(AnonUser(name))
      case (Some(userId), _) =>
        userDao.findUser(userId).map(
          p => new RegedUser(userId)
        )
      case (_, _) =>
        None //Left("No user identification given")
      }
        //val userValidation = userOpt.toRight("Couldnt Construct user")
        //userValidation.onSuccess { existingUserId =>
          try {

            val rgAndPass = (serverIdOpt, reservationIdOpt, gameOccasionIdOpt, userOpt) match {
              case (None, Some(reservationId), None, uOpt) =>
                gameReservationService.playNonScheduledClosed(reservationId)
              case (None, None , Some(gameOccassionId), Some(existingUserId:RegedUser)) =>
                gameReservationService.playScheduledClosed(gameOccassionId, existingUserId)

              case (Some(serverId), None, None, Some(p)) =>
                // Continuous
                gameReservationService.playOpenServer(serverId,p)
              /*case (None, Some(reservationId)) =>
                gameReservationService.playNonScheduledClosed(reservationId) */
              case _ => throw new IllegalArgumentException("No game identification given " + (serverIdOpt, reservationIdOpt, gameOccasionIdOpt, userOpt))


            }
            Right(rgAndPass)

          } catch {
            case e:IllegalArgumentException => Left(e.getMessage)
          }
    }

    serverAndPassValidation
  }
}
