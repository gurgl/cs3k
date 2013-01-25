package se.bupp.cs3k.integrationtest

import org.specs2.mutable.Specification

import org.springframework.beans.factory.BeanFactory
import org.springframework.context.support.FileSystemXmlApplicationContext
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.support.{TransactionCallbackWithoutResult, TransactionTemplate}
import org.springframework.transaction.{PlatformTransactionManager, TransactionStatus}
import javax.persistence.TypedQuery
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import se.bupp.cs3k.server.model.{Team, RunningGame, User}
import se.bupp.cs3k.server.service.{TeamService, GameReservationService, CompetitorService}
import se.bupp.cs3k.server.{Cs3kConfig, LobbyHandler}
import se.bupp.cs3k.LobbyJoinRequest
import com.esotericsoftware.kryonet.Connection

import org.specs2.mock.Mockito
import se.bupp.cs3k.server.service.gameserver._
import se.bupp.cs3k.api.GameServerFacade
import java.net.URL

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class IntegrationTest extends Specification with Mockito {




  def inTx[G](txMgr:PlatformTransactionManager)(body : => G) = {
    new TransactionTemplate(txMgr).execute(new TransactionCallbackWithoutResult {
      def doInTransactionWithoutResult(p1: TransactionStatus) {
        body
      }
      })
  }

  def withTx[T](body:(PlatformTransactionManager, BeanFactory) => T) : T = {
    val appContext = new FileSystemXmlApplicationContext("server/src/test/resources/applicationContext.xml");
    val factory =  appContext.asInstanceOf[BeanFactory];
    var txMgr = factory.getBean("transactionManager", classOf[JpaTransactionManager])
    //val tdef = new DefaultTransactionDefinition();
    var tx = null//txMgr.getTransaction(tdef)

    val res = body(txMgr, factory)
    appContext.close()
    res
  }

  "game setup" should {
    "handle 1vs1 " in {
      //GameServerPool.pool = new GameServerPool()
      val appContext = new FileSystemXmlApplicationContext("server/src/test/resources/applicationContext.xml");
      val factory =  appContext.asInstanceOf[BeanFactory];
      var competitorService = factory.getBean("competitorService", classOf[CompetitorService])
      var gameReservationService = factory.getBean(classOf[GameReservationService])
      var gameServerFacade = factory.getBean(classOf[GameServerFacade])

      val user1 = competitorService.createUser("leffe")
      val user2 = competitorService.createUser("janne")

      val game = gameReservationService.challangeCompetitor(user1,user2)



      game.gameSessionId === 100
      game !== null
      game.participants.size === 2
      gameReservationService.findGame(game.gameSessionId).isDefined === true

      var gameAndSettingsId: GameServerRepository.GameAndRulesId = ('Asdf, 'QWer)

      var gpTemplate: GameProcessTemplate = new GameProcessTemplate("asdf", "asdf", null, new GameServerSpecification("asdf", null))
      GameServerRepository.addProcessTemplate(gameAndSettingsId, gpTemplate)
      val lobbyHandler = new LobbyHandler(2,gameAndSettingsId)

      Cs3kConfig.TEMP_FIX_FOR_STORING_GAME_TYPE = gameAndSettingsId
      Cs3kConfig.LOBBY_GAME_LAUNCH_ANNOUNCEMENT_DELAY = 1
      val connection1 = mock[Connection]
      connection1.getID() returns 11
      var connection2 = mock[Connection]
      connection2.getID() returns 12
      game.gameSessionId === 100

      LobbyHandler.gameReservationService = gameReservationService

      GameServerPool.pool = mock[GameServerPool]

      var gpSettings: GameProcessSettings = mock[GameProcessSettings]

      gpSettings.jnlpUrl(any,anyInt) returns new URL("http://www.dn.se")
      gpSettings.jnlpUrl(any,anyString) returns new URL("http://www.dn.se")

      GameServerPool.pool.spawnServer(any,any) returns new RunningGame(game,gpSettings)
      game.gameSessionId === 100
      /*lobbyHandler.playerJoined(new LobbyJoinRequest(user1.id,user1.nameAccessor),connection1)
      lobbyHandler.playerJoined(new LobbyJoinRequest(user2.id,user2.nameAccessor),connection2)

      */

      gameReservationService.startPersistedGameServer(game)
      game.gameSessionId === 100
      try {
        Thread.sleep(3000)
      } catch { case e:InterruptedException => }

      game.hasStarted === true
      game.result === null
      game.gameSessionId === 100
      gameServerFacade.endGame(game.gameSessionId,"""{"@class":"se.bupp.cs3k.example.ExampleScoreScheme$ExContestScore","s":{"1":{"a":0,"b":0},"2":{"a":2,"b":0}}}""")

      //game.result !== null
      val gg = gameReservationService.findGame(game.gameSessionId).get
      gg.result !== null


      appContext.close()
      1 === 1
    }

    "bubba" in {
      //GameServerPool.pool = new GameServerPool()
      val appContext = new FileSystemXmlApplicationContext("server/src/test/resources/applicationContext.xml");
      val factory =  appContext.asInstanceOf[BeanFactory];
      var competitorService = factory.getBean("competitorService", classOf[CompetitorService])
      var gameReservationService = factory.getBean(classOf[GameReservationService])
      var gameServerFacade = factory.getBean(classOf[GameServerFacade])
      var teamService = factory.getBean(classOf[TeamService])

      val user1 = competitorService.createUser("leffe")
      val user2 = competitorService.createUser("janne")

      val user3 = competitorService.createUser("klas")
      val user4 = competitorService.createUser("nisse")

      var team1 = new Team()
      var team2 = new Team()

      competitorService.createTeam(team1)
      competitorService.createTeam(team2)

      teamService.storeTeamMember(user1,team1)
      teamService.storeTeamMember(user2,team1)

      teamService.storeTeamMember(user3,team2)
      teamService.storeTeamMember(user4,team2)

      val game = gameReservationService.challangeCompetitor(team1,team2)

      game.gameSessionId === 100
      game !== null
      game.participants.size === 2
      gameReservationService.findGame(game.gameSessionId).isDefined === true

      var gameAndSettingsId: GameServerRepository.GameAndRulesId = ('Asdf, 'QWer)

      var gpTemplate: GameProcessTemplate = new GameProcessTemplate("asdf", "asdf", null, new GameServerSpecification("asdf", null))
      GameServerRepository.addProcessTemplate(gameAndSettingsId, gpTemplate)
      val lobbyHandler = new LobbyHandler(2,gameAndSettingsId)

      Cs3kConfig.TEMP_FIX_FOR_STORING_GAME_TYPE = gameAndSettingsId
      Cs3kConfig.LOBBY_GAME_LAUNCH_ANNOUNCEMENT_DELAY = 1
      val connection1 = mock[Connection]
      connection1.getID() returns 11
      var connection2 = mock[Connection]
      connection2.getID() returns 12
      game.gameSessionId === 100

      LobbyHandler.gameReservationService = gameReservationService

      GameServerPool.pool = mock[GameServerPool]

      var gpSettings: GameProcessSettings = mock[GameProcessSettings]

      gpSettings.jnlpUrl(any,anyInt) returns new URL("http://www.dn.se")
      gpSettings.jnlpUrl(any,anyString) returns new URL("http://www.dn.se")

      GameServerPool.pool.spawnServer(any,any) returns new RunningGame(game,gpSettings)
      game.gameSessionId === 100
      /*lobbyHandler.playerJoined(new LobbyJoinRequest(user1.id,user1.nameAccessor),connection1)
      lobbyHandler.playerJoined(new LobbyJoinRequest(user2.id,user2.nameAccessor),connection2)

      */

      gameReservationService.startPersistedGameServer(game)
      game.gameSessionId === 100
      try {
        Thread.sleep(3000)
      } catch { case e:InterruptedException => }

      game.hasStarted === true
      game.result === null
      game.gameSessionId === 100
      gameServerFacade.endGame(game.gameSessionId,"""{"@class":"se.bupp.cs3k.example.ExampleScoreScheme$ExContestScore","s":{"1":{"a":0,"b":0},"2":{"a":2,"b":0}}}""")

      //game.result !== null
      val gg = gameReservationService.findGame(game.gameSessionId).get
      gg.result !== null


      appContext.close()
      1 === 1
    }

  }
}
