package se.bupp.cs3k.server.facade

import org.apache.wicket.request.resource.{ContextRelativeResource, AbstractResource}
import org.apache.wicket.request.resource.IResource.Attributes
import org.apache.wicket.request.resource.AbstractResource.{WriteCallback, ResourceResponse}
import org.apache.wicket.util.time.{Duration, Time}
import java.util.Scanner
import se.bupp.cs3k.server.{GameServerPool, LobbyServer}
import java.io.IOException
import org.springframework.stereotype.Component
import se.bupp.cs3k.api._
import com.fasterxml.jackson.databind.ObjectMapper
import se.bupp.cs3k.server.service.GameReservationService._
import se.bupp.cs3k.server.service.GameReservationService
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.server.model.RunningGame
import org.apache.log4j.Logger
import user.{RegisteredPlayerIdentifier, AnonymousPlayerIdentifier, AbstractPlayerIdentifier}
import xml.Utility.Escapes
import xml.Utility
import se.bupp.cs3k.server.model.Model._
import se.bupp.cs3k.server.web.MyBean
import scala.Some
import se.bupp.cs3k.server.model.RunningGame
import scala.Left
import scala.Some
import scala.Right
import se.bupp.cs3k.server.model.RunningGame
import org.apache.wicket.request.http.WebResponse
import org.apache.wicket.request.resource.caching.NoOpResourceCachingStrategy
import se.bupp.cs3k.server.service.dao.UserDao


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-18
 * Time: 00:53
 * To change this template use File | Settings | File Templates.
 */

object  WebStartResourceFactory {
  var JNLP_MIME = "application/x-java-jnlp-file"
}
@Component
class WebStartResourceFactory {

  import WebStartResourceFactory._

  val log = Logger.getLogger(classOf[WebStartResourceFactory])

  def createGameJnlpHandler = new GameJnlpHandler
  def createLobbyJnlpHandler = new LobbyJnlpHandler

  var objectMapper:ObjectMapper = new ObjectMapper()

  @Autowired
  var gameReservationService:GameReservationService = _

  @Autowired
  var dao:MyBean = _

  @Autowired
  var userDao:UserDao = _


  class GameJnlpHandler extends AbstractResource {


    //override def getCachingStrategy = super.getCachingStrategy.

    override def newResourceResponse(p1: Attributes) = {


      val reservationIdOpt:Option[SeatId] = Option(p1.getParameters().get("reservation_id").toOptionalLong).map(p=> p.asInstanceOf[Long])
      val serverIdOpt = Option(p1.getParameters().get("server_id").toOptionalLong).map(p=> p.asInstanceOf[Long])

      val gameOccasionIdOpt:Option[OccassionId] = Option(p1.getParameters().get("game_occassion_id").toOptionalLong).map(p=> p.asInstanceOf[Long])

      val playerNameOpt = Option(p1.getParameters().get("player_name").toOptionalString)
      val userIdOpt:Option[UserId] = Option(p1.getParameters().get("user_id").toOptionalLong).map(p=> p.asInstanceOf[Long])


      log.info("playerNameOpt " + playerNameOpt)
      log.info("reservationIdOpt " + reservationIdOpt)

      val userOpt:Option[AbstractPlayerIdentifier] = userIdOpt.flatMap( id => userDao.findUser(id).map( p => new RegisteredPlayerIdentifier(p.id))).orElse( playerNameOpt.map(n => new AnonymousPlayerIdentifier(n)) )

      /*val user2:Option[AbstractPlayerIdentifier] = userIdOpt.flatMap( id => dao.findUser(id)) match {
        case Some(p) => Some(new PlayerIdentifierWithInfo(p.username,p.id))
        case None => playerNameOpt.map(n => new AnonymousPlayerIdentifier(n))
      }*/
      val userOrFail = userOpt.toRight("Couldnt Construct user")

      val serverOrFail = userOrFail.right.flatMap { k => serverIdOpt match {
        case Some(serverId) => // Rullande/Public
          Left("Not implemented")

        case None =>
          val instrOpt = (reservationIdOpt, gameOccasionIdOpt) match {
            case (None, Some(gameOccassionId)) => // Rullande/Public
                Right((false,gameOccassionId))
            case (Some(reservationId), _) => // Lobby
              gameReservationService.findReservation(reservationId) match {
                case Some((occassionId,_)) => Right((true, occassionId))
                case None => Left("Reservation not found")
              }
            case _ => Left("No game id sent")
          }
          val rgOpt = instrOpt.right.flatMap {
            case (canSpawn, occassionId) =>

              val alreadyRunningGame = GameServerPool.pool.findRunningGame(occassionId)
              val r = alreadyRunningGame.orElse {
                gameReservationService.findGame(occassionId).flatMap( g =>
                // TODO: Fix me - hardcoded below
                  if (!g.timeTriggerStart && canSpawn) {
                    Some(GameServerPool.pool.spawnServer(GameServerPool.tankGameSettings2, g))
                  } else {
                    None
                    //Left("Couldnt find game")
                  }
                )
              }
              val s = r.map(Right(_)).getOrElse(Left("Couldnt find game"))
              s
          }
          rgOpt
        }
      }

      val serverAndPassOrFail = serverOrFail.right.flatMap { rg =>
        val r = gameReservationService.createGamePass(rg,userOpt.get,reservationIdOpt) match {
          case Some(gamePass) => Right((rg,gamePass))
          case None => Left("Unable to acquire valid pass")
        }
        r

      }

      serverAndPassOrFail match {
        case Right((rg, gamePass)) => produceGameJnlp(p1, gamePass, rg)
        case Left(message) =>
          var response = new AbstractResource.ResourceResponse
          log.info("Resorting to fail response : " + message)

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

      /*
      if (response == null) {

      }








      response
       */

      /*
       case (None, None, Some(gameOccassionId)) => // Rullande/Public
         null
       case (Some(reservationId), _, _) => // Lobby
         gameReservationService.findReservation(reservationId) match {

         }
       case _ => null
     }     *&


     gameReservationService.findReservation(reservationId) match {
       case Some((occassionId,_)) =>

         log.info("Reservation found " + occassionId)
         var alreadyStartedGame: Option[RunningGame] = GameServerPool.pool.findRunningGame(occassionId)

         // Occassions are either created in lobby or pre persisted.
         log.info("alreadyStartedGame " + alreadyStartedGame)

         val runningGameOpt = alreadyStartedGame.orElse {
           // Not a lobby game, should exist a unstarded preconfigured game
           log.info("No running game found - try to launch")
           gameReservationService.findGame(occassionId).flatMap( g =>
           // TODO: Fix me - hardcoded below
             if (g.timeTriggerStart) {
               GameServerPool.pool.spawnServer(GameServerPool.tankGameSettings2, g)
             } else {
               None
             }
           )
         }
         runningGameOpt match {
           case Some(rg:RunningGame) =>
             val gamePass = gameReservationService.createGamePass(reservationId, occassionId)
             produceGameJnlp(target, gamePass, rg)
           case None => null
         }
       case None =>
         log.error("No reservationId of " + reservationId + " found")
         null
     } */


    }

    def produceGameJnlp(p1: Attributes, gamePass:AbstractGamePass, game: RunningGame): AbstractResource.ResourceResponse = {
      var response = new ResourceResponse
      response.setContentType(JNLP_MIME)
      //response.setLastModified(Time.now())
      //response.disableCaching()
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
              .replace("http://localhost:8080/game_deploy_dir_tmp/tanks", "http://" + LobbyServer.remoteIp + ":8080/game_deploy_dir_tmp/tanks")
              .replace("Game.jnlp", "http://" + LobbyServer.remoteIp + ":8080/" + Utility.escape(p1.getRequest.getClientUrl.toString))


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




    override def getCachingStrategy = NoOpResourceCachingStrategy.INSTANCE

    override def newResourceResponse(p1: Attributes) = {

      val playerNameOpt = Option.apply(p1.getParameters().get("player_name").toOptionalString).map(p=> p.asInstanceOf[String])
      val userIdOpt:Option[UserId] = Option.apply(p1.getParameters().get("user_id").toOptionalLong).map(p=> p.asInstanceOf[Long])

      //val playerNameOpt = Option.apply(target.getParameters().get("playerName").toString)

      var response = new ResourceResponse {}

      response.setContentType(JNLP_MIME)
      //response.disableCaching()
      //response.setLastModified(Time.now())
      //response.setCacheDuration(Duration.NONE)
      response.setFileName("lobby2.jnlp")
      //response.setContentDisposition()
      //response.setCacheScope(null)

      response.setWriteCallback(new WriteCallback {
        def writeData(p2: Attributes) {
          try {

            val lobbyJnlpFile = new ContextRelativeResource("./lobbyX.jnlp")
            //val launchGameJnlp = new ContextRelativeResource("/game_deploy_dir_tmp/tanks/Game.jnlp")
            val jnlpXML: String = new Scanner(lobbyJnlpFile.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next


            /*
          val jnlpXML: String = new Scanner(lobbyJnlpFile.getCacheableResourceStream.getInputStream).useDelimiter("\\A").next
          val jnlpXML2 = jnlpXML.replace("<resources>", "<resources><property name=\"lobbyPort\" value=\"12345\"/>")
            .replace("http://localhost:8080/", "http://" + LobbyServer.remoteIp +":8080/")
            .replace("Test.jnlp", "http://" + LobbyServer.remoteIp +":8080/lobby2.jnlp")
            */
            //lobbyResource= new ByteArrayResource("application/x-java-jnlp-file", jnlpXML2.getBytes, "lobby2.jnlp")
            //getSharedResources().add(resourceKey2, lobbyResource)


            //println(target.getRequest.getClientUrl.toString)

            val resourcesNew = "<resources>" +
              "<property name=\"javaws.lobbyPort\" value=\"12345\"/>" +
              "<property name=\"javaws.lobbyHost\" value=\"" + LobbyServer.remoteIp  + "\"/>" +
              userIdOpt.map(a => "<property name=\"javaws.userId\" value=\"" + a + "\"/>").getOrElse(
                playerNameOpt.map(a => "<property name=\"javaws.playerName\" value=\"" + a + "\"/>").getOrElse("")
              )

            println("playerNameOpt " + playerNameOpt)
            val jnlpXMLModified = jnlpXML
              .replace("http://localhost:8080/", "http://" + LobbyServer.remoteIp +":8080/")
              .replace("lobbyX.jnlp", "http://" + LobbyServer.remoteIp +":8080/lobby2.jnlp?" +
                userIdOpt.map(a => "userId=" + a ).getOrElse(
                  playerNameOpt.map(a => "playerName=" + a).getOrElse("")
                )
            )
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
