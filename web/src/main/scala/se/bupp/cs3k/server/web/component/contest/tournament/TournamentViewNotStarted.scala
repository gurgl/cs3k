package se.bupp.cs3k.server.web.component.contest.tournament

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.IModel

import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.{TournamentHelper, CompetitionService}
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.service.TournamentHelper.TwoGameQualifierPositionAndSize
import se.bupp.cs3k.server.web.component.contest.tournament.TournamentNodeView

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-12
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */
class TournamentViewNotStarted(id:String, mod:IModel[Integer]) extends Panel(id) {

  @SpringBean
  var ladderService:CompetitionService = _

  var buffa:ListModel[TwoGameQualifierPositionAndSize] = new ListModel[TwoGameQualifierPositionAndSize]()

  override def onBeforeRender() {
    val listn: List[TwoGameQualifierPositionAndSize] = TournamentHelper.createLayout(mod.getObject)
    import scala.collection.JavaConversions.seqAsJavaList


    buffa.setObject(listn)
    super.onBeforeRender()
  }




  var view = new ListView[TwoGameQualifierPositionAndSize]("it", buffa) {
    def populateItem(p1: ListItem[TwoGameQualifierPositionAndSize]) {
      println("Rend " + p1.getIndex)
      p1.add(new TournamentNodeView("item", p1.getModel))

    }
  }
  add(view)
}

