package se.bupp.cs3k.web

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.apache.wicket.util.tester.WicketTester
import se.bupp.cs3k.server.web.component.LadderFormPanel
import se.bupp.cs3k.server.service.dao.GameSetupTypeDao
import org.specs2.mock.Mockito
import se.bupp.cs3k.server.model.GameSetupType

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class WebTest extends Specification with Mockito {

  "scala tests" should {
    "immutable maps" in {

      val wt = new WicketTester()
      wt.getApplication.getMarkupSettings.setStripWicketTags(true)

      val gameSetupDao = mock[GameSetupTypeDao]
      //import scala.collection.JavaConversions.seqAsJavaList
      gameSetupDao.findAll returns List(new GameSetupType('bla,"tjo","",""),new GameSetupType('bla2,"tjo2","",""))

      var panel = new LadderFormPanel("asdf", "Blha", null)

      panel.gameSetupDao = gameSetupDao
      wt.startComponentInPage(panel)
      wt.getLastResponseAsString
      1.shouldEqual(1)
    }

    "handle conversions" in {

      1.shouldEqual(1)
    }
  }
}
