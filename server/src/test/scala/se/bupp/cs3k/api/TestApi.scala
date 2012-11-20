package se.bupp.cs3k.api

import org.specs2.mutable.Specification
import io.Source
import java.net.URL
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.util.StringUtils
import score.ContestScore
import user.{AnonymousPlayerIdentifier, PlayerIdentifierWithInfo}
import xml.Utility
import se.bupp.cs3k.server.service.GameReservationService._
import se.bupp.cs3k.server.web.component.Example.{JavaTuple2, MyContestScore}
import se.bupp.cs3k.server.web.component.Example

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

    "handle score" in {

      val sc = Example.MyScoreScheme
      var mapper: ObjectMapper = new ObjectMapper()

      import scala.collection.JavaConversions.mapAsJavaMap
      var original = new MyContestScore(Map(1L->new  JavaTuple2(10,1), 2L-> new JavaTuple2(3,4)))
      var str: String = mapper.writeValueAsString(original)
      str.shouldEqual("{\"s\":{\"1\":{\"@class\":\"se.bupp.cs3k.server.web.component.Example$JavaTuple2\",\"_1\":null,\"_2\":null,\"_1$mcI$sp\":10,\"_2$mcI$sp\":1},\"2\":{\"@class\":\"se.bupp.cs3k.server.web.component.Example$JavaTuple2\",\"_1\":null,\"_2\":null,\"_1$mcI$sp\":3,\"_2$mcI$sp\":4}}}")

      val r = mapper.readValue(str, sc.getContestStoreClass)
      //original.s.asInstanceOf[java.util.Map[Long,JavaTuple2]].shouldEqual(r.s.asInstanceOf[java.util.Map[Long,JavaTuple2]])
      import scala.collection.JavaConversions.mapAsScalaMap
      val a = Map.empty ++ collection.mutable.Map(r.s.toSeq:_*)
      val b = Map.empty ++ collection.mutable.Map( original.s.toSeq:_*)

      mapAsScalaMap(r.s) must haveTheSameElementsAs(mapAsScalaMap(original.s), (a:Any,b:Any) => a.toString == b.toString)
      //a === b

              1 mustEqual 1
    }
  }
}
