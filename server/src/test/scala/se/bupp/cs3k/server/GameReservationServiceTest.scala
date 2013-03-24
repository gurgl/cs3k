package se.bupp.cs3k.server

import model.AnonUser
import org.specs2.mutable.Specification
import com.fasterxml.jackson.databind.ObjectMapper
import service.{GameReservationServiceStore, GameReservationService}


import se.bupp.cs3k.api.user.AnonymousPlayerIdentifier
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.junit.Ignore
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
class GameReservationServiceTest extends Specification {

  sequential

  trait Setup extends Scope {
    Init.gameReservationServiceStore = new GameReservationServiceStore
  }

  "should handle reservations " should {
    "handle store" in new Setup {

      val service = new GameReservationService

      var occassion: GameSessionId = service.allocateGameSession()
      var p1 = new AnonUser("Tja")
      var seat1 = service.reserveSeat(occassion, p1, None)
      var p2 = new AnonUser("Tja2")
      var seat2 = service.reserveSeat(occassion, p2, None)
      service.findGameSessionByReservationId(seat1) shouldEqual(Some((100,(collection.mutable.Map(seat1 -> (p1,None), seat2 -> (p2,None)),None))))

    }


    "provide" in new Setup  {

      val service = new GameReservationService
      var occassion: GameSessionId = service.allocateGameSession()
      var p1 = new AnonUser("Tja")
      var seat1 = service.reserveSeat(occassion, p1, None)
      var p2 = new AnonUser("Tja2")
      var seat2 = service.reserveSeat(occassion, p2, None)
      service.findGameSessionByReservationId(seat1) shouldEqual(Some((100,(collection.mutable.Map(seat1 -> (p1,None), seat2 -> (p2,None)),None))))
    }

  }
}
