package se.bupp.cs3k.server.web.component

import generic.table.NiceDataTable
import generic.{FodelPropertyColumn, AjaxLinkLabel, VertTabbedPanel}
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.{PropertyModel, Model, LoadableDetachableModel, IModel}
import se.bupp.cs3k.server.model.{Competitor, Ladder}
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{CompetitorDao, LadderDao}
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import org.apache.wicket.extensions.markup.html.repeater.data.table.{PropertyColumn, AbstractColumn, IColumn}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import org.apache.wicket.event.Broadcast
import se.bupp.cs3k.server.web.component.Events.{CreateLadderEvent, CompetitionSelectedEvent}
import org.apache.wicket.ajax.markup.html.AjaxLink
import se.bupp.cs3k.server.service.{ResultService, CompetitionService}
import se.bupp.cs3k.example.ExampleScoreScheme.ExCompetitorTotal
import se.bupp.cs3k.api.score.ScoreScheme.CompetitorTotal

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-16
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */

object LadderStandingsPanel {
  type RowType = (Competitor,Option[ExCompetitorTotal])
}
import LadderStandingsPanel._

class LadderStandingsPanel(id:String, ladderModel:IModel[Ladder]) extends Panel(id) {


  @SpringBean
  var ladderDao:LadderDao = _

  @SpringBean
  var competitorDao:CompetitorDao = _

  @SpringBean
  var ladderService:CompetitionService = _

  @SpringBean
  var resultService:ResultService = _

  val provider = new SortableDataProvider[RowType,String]() {
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = {
      val ladder = ladderModel.getObject
      val participants = competitorDao.findCompetitionParticipants(ladder, p1.toInt,p2.toInt)
      val results = ladderService.decoratePariticpantResults(participants, ladder)
      results.toIterator
    }

    def size() = ladderDao.selectRangeCount

    def model(p1: RowType) = new LoadableDetachableModel[RowType](p1) {
      def load() = {
        val ladder = ladderModel.getObject
        val comp = competitorDao.find(p1._1.id).get
        ladderService.decoratePariticpantResults(List(comp),ladder).head
      }
    }
  }

  val columns = List[IColumn[RowType,String]] (
    new AbstractColumn[RowType,String](new Model("Competition"))
    {
      def populateItem(cellItem:Item[ICellPopulator[RowType]], componentId:String, model:IModel[RowType])
      {
        cellItem.add(new AjaxLinkLabel(componentId, new PropertyModel(model,"_1.nameAccessor")) {
          def onClick(target: AjaxRequestTarget) {
            //send(getPage(), Broadcast.BREADTH, new CompetitionSelectedEvent(model.getObject, target));
          }
        });
      }
    },
    new PropertyColumn(new Model("Setup"), "_1.id"),
    new FodelPropertyColumn(new Model("Ein"),
      ((a:RowType) => a._2.map(_.getRenderer.render()(0)).orNull,
        (a:RowType,v:String) => ()
        )),
  new FodelPropertyColumn(new Model("Zwei"),
    ((a:RowType) => a._2.map(_.getRenderer.render()(1)).orNull,
      (a:RowType,v:String) => ()
      )),
  new FodelPropertyColumn(new Model("Drei"),
    ((a:RowType) => a._2.map(_.getRenderer.render()(2)).orNull,
      (a:RowType,v:String) => ()
      ))
  )

  add(new NiceDataTable("table", columns, provider, 8))

  add(new AjaxLink("debugTrans") {

    def onClick(p1: AjaxRequestTarget) {
      var ladder = ladderModel.getObject

      ladderService.startLadder(ladder)
    }
  })
}
