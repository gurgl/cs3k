package se.bupp.cs3k.server.web.component.contest.tournament

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.{Model, IModel}

import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.{TournamentHelper, CompetitionService}
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.model.Tournament
import se.bupp.cs3k.server.service.TournamentHelper.TwoGameQualifierPositionAndSize

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-12
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */
class TournamentView(id:String, mod:IModel[Tournament]) extends Panel(id) {

  @SpringBean
  var ladderService:CompetitionService = _

  var buffa:ListModel[TwoGameQualifierPositionAndSize] = new ListModel[TwoGameQualifierPositionAndSize]()

  override def onBeforeRender() {
    val listn: List[TwoGameQualifierPositionAndSize] = ladderService.createLayout2(mod.getObject, 20)
    import scala.collection.JavaConversions.seqAsJavaList

    listn.find(_.winnerPosOpt.isDefined).flatMap(_.winnerPosOpt) match {
      case Some((left,top,nameOpt)) =>
        add(new SvgPath("winnerPath",new Model(s"m ${left},${top} 100.0,0"),new Model("svg-tour-path-css undetermined")))
        add(new SvgLabel("winnerLbl",new Model(left),new Model(top-TournamentHelper.lineToTextMargin),new Model(""),new Model(Some(nameOpt.getOrElse("Winner!")))))
      case None =>
    }


    buffa.setObject(listn)
    super.onBeforeRender()
  }




  var view = new ListView[TwoGameQualifierPositionAndSize]("it", buffa) {
    def populateItem(p1: ListItem[TwoGameQualifierPositionAndSize ]) {
      println("Rend " + p1.getIndex)
      p1.add(new TournamentNodeView("item", p1.getModel))

    }
  }
  add(view)
}

