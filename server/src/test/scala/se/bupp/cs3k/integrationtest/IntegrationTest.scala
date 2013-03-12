package se.bupp.cs3k.integrationtest

import org.specs2.mutable.Specification

import org.springframework.beans.factory.BeanFactory
import org.springframework.context.support.FileSystemXmlApplicationContext
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.support.{TransactionCallbackWithoutResult, TransactionTemplate}
import org.springframework.transaction.{PlatformTransactionManager, TransactionStatus}
import javax.persistence.{EntityManagerFactory, TypedQuery}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import se.bupp.cs3k.server.model._
import se.bupp.cs3k.server.service._
import dao.GameSetupTypeDao
import lobby.{AbstractLobbyQueueHandler, NonTeamLobbyQueueHandler}
import se.bupp.cs3k.server.{Init, Cs3kConfig}
import se.bupp.cs3k.LobbyJoinRequest
import com.esotericsoftware.kryonet.Connection

import org.specs2.mock.Mockito
import se.bupp.cs3k.server.service.gameserver._
import se.bupp.cs3k.api.GameServerFacade
import java.net.URL
import se.bupp.cs3k.api.user.{AnonymousPlayerIdentifierWithInfo, TeamIdentifier, RegisteredPlayerIdentifier}
import com.fasterxml.jackson.databind.ObjectMapper
import scala.Some
import se.bupp.cs3k.server.model.RunningGame
import se.bupp.cs3k.server.facade.GameServerFacadeImpl
import se.bupp.cs3k.server.model.NonPersisentGameOccassion
import scala.Some
import se.bupp.cs3k.server.model.RunningGame
import se.bupp.cs3k.server.model.RegedUser
import se.bupp.cs3k.server.model.AnonUser
import se.bupp.cs3k.server.model.Model._
import org.specs2.specification.Scope

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class IntegrationTest extends Specification with Mockito {


  sequential


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
    val emf = factory.getBean(classOf[EntityManagerFactory])
    var txMgr = factory.getBean("transactionManager", classOf[JpaTransactionManager])
    //val tdef = new DefaultTransactionDefinition();
    var tx = null//txMgr.getTransaction(tdef)

    val res = body(txMgr, factory)
    emf.close()
    appContext.close()
    appContext.destroy()
    res
  }
  var mapper = new ObjectMapper()


  trait NeedsGameSetup extends Scope {

    var gameAndSettingsId: GameAndRulesId = ('Asdf, 'QWer)

    var gpTemplate: GameProcessTemplate = new GameProcessTemplate("asdf", "asdf", null, new GameServerSpecification("asdf", null))
    GameServerRepository.reset
    GameServerRepository.add(gameAndSettingsId._1,null)
    GameServerRepository.addProcessTemplate(gameAndSettingsId, gpTemplate)

    val appContext = new FileSystemXmlApplicationContext("server/src/test/resources/applicationContext.xml");
    val factory =  appContext.asInstanceOf[BeanFactory];
    var setupDao = factory.getBean(classOf[GameSetupTypeDao])

    Init.persistAnyNewRules(factory)

    var gameSetupType = setupDao.findGameSetupType(gameAndSettingsId._1,gameAndSettingsId._2).get

  }

  "scheduled game setup" should {
    "handle 1vs1 " in new NeedsGameSetup {
      //GameServerPool.pool = new GameServerPool()
      var competitorService = factory.getBean("competitorService", classOf[CompetitorService])
      var gameReservationService = factory.getBean(classOf[GameReservationService])
      var gameServerFacade = factory.getBean(classOf[GameServerFacade])

      val user1 = competitorService.createUser("leffe")
      val user2 = competitorService.createUser("janne")

      val game = gameReservationService.challangeCompetitor(user1,user2,gameSetupType)



      game !== null
      game.participants.size === 2
      game.gameSessionIdOpt.isDefined === false
      //gameReservationService.findByGameSessionId(game.gameSessionId).isDefined === true


      val lobbyHandler = new NonTeamLobbyQueueHandler(2,gameAndSettingsId)

      Cs3kConfig.TEMP_FIX_FOR_STORING_GAME_TYPE = gameAndSettingsId
      Cs3kConfig.LOBBY_GAME_LAUNCH_ANNOUNCEMENT_DELAY = 1
      val connection1 = mock[Connection]
      connection1.getID() returns 11
      var connection2 = mock[Connection]
      connection2.getID() returns 12

      AbstractLobbyQueueHandler.gameReservationService = gameReservationService

      GameServerPool.pool = mock[GameServerPool]

      var gpSettings: GameProcessSettings = mock[GameProcessSettings]

      gpSettings.jnlpUrl(any,any) returns new URL("http://www.dn.se")
      //gpSettings.jnlpUrl(any) returns new URL("http://www.dn.se")


      GameServerPool.pool.spawnServer(any, any, any) answers {
        (params,mock) => new RunningGame(params.asInstanceOf[Array[_]](1).asInstanceOf[GameOccassion], null, null)
      }

      //GameServerPool.pool.spawnServer(any,any) returns new RunningGame(game,gpSettings)

      /*lobbyHandler.playerJoined(new LobbyJoinRequest(user1.id,user1.nameAccessor),connection1)
      lobbyHandler.playerJoined(new LobbyJoinRequest(user2.id,user2.nameAccessor),connection2)

      */
      game.gameSessionIdOpt.isDefined === false
      gameReservationService.playScheduledClosed(game.id,new RegedUser(user1.id))
      there was one(GameServerPool.pool).spawnServer(any,any,any)

      val game_v1 = gameReservationService.findGame(game.id).get
      game_v1.gameSessionIdOpt.isDefined === true

      GameServerPool.pool.findRunningGame(any) returns Some(RunningGame(game_v1, null, null))
      gameReservationService.playScheduledClosed(game.id,new RegedUser(user2.id))
      there was one(GameServerPool.pool).findRunningGame(any)

      //gameReservationService.startPersistedGameServer(game)

      try {
        Thread.sleep(3000)
      } catch { case e:InterruptedException => }

      val game_v2 = gameReservationService.findGame(game.id).get
      game_v2.hasStarted === true
      game_v2.result === null

      gameServerFacade.endGame(game_v2.gameSessionId,"""{"@class":"se.bupp.cs3k.example.ExampleScoreScheme$ExContestScore","s":{"1":{"a":0,"b":0},"2":{"a":2,"b":0}}}""")

      //game.result !== null
      val game_v3 = gameReservationService.findGame(game.id).get
      game_v3.result !== null


      appContext.close()
      1 === 1
    }

    "handle 2vs2 scheduled" in new NeedsGameSetup {
      //GameServerPool.pool = new GameServerPool()
      var competitorService = factory.getBean("competitorService", classOf[CompetitorService])
      var gameReservationService = factory.getBean(classOf[GameReservationService])
      var gameServerFacade = factory.getBean(classOf[GameServerFacade])
      var teamService = factory.getBean(classOf[TeamService])

      val user1 = competitorService.createUser("leffe")
      val user2 = competitorService.createUser("janne")

      val user3 = competitorService.createUser("klas")
      val user4 = competitorService.createUser("nisse")

      var team1 = new Team("ena")
      var team2 = new Team("andre")

      competitorService.createTeam(team1)
      competitorService.createTeam(team2)

      teamService.storeTeamMember(user1,team1)
      teamService.storeTeamMember(user2,team1)

      teamService.storeTeamMember(user3,team2)
      teamService.storeTeamMember(user4,team2)

      val game = gameReservationService.challangeCompetitor(team1,team2, gameSetupType)
      game !== null
      game.participants.size === 2
      game.gameSessionIdOpt.isDefined === false
      //gameReservationService.findByGameSessionId(game.gameSessionId).isDefined === true



      val lobbyHandler = new NonTeamLobbyQueueHandler(2,gameAndSettingsId)

      Cs3kConfig.TEMP_FIX_FOR_STORING_GAME_TYPE = gameAndSettingsId
      Cs3kConfig.LOBBY_GAME_LAUNCH_ANNOUNCEMENT_DELAY = 1
      val connection1 = mock[Connection]
      connection1.getID() returns 11
      var connection2 = mock[Connection]
      connection2.getID() returns 12

      AbstractLobbyQueueHandler.gameReservationService = gameReservationService

      GameServerPool.pool = mock[GameServerPool]

      var gpSettings: GameProcessSettings = mock[GameProcessSettings]

      //gpSettings.jnlpUrl(any,) returns new URL("http://www.dn.se")
      gpSettings.jnlpUrl(any,any) returns new URL("http://www.dn.se")


      GameServerPool.pool.spawnServer(any, any, any) answers {
        (p,b) => new RunningGame(p.asInstanceOf[Array[_]](1).asInstanceOf[GameOccassion], null, null)
      }

      //GameServerPool.pool.spawnServer(any,any) returns new RunningGame(game,gpSettings)

      /*lobbyHandler.playerJoined(new LobbyJoinRequest(user1.id,user1.nameAccessor),connection1)
      lobbyHandler.playerJoined(new LobbyJoinRequest(user2.id,user2.nameAccessor),connection2)

      */


      game.gameSessionIdOpt.isDefined === false
      var (g1,p1) = gameReservationService.playScheduledClosed(game.id,new RegedUser(user1.id))
      there was one(GameServerPool.pool).spawnServer(any,any,any)

      val game_v1 = gameReservationService.findGame(game.id).get
      game_v1.gameSessionIdOpt.isDefined === true

      GameServerPool.pool.findRunningGame(any) returns Some(RunningGame(game_v1, null, null))

      var (g2,p2) = gameReservationService.playScheduledClosed(game.id,new RegedUser(user2.id))
      there was one(GameServerPool.pool).findRunningGame(any)

      var (g3,p3) = gameReservationService.playScheduledClosed(game.id,new RegedUser(user3.id))
      there was two(GameServerPool.pool).findRunningGame(any)

      var (g4,p4) = gameReservationService.playScheduledClosed(game.id,new RegedUser(user4.id))
      there was three(GameServerPool.pool).findRunningGame(any)

      val spi1 = gameServerFacade.evaluateGamePass(mapper.writeValueAsString(p1),game_v1.gameSessionIdOpt.get)
      spi1.getName === "leffe"

      val spi4 = gameServerFacade.evaluateGamePass(mapper.writeValueAsString(p4),game_v1.gameSessionIdOpt.get)
      spi4.getName === "nisse"

      spi1.getTeam !== null
      spi1.getTeam.getReportableId === team1.id
      spi4.getTeam !== null
      spi4.getTeam.getReportableId === team2.id

      //gameReservationService.startPersistedGameServer(game)

      try {
        Thread.sleep(3000)
      } catch { case e:InterruptedException => }

      val game_v2 = gameReservationService.findGame(game.id).get
      game_v2.hasStarted === true
      game_v2.result === null

      gameServerFacade.endGame(game_v2.gameSessionId,"""{"@class":"se.bupp.cs3k.example.ExampleScoreScheme$ExContestScore","s":{"1":{"a":0,"b":0},"2":{"a":2,"b":0}}}""")

      //game.result !== null
      val game_v3 = gameReservationService.findGame(game.id).get
      game_v3.result !== null


      appContext.close()
      1 === 1
    }
  }

 "non scheduled closed setup" should {
    "handle 2vs2 g" in {
      val appContext = new FileSystemXmlApplicationContext("server/src/test/resources/applicationContext.xml");
      val factory =  appContext.asInstanceOf[BeanFactory];
      var competitorService = factory.getBean("competitorService", classOf[CompetitorService])
      var gameReservationService = factory.getBean(classOf[GameReservationService])
      var gameServerFacade = factory.getBean(classOf[GameServerFacade])
      var gameResultService = factory.getBean(classOf[ResultService])

      GameReservationService.openGameSessions = Map.empty

      // TODO - WRONG - Should be either "gameReservationService" or "service" (prolly gameReservationService)
      val service = new GameReservationService()

      val sessionId = service.allocateGameSession()
      val t1 = service.createVirtualTeam(Some("Ena"))
      service.addTeamToSession(sessionId,t1)
      var t2 = service.createVirtualTeam(Some("Tjing"))
      service.addTeamToSession(sessionId,t2)

      val user2 = competitorService.createUser("leffe")

      val seat1 = service.reserveSeat(sessionId, AnonUser("Nisse"), Some(t1))
      val seat2 = service.reserveSeat(sessionId, RegedUser(user2.id), Some(t1))

      val seat3 = service.reserveSeat(sessionId, AnonUser("Peter"), Some(t2))
      val seat4 = service.reserveSeat(sessionId, AnonUser("Fredrik"), Some(t2))

      GameServerPool.pool = mock[GameServerPool]

      // TODO : Spawn swerver
      val occassion = NonPersisentGameOccassion(sessionId)

      GameServerPool.pool.findRunningGame(any) returns Some(RunningGame(occassion, null, null))
      val (_,gp1) = gameReservationService.playNonScheduledClosed(seat1)
      there was one(GameServerPool.pool).findRunningGame(any)
      val (_,gp2) = gameReservationService.playNonScheduledClosed(seat2)
      there was two(GameServerPool.pool).findRunningGame(any)
      val (_,gp3) = gameReservationService.playNonScheduledClosed(seat3)
      there was three(GameServerPool.pool).findRunningGame(any)
      val (_,gp4) = gameReservationService.playNonScheduledClosed(seat4)

      val p1 = gameServerFacade.evaluateGamePass(mapper.writeValueAsString(gp1), occassion.gameSessionId)
      val p2 = gameServerFacade.evaluateGamePass(mapper.writeValueAsString(gp2), occassion.gameSessionId)
      val p3 = gameServerFacade.evaluateGamePass(mapper.writeValueAsString(gp3), occassion.gameSessionId)
      val p4 = gameServerFacade.evaluateGamePass(mapper.writeValueAsString(gp4), occassion.gameSessionId)


      gameResultService.gameLog = mock[ResultLogService]
      //val gameResultService = mock[GameResultService]


      gameServerFacade.endGame(sessionId,"""{"@class":"se.bupp.cs3k.example.ExampleScoreScheme$ExContestScore","s":{"1000":{"a":10,"b":30},"1001":{"a":2,"b":40}}}""")
      there was one(gameResultService.gameLog).write("<tr><th></th><th>Kills</th><th>Diff Kills</th><th>Trophys</th></tr><tr><td>Tjing</td><td>2</td><td>0</td><td>40</td></tr><tr><td>Ena</td><td>10</td><td>0</td><td>30</td></tr>")



      //gameResultService.transformToRenderable(sessionId)

      appContext.close()
      1 === 1
    }
  }
}
