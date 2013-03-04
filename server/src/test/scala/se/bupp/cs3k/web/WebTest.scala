package se.bupp.cs3k.web

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.apache.wicket.util.tester.WicketTester
import se.bupp.cs3k.server.web.component.{TournamentNodeView, TournamentViewNotStarted, LadderFormPanel}
import se.bupp.cs3k.server.service.dao.GameSetupTypeDao
import org.specs2.mock.Mockito
import se.bupp.cs3k.server.model.GameSetupType
import org.apache.wicket.model.Model
import se.bupp.cs3k.server.web.component.TournamentNodeView.TwoGameQualifierPositionAndSize

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
      wt.destroy()
    }

    "do stuff" in {


      TournamentNodeView.createLayout(14) must haveTheSameElementsAs(List(TwoGameQualifierPositionAndSize("None","None",1234,0.0f,37.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("None","None",1234,0.0f,107.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","Some(1)",1234,100.0f,55.0f,100.0f,70.0f), TwoGameQualifierPositionAndSize("None","None",1234,0.0f,177.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("None","None",1234,0.0f,247.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","Some(1)",1234,100.0f,195.0f,100.0f,70.0f), TwoGameQualifierPositionAndSize("Some(2)","Some(2)",1234,200.0f,90.0f,100.0f,140.0f), TwoGameQualifierPositionAndSize("None","None",1234,0.0f,317.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("None","None",1234,0.0f,387.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","Some(1)",1234,100.0f,335.0f,100.0f,70.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,457.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(2)","Some(1)",1234,200.0f,370.0f,100.0f,105.0f), TwoGameQualifierPositionAndSize("Some(4)","Some(4)",1234,300.0f,160.0f,100.0f,280.0f)))

      TournamentNodeView.createLayout(33) must haveTheSameElementsAs(List(TwoGameQualifierPositionAndSize("None","None",1234,0.0f,37.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","None",1234,100.0f,55.0f,100.0f,52.5f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,177.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(2)","Some(1)",1234,200.0f,90.0f,100.0f,105.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,317.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,387.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","Some(1)",1234,200.0f,335.0f,100.0f,70.0f), TwoGameQualifierPositionAndSize("Some(4)","Some(2)",1234,300.0f,160.0f,100.0f,210.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,597.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,667.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","Some(1)",1234,200.0f,615.0f,100.0f,70.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,737.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,807.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","Some(1)",1234,200.0f,755.0f,100.0f,70.0f), TwoGameQualifierPositionAndSize("Some(2)","Some(2)",1234,300.0f,650.0f,100.0f,140.0f), TwoGameQualifierPositionAndSize("Some(8)","Some(4)",1234,400.0f,300.0f,100.0f,420.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,1157.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,1227.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","Some(1)",1234,200.0f,1175.0f,100.0f,70.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,1297.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,1367.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","Some(1)",1234,200.0f,1315.0f,100.0f,70.0f), TwoGameQualifierPositionAndSize("Some(2)","Some(2)",1234,300.0f,1210.0f,100.0f,140.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,1437.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,1507.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","Some(1)",1234,200.0f,1455.0f,100.0f,70.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,1577.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("None","None",1234,100.0f,1647.5f,100.0f,35.0f), TwoGameQualifierPositionAndSize("Some(1)","Some(1)",1234,200.0f,1595.0f,100.0f,70.0f), TwoGameQualifierPositionAndSize("Some(2)","Some(2)",1234,300.0f,1490.0f,100.0f,140.0f), TwoGameQualifierPositionAndSize("Some(4)","Some(4)",1234,400.0f,1280.0f,100.0f,280.0f), TwoGameQualifierPositionAndSize("Some(16)","Some(8)",1234,500.0f,580.0f,100.0f,840.0f)))

    }
  }
}
