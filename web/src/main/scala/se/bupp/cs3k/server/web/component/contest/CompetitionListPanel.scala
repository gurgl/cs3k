package se.bupp.cs3k.server.web.component.contest

import org.apache.wicket.markup.html.panel.{EmptyPanel, Panel}
import se.bupp.cs3k.server.model.{Competition, Team, Ladder}
import org.apache.wicket.markup.repeater.data.IDataProvider
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{CompetitionDao, LadderDao}
import org.apache.wicket.model.{PropertyModel, IModel, Model, LoadableDetachableModel}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.extensions.breadcrumb.BreadCrumbBar
import org.apache.wicket.event.Broadcast
import Events.{CreateLadderEvent, CreateTeamEvent, CompetitionSelectedEvent}
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.extensions.markup.html.repeater.data.table.{PropertyColumn, AbstractColumn, IColumn}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import se.bupp.cs3k.model.CompetitionState
import javax.persistence.criteria.{Root, CriteriaBuilder}
import se.bupp.cs3k.server.web.component.generic.{FodelPropertyColumn, AjaxLinkLabel}
import se.bupp.cs3k.server.web.component.generic.table.NiceDataTable


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-06
 * Time: 21:18
 * To change this template use File | Settings | File Templates.
 */
class CompetitionListPanel(id:String, m:IModel[Option[CompetitionState]] = new Model(None)) extends Panel(id) {

  @SpringBean
  var competitionDao:CompetitionDao = _


  val provider = new SortableDataProvider[Competition,String]() {
    import scala.collection.JavaConversions.asJavaIterator

    def iterator(p1: Long, p2: Long) = competitionDao.withState(m.getObject,p1.toInt,p2.toInt).toIterator

    def size() = competitionDao.withStateCnt(m.getObject)

    def model(p1: Competition) = new LoadableDetachableModel[Competition](p1) {
      def load() = competitionDao.find(p1.id).get

    }
  }

  val columns = List[IColumn[Competition,String]] (
    new AbstractColumn[Competition,String](new Model("Competition"))
    {
      def populateItem(cellItem:Item[ICellPopulator[Competition]], componentId:String, model:IModel[Competition])
      {
        cellItem.add(new AjaxLinkLabel(componentId, new PropertyModel(model,"name")) {
          def onClick(target: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new CompetitionSelectedEvent(model.getObject, target));
          }
        });
      }
    },
    new PropertyColumn(new Model("Setup"),"gameSetup.name"),
    new FodelPropertyColumn(new Model("Form"),
      ((a:Competition) => a.getClass.getSimpleName,
        (a:Competition,v:String) => ()
        )),
    new PropertyColumn(new Model("Competitor Type"),"competitorType"),
    new PropertyColumn(new Model("State"),"state"),

    new PropertyColumn(new Model("Left"),"name")
  )

  var table: NiceDataTable[Competition, String] = new NiceDataTable("table", columns, provider, 8)
  add(table)



}
