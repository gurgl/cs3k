package se.bupp.cs3k.server

import org.specs2.mutable.Specification

import org.springframework.beans.factory.BeanFactory
import org.springframework.context.support.FileSystemXmlApplicationContext
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.support.{TransactionCallbackWithoutResult, TransactionTemplate}
import org.springframework.transaction.{PlatformTransactionManager, TransactionStatus}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import model.{GameOccassion, Team, RunningGame}
import se.bupp.cs3k.server.service.{TeamService, GameReservationService, CompetitorService}
import com.esotericsoftware.kryonet.Connection

import org.specs2.mock.Mockito
import se.bupp.cs3k.server.service.gameserver._
import se.bupp.cs3k.api.GameServerFacade
import java.net.URL
import se.bupp.cs3k.api.user.RegisteredPlayerIdentifier

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class ModelTest extends Specification with Mockito {


  "Model " should {
    "behave as intended" in {


      var occassion: GameOccassion = new GameOccassion()
      occassion.gameSessionIdOpt = Some(123)

      occassion.gameSessionIdOpt.isDefined === true
      occassion.gameSessionIdOpt.get === 123
    }

  }
}
