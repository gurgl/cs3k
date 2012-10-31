package se.bupp.cs3k.api

import org.specs2.mutable.Specification
import io.Source
import java.net.URL
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.util.StringUtils
import user.{AnonymousPlayerIdentifier, PlayerIdentifierWithInfo}
import xml.Utility
import se.bupp.cs3k.server.service.GameReservationService._

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
class TestApi extends Specification {

  "should return my address" should {
    "handle conversions1" in {
      def llll : java.lang.Long = if (1 == 1) null else 1L
      val reservationIdOpt:Option[SeatId] = Option(llll).map(p=> p.asInstanceOf[Long])
      reservationIdOpt.shouldEqual(None)
    }
    "handle conversions" in {

      var mapper: ObjectMapper = new ObjectMapper()

      /*var original: Ticket = new Ticket {
        def getId = 3
      }
      var str: String = mapper.writeValueAsString(original)
      str.shouldEqual("{\"@class\":\"se.bupp.cs3k.api.Ticket\",\"reservationId\":3}")
      */

      //var back: AbstractGamePass = mapper.readValue(str, classOf[AbstractGamePass])
      //back.shouldEqual(original)
      var original2 = new IdentifyOnlyPass(new AnonymousPlayerIdentifier("lennart"))

      var str2: String = mapper.writeValueAsString(original2)
      str2.shouldEqual("{\"@class\":\"se.bupp.cs3k.api.IdentifyOnlyPass\",\"userIdentifier\":{\"@class\":\"se.bupp.cs3k.api.user.AnonymousPlayerIdentifier\",\"name\":\"lennart\"}}")

    }
  }
}
