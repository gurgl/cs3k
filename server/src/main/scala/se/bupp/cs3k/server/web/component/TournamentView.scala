package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import java.io.PrintWriter
import org.apache.wicket.markup.MarkupStream
import org.apache.wicket.model.{IModel, Model}
import se.bupp.cs3k.server.web.component.TournamentNodeView.TwoGameQualifierPositionAndSize
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.{TournamentHelper, CompetitionService}
import se.bupp.cs3k.server.model.QualifierWithParentReference
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.model.util.ListModel

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-12
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */
class TournamentView(id:String, mod:IModel[Integer]) extends Panel(id) {

  @SpringBean
  var ladderService:CompetitionService = _

  var buffa:ListModel[TwoGameQualifierPositionAndSize] = new ListModel[TwoGameQualifierPositionAndSize]()

  override def onBeforeRender() {
    var listn: List[TournamentNodeView.TwoGameQualifierPositionAndSize] = TournamentNodeView.createLayout(mod.getObject)
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

