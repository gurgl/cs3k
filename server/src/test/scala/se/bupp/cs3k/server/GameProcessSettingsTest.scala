package se.bupp.cs3k.server

import org.specs2.mutable.Specification
import collection.SortedSet

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-20
 * Time: 20:00
 * To change this template use File | Settings | File Templates.
 */
class GameProcessSettingsTest extends Specification {

  "Settings Template" should {
    "replace arguments and insert given resources" in {
      val lal = GameProcessTemplate.applyInstanceSpecificResources("asdf ${tcp[0]} qwer ${udp[1]} + ${udp[0]}", new ResourceSet(Set(2), Set(3,4)))

      lal === "asdf 2 qwer 4 + 3"
    }
  }


}