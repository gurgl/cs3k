package se.bupp.cs3k.server

import facade.lobby.{LobbyServer}
import facade.WebStartResourceFactory
import service.lobby.AbstractLobbyQueueHandler
import service.{GameService, RankingService, GameReservationService}
import service.gameserver.{GameServerRepository, GameServerSpecification}
import service.resourceallocation.{ServerAllocator, ResourceNeeds}
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.ApplicationContext

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-19
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */

object Init {
  var lobby2Player:LobbyServer = _
  var lobby4Player:LobbyServer = _

  def cleanup() {
    println("Init cleanup")
    lobby2Player.stop();
    lobby4Player.stop();

  }

  def setupSpringDeps(beanFactory :BeanFactory) {
    // of course, an ApplicationContext is just a BeanFactory


    var gameReservationService = beanFactory.getBean(classOf[GameReservationService])
    var rankingService= beanFactory.getBean(classOf[RankingService])


    //eventSystem = new EventSystem(this)
    try {
      AbstractLobbyQueueHandler.gameReservationService = gameReservationService
      AbstractLobbyQueueHandler.rankingService = rankingService

      lobby2Player = LobbyServer.createContinousForNonPersistedGameOcassionsInstance(2,('TankGame, 'TG2Player))
      lobby2Player.start
      lobby4Player = LobbyServer.createContinous2vsNTeamForNonPersistedGameOcassionsInstance(2,('TankGame, 'TG4Player))
      lobby4Player.start


    } catch {
      case e:Exception => e.printStackTrace()
    }

    persistAnyNewRules(beanFactory)

  }

  def persistAnyNewRules(beanFactory :BeanFactory) {
    var gameService = beanFactory.getBean(classOf[GameService])
    GameServerRepository.gameServerTypes.foreach { case (gsType,spec) =>
      val entity = gameService.getOrCreateGameTypeEntity(gsType)
    }

    GameServerRepository.gameServerSetups.foreach { case ((gameTypeId,gameSetupTypeId),spec) =>
      println("gameTypeId,gameSetupTypeId" + (gameTypeId,gameSetupTypeId))
      val entity = gameService.getOrCreateGameSetupTypeEntity(gameTypeId,gameSetupTypeId)
    }
  }


}
class Init {

  var lobby2Player:LobbyServer = _
  var lobby4Player:LobbyServer = _

  import Init._

  val gameIdentifier = "tanks"

  val tankGameServer = new GameServerSpecification("java " +
    "-jar " +
    "C:/dev/workspace/opengl-tanks/server/target/scala-2.10/server_2.10-0.1-one-jar.jar" +
    " ", new ResourceNeeds(1,1))

  val tankGameSettings2  = {

    tankGameServer.create(
      " --tcp-port ${tcp[0]} --udp-port ${udp[0]} --master-host ${cs3k_host} --master-port ${cs3k_port} --game-setup ffa2", Cs3kConstants.LAUNCHER_PATH + gameIdentifier + "/"+ "start_game.jnlp",
      Map("gamePortUDP" -> "${udp[0]}", "gamePortTCP" -> "${tcp[0]}", "gameHost" -> Cs3kConfig.REMOTE_IP)
    )
  }

  val tankGameSettings4  = tankGameServer.create(
    " --tcp-port ${tcp[0]} --udp-port ${udp[0]} --master-host ${cs3k_host} --master-port ${cs3k_port} --game-setup 2vs2", Cs3kConstants.LAUNCHER_PATH + gameIdentifier + "/"+ "start_game.jnlp",
    Map("gamePortUDP" -> "${udp[0]}", "gamePortTCP" -> "${tcp[0]}", "gameHost" -> Cs3kConfig.REMOTE_IP)
  )


  GameServerRepository.add('TankGame, tankGameServer)
  GameServerRepository.addProcessTemplate(('TankGame, 'TG2Player), tankGameSettings2)
  GameServerRepository.addProcessTemplate(('TankGame, 'TG4Player), tankGameSettings4)


}
