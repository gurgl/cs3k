package se.bupp.cs3k.server.web.component

import generic.table.NiceDataTable
import generic.{AjaxLinkLabel, ListSelector}
import org.apache.wicket.markup.html.panel.{EmptyPanel, Panel}
import se.bupp.cs3k.server.model.{Team, Ladder}
import org.apache.wicket.markup.repeater.data.IDataProvider
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.LadderDao
import org.apache.wicket.model.{PropertyModel, IModel, Model, LoadableDetachableModel}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.extensions.breadcrumb.BreadCrumbBar
import org.apache.wicket.event.Broadcast
import se.bupp.cs3k.server.web.component.Events.{TeamSelectedEvent, CreateLadderEvent, CreateTeamEvent, LadderSelectedEvent}
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.extensions.markup.html.repeater.data.table.{PropertyColumn, AbstractColumn, IColumn}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-06
 * Time: 21:18
 * To change this template use File | Settings | File Templates.
 */
class LadderListPanel(id:String) extends Panel(id) {

  @SpringBean
  var ladderDao:LadderDao = _


  val provider = new SortableDataProvider[Ladder,String]() {
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = ladderDao.selectRange(p1.toInt,p2.toInt).toIterator

    def size() = ladderDao.selectRangeCount

    def model(p1: Ladder) = new LoadableDetachableModel[Ladder](p1) {
      def load() = ladderDao.find(p1.id).get

    }
  }

  val columns = List[IColumn[Ladder,String]] (
    new AbstractColumn[Ladder,String](new Model("Competition"))
    {
      def populateItem(cellItem:Item[ICellPopulator[Ladder]], componentId:String, model:IModel[Ladder])
      {
        cellItem.add(new AjaxLinkLabel(componentId, new PropertyModel(model,"name")) {
          def onClick(target: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new LadderSelectedEvent(model.getObject, target));
          }
        });
      }
    },
    new PropertyColumn(new Model("Setup"),"gameSetup.name"),
    new PropertyColumn(new Model("State"),"state"),
    new PropertyColumn(new Model("Participants"),"name"),
    new PropertyColumn(new Model("Left"),"name")
  )

  add(new NiceDataTable("table", columns, provider, 8))


  add(new AjaxLink("createTeamLink") {
    def onClick(target: AjaxRequestTarget) {
      send(getPage(), Broadcast.BREADTH, new CreateLadderEvent(target));
    }
  })
}
