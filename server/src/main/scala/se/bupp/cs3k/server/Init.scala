package se.bupp.cs3k.server

import facade.lobby.{LobbyServer}
import facade.WebStartResourceFactory
import model.{GameType, GameSetupType}
import model.Model._
import service.dao.GameOccassionDao
import service.lobby.AbstractLobbyQueueHandler
import service.{GameReservationServiceStore, GameService, RankingService, GameReservationService}
import service.gameserver.{GameProcessTemplate, GameServerRepository, GameServerSpecification}
import service.resourceallocation.{ServerAllocator, ResourceNeeds}
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.ApplicationContext
import java.lang.Long

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
  var gameReservationServiceStore:GameReservationServiceStore = _


  var gameSetupMeta = Map[(GameServerTypeId, GameProcessTemplateId),(GameProcessTemplate,GameProcessTemplateId => GameSetupType)]()
  var gameMeta = Map[GameServerTypeId,(GameServerSpecification,GameServerTypeId=> GameType)]()

  var gameServerRepository:GameServerRepository = null


  def cleanup() {
    println("Init cleanup")
    lobby2Player.stop();
    lobby4Player.stop();
  }

  def init {
    gameSetupMeta = Map.empty
    gameMeta = Map.empty

    gameServerRepository = new GameServerRepository
    gameReservationServiceStore = new GameReservationServiceStore

  }

  def setupSpringDeps(beanFactory :BeanFactory) {
    // of course, an ApplicationContext is just a BeanFactory


    var gameReservationService = beanFactory.getBean(classOf[GameReservationService])
    var rankingService= beanFactory.getBean(classOf[RankingService])

    persistAnyNewRules(beanFactory)


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


    var godao = beanFactory.getBean(classOf[GameOccassionDao])
    var seqId: Long = godao.findMaxSessionId() match  {
      case null => 100
      case x => x+1
    }
    gameReservationServiceStore = new GameReservationServiceStore
    gameReservationServiceStore.init(seqId)
  }

  def persistAnyNewRules(beanFactory :BeanFactory) {
    var gameService = beanFactory.getBean(classOf[GameService])
    gameMeta.foreach { case (gsType,(spec,temp)) =>

      gameServerRepository.add('TankGame, spec)

      val entity = gameService.getOrCreateGameTypeEntity(gsType,temp)
    }

    gameSetupMeta.foreach { case ((gameTypeId,gameSetupTypeId),(spec,temp)) =>
      println("gameTypeId,gameSetupTypeId" + (gameTypeId,gameSetupTypeId))
      gameServerRepository.addProcessTemplate((gameTypeId,gameSetupTypeId),spec)
      val entity = gameService.getOrCreateGameSetupTypeEntity(gameTypeId,gameSetupTypeId,temp)
    }

    gameServerRepository.gameServerSetups.keys.foreach(println)
  }
  //


}
class Init {

  var lobby2Player:LobbyServer = _
  var lobby4Player:LobbyServer = _

  import Init._

  val gameIdentifier = "tanks"

  val tankGameServer = new GameServerSpecification("java " +
    "-jar " +
    Cs3kConfig.TankGame.SERVER_JAR_PATH +
    " ", new ResourceNeeds(1,1))

  val tankGameSettings2  = tankGameServer.create(
      " --tcp-port ${tcp[0]} --udp-port ${udp[0]} --master-host ${cs3k_host} --master-port ${cs3k_port} --game-setup ffa2", Cs3kConstants.LAUNCHER_PATH + gameIdentifier + "/"+ "start_game.jnlp",
      Map("gamePortUDP" -> "${udp[0]}", "gamePortTCP" -> "${tcp[0]}", "gameHost" -> Cs3kConfig.REMOTE_IP)
    )

  val tankGameSettings4  = tankGameServer.create(
    " --tcp-port ${tcp[0]} --udp-port ${udp[0]} --master-host ${cs3k_host} --master-port ${cs3k_port} --game-setup 2vs2", Cs3kConstants.LAUNCHER_PATH + gameIdentifier + "/"+ "start_game.jnlp",
    Map("gamePortUDP" -> "${udp[0]}", "gamePortTCP" -> "${tcp[0]}", "gameHost" -> Cs3kConfig.REMOTE_IP)
  )

  val tankGameSettings1vs1Team  = tankGameServer.create(
    " --tcp-port ${tcp[0]} --udp-port ${udp[0]} --master-host ${cs3k_host} --master-port ${cs3k_port} --game-setup 1vs1", Cs3kConstants.LAUNCHER_PATH + gameIdentifier + "/"+ "start_game.jnlp",
    Map("gamePortUDP" -> "${udp[0]}", "gamePortTCP" -> "${tcp[0]}", "gameHost" -> Cs3kConfig.REMOTE_IP)
  )

  val tankGameSettings1vs1ContinousFFA  = tankGameServer.create(
    " --tcp-port ${tcp[0]} --udp-port ${udp[0]} --master-host ${cs3k_host} --master-port ${cs3k_port} --game-setup ffa2cont", Cs3kConstants.LAUNCHER_PATH + gameIdentifier + "/"+ "start_game.jnlp",
    Map("gamePortUDP" -> "${udp[0]}", "gamePortTCP" -> "${tcp[0]}", "gameHost" -> Cs3kConfig.REMOTE_IP)
  )





  Init.gameMeta = Map() + ('TankGame -> (tankGameServer,(x:GameServerTypeId) => new GameType(x,"Tank Game")))

  Init.gameSetupMeta = Map() + (('TankGame, 'TG2Player) -> (tankGameSettings2,(x:GameProcessTemplateId) => new GameSetupType(x, "1vs1", null, null))) +
                          (('TankGame, 'TG1vs1Team) ->  (tankGameSettings1vs1Team, (x:GameProcessTemplateId) => new GameSetupType(x, "1vs1 team", null, null))) +
                          (('TankGame, 'TG1vs1FFAContinous) -> (tankGameSettings1vs1ContinousFFA,(x:GameProcessTemplateId) => new GameSetupType(x, "1vs1FFACont", null, null))) +
                          (('TankGame, 'TG4Player) -> (tankGameSettings4,(x:GameProcessTemplateId) => new GameSetupType(x, "2vs2", null, null)))

}
