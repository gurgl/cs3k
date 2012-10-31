package se.bupp.cs3k

import org.specs2.mutable.Specification
import com.fasterxml.jackson.databind.ObjectMapper
import se.bupp.cs3k.server.service.GameReservationService._

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
class MiscTest extends Specification {

  "scala tests" should {
    "immutable maps" in {

      val m = Map[Int,String](1->"a",2->"b")


      (m + (1 -> "c")).shouldEqual(Map(1->"c",2->"b"))
    }
    "handle conversions" in {

      1.shouldEqual(1)
    }
  }
}
