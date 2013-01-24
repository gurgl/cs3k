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
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.server.service.{GameReservationService, CompetitorService}
import se.bupp.cs3k.server.LobbyHandler
import se.bupp.cs3k.LobbyJoinRequest
import com.esotericsoftware.kryonet.Connection

import org.specs2.mock.Mockito
import se.bupp.cs3k.server.service.gameserver.{GameServerSpecification, GameProcessTemplate, GameServerRepository, GameServerPool}

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

      val user1 = competitorService.createUser("leffe")
      val user2 = competitorService.createUser("janne")

      val game = gameReservationService.challangeCompetitor(user1,user2)
      game !== null
      var gameAndSettingsId: GameServerRepository.GameAndRulesId = ('Asdf, 'QWer)
      val lobbyHandler = new LobbyHandler(2,gameAndSettingsId)

      val connection1 = mock[Connection]
      connection1.getID() returns 11
      var connection2 = mock[Connection]
      connection2.getID() returns 12


      LobbyHandler.gameReservationService = gameReservationService

      GameServerPool.pool = mock[GameServerPool]

      GameServerRepository.addProcessTemplate(gameAndSettingsId, new GameProcessTemplate("asdf","asdf",null,new GameServerSpecification("asdf", null)))

      lobbyHandler.playerJoined(new LobbyJoinRequest(user1.id,user1.nameAccessor),connection1)
      lobbyHandler.playerJoined(new LobbyJoinRequest(user2.id,user2.nameAccessor),connection2)


      game.hasStarted == true



      appContext.close()
      1 === 1
    }

    "handle game occassion transistion to game result" in {

    }
  }
}
