package se.bupp.cs3k.server

import org.specs2.mutable.Specification
import com.fasterxml.jackson.databind.ObjectMapper
import service.GameReservationService

import se.bupp.cs3k.server.service.GameReservationService._
import se.bupp.cs3k.api.user.AnonymousPlayerIdentifier
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class GameReservationServiceTest extends Specification {

  "should handle reservations " should {
    "handle store" in {

      val service = new GameReservationService

      var occassion: GameReservationService.GameSessionId = service.allocateGameSession()
      var p1: AnonymousPlayerIdentifier = new AnonymousPlayerIdentifier("Tja")
      var seat1 = service.reserveSeat(occassion, p1)
      var p2: AnonymousPlayerIdentifier = new AnonymousPlayerIdentifier("Tja2")
      var seat2 = service.reserveSeat(occassion, p2)
      service.findInMemoryReservation(seat1) shouldEqual(Some(1,collection.mutable.Map(seat1 -> p1, seat2 -> p2)))

    }

  }
}
