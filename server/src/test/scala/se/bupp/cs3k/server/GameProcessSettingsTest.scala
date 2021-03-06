package se.bupp.cs3k.server

import org.specs2.mutable.Specification
import collection.SortedSet
import service.gameserver.GameProcessTemplate
import service.resourceallocation.AllocatedResourceSet
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-20
 * Time: 20:00
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class GameProcessSettingsTest extends Specification {

  "Settings Template" should {
    "replace arguments and insert given resources" in {
      val lal = GameProcessTemplate.applyInstanceCommandLinePlaceholders("asdf ${tcp[0]} qwer ${udp[1]} + ${udp[0]} ${cs3k_host} ${cs3k_port}", new AllocatedResourceSet(Set(2), Set(3,4)))

      lal === "asdf 2 qwer 4 + 3 localhost 1199"
    }

    "replace props and insert given resources" in {
      val lal = GameProcessTemplate.replacePropsPlaceholders(Map("Blaja" -> "${tcp[0]}", "Lal" -> "${udp[1]}"), new AllocatedResourceSet(Set(2), Set(3,4)))

      lal must haveTheSameElementsAs(Map("Blaja" -> "2", "Lal" -> "4"))
    }
  }




}
