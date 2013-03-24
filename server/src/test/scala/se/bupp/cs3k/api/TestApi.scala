package se.bupp.cs3k.api

import org.specs2.mutable.Specification
import io.Source
import java.net.URL
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.util.StringUtils
import score.ContestScore
import user.{TeamIdentifier, AnonymousPlayerIdentifierWithInfo, AnonymousPlayerIdentifier, RegisteredPlayerIdentifierWithInfo}
import xml.Utility

import se.bupp.cs3k.example.ExampleScoreScheme._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import se.bupp.cs3k.server.service.GameReservationService
import se.bupp.cs3k.server.model.AnonUser
import se.bupp.cs3k.server.model.Model._
/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class TestApi extends Specification {

  "should return my address" should {
    "handle conversions1" in {
      def llll : java.lang.Long = if (1 == 1) null else 1L
      val reservationIdOpt:Option[GameServerReservationId] = Option(llll).map(p=> p.asInstanceOf[Long])
      reservationIdOpt.shouldEqual(None)
    }

    "handle conversions" in {

      var mapper: ObjectMapper = new ObjectMapper()

      /*var original: Ticket = new Ticket {
        def getReportableId = 3
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

  "score protocol" should {
    var mapper: ObjectMapper = new ObjectMapper()
    val sc = ExScoreScheme
    import scala.collection.JavaConversions.mapAsJavaMap
    import scala.collection.JavaConversions.mapAsScalaMap

    import scala.collection.JavaConversions.seqAsJavaList

    "serialize contest score" in {

      var original = new ExContestScore(collection.immutable.Map(1L->new JavaTuple2(10,1), 2L-> new JavaTuple2(3,4)))
      var str: String = mapper.writeValueAsString(original)
      str.shouldEqual("{\"@class\":\"se.bupp.cs3k.example.ExampleScoreScheme$ExContestScore\",\"s\":{\"1\":{\"a\":10,\"b\":1},\"2\":{\"a\":3,\"b\":4}}}")

      val r = mapper.readValue(str, sc.getContestStoreClass)


      /*u.put(1L, new ExCompetitorScore(10,0,1))
      u.put(2L, new ExCompetitorScore(3,0,4))*/
      r.competitorScores()
      //original.s.asInstanceOf[java.util.Map[Long,JavaTuple2]].shouldEqual(r.s.asInstanceOf[java.util.Map[Long,JavaTuple2]])
      //val a = Map.empty ++ collection.mutable.Map(r.s.toSeq:_*)
      //val b = Map.empty ++ collection.mutable.Map( original.s.toSeq:_*)

      /*
      val jum:java.util.Map[Long,JavaTuple2] = new java.util.HashMap[Long,JavaTuple2]()
      jum.put(1L, new JavaTuple2(10,1))
      jum.put(2L, new JavaTuple2(3,4))
      val jumJson = mapper.writeValueAsString(jum)
      val des:java.util.Map[Long,JavaTuple2] = mapper.readValue(jumJson,classOf[java.util.HashMap[Long,JavaTuple2]])

      mapAsScalaMap(jum) must haveTheSameElementsAs(des)

      */

      var org = collection.mutable.Map(1L->new  JavaTuple2(10,1), 2L-> new JavaTuple2(3,4))
      var jul: java.util.Map[Long,JavaTuple2] = org

      mapAsScalaMap(jul) ==== org


      mapAsScalaMap(r.s) must haveTheSameElementsAs(mapAsScalaMap(original.s), (a:AnyRef,b:AnyRef) => {
        val c = a == b
        (a,b) match {
          case (a:Tuple2[_,_], b:Tuple2[_,_]) =>
            println("a.canEqual(b) " + a.canEqual(b))
            println("a._1 == b._1 " + (a._1 == b._1))
          println("a._2 == b._2 " + (a._2 == b._2))
          println("a._1 == b._1 " + a._1.getClass +" " + b._1.getClass)
          println("a._1 == b._1 " + a._2.getClass +" " + b._2.getClass)
          //println("a._2 == b._2" + a._2 == b._2)
          case _ =>
        }
            if(!c) println(a + " " + a.getClass.getSimpleName + " != " + b + " " + b.getClass.getSimpleName)
            a.toString == b.toString


      })

      Tuple2(1L,23) === Tuple2(1,23)
      //a === b

              1 mustEqual 1
    }

    "handle deriving correct competitor score per contest" in {
      var game1 = new ExContestScore(collection.mutable.Map(1L->new JavaTuple2(10,1), 2L-> new JavaTuple2(3,4)))
      game1.competitorScore(1L) === new ExCompetitorScore(10, 7, 1)
      game1.competitorScore(2L) === new ExCompetitorScore(3, -7, 4)
    }

    "handle contest score addition to total" in {
      var game1 = new ExContestScore(collection.mutable.Map(1L->new JavaTuple2(10,1), 2L-> new JavaTuple2(3,4)))

      var game2 = new ExContestScore(collection.mutable.Map(1L->new JavaTuple2(2,3), 2L-> new JavaTuple2(3,5)))

      val scores = List(game1.competitorScore(1L), game2.competitorScore(1))

      val total = sc.calculateTotal(scores)

      total === new ExCompetitorTotal(12,6,4)
    }


    "ex impl" in {
      //val scoreByCompetitors = Map(0 -> (1,3), 1 -> (3,3))

      var result = new ExContestScore(collection.mutable.Map(1L->new JavaTuple2(10,1), 2L-> new JavaTuple2(3,4)))

      //ExScoreScheme.renderToHtml()


      1 == 1


    }

  }
}
