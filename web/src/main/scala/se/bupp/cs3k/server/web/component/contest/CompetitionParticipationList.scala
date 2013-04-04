package se.bupp.cs3k.server.web.component.contest

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{CompetitionDao, TeamMemberDao, TeamDao}
import org.apache.wicket.extensions.markup.html.repeater.data.table._
import se.bupp.cs3k.server.model.{Team, Competition, User, TeamMember}
import org.apache.wicket.model.{PropertyModel, IModel, LoadableDetachableModel, Model}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.event.Broadcast
import Events.CompetitionSelectedEvent
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.server.web.component.generic.{FodelPropertyColumn, AjaxLinkLabel}
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-17
 * Time: 19:32
 * To change this template use File | Settings | File Templates.
 */
class CompetitionParticipationList(id:String, mod:IModel[User]) extends Panel(id) {

  @SpringBean
  var competitionDao:CompetitionDao = _

  @SpringBean
  var teamMemberDao:TeamMemberDao = _

  import scala.collection.JavaConversions.seqAsJavaList

  val columns = List[IColumn[Competition,String]] (
    new AbstractColumn[Competition,String](new Model(""))
    {
      def populateItem(cellItem:Item[ICellPopulator[Competition]], componentId:String, model:IModel[Competition])
      {

        cellItem.add(new AjaxLinkLabel(componentId,new PropertyModel[String](model,"name")){
          def onClick(target: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new CompetitionSelectedEvent(model.getObject, target));
          }
        })
      }
    },
    new PropertyColumn(new Model("Setup"),"gameSetup.name")
    ,
    new FodelPropertyColumn(new Model("Form"),
      ((a:Competition) => a.getClass.getSimpleName,
        (a:Competition,v:String) => ()
        )),
    new PropertyColumn(new Model("Competitor Type"),"competitorType"),
    new PropertyColumn(new Model("State"),"state")

  )

  var challanges = new ISortableDataProvider[Competition,String] {
    def detach() {}
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = competitionDao.findUserCompetitions(mod.getObject,p1,p2).toIterator

    def size() = competitionDao.findUserCompetitionsCount(mod.getObject)

    def model(p1: Competition) = new LoadableDetachableModel[Competition]() {
      def load() = competitionDao.find(p1.id).get
    }

    def getSortState = null
  }

  var table = new DataTable("table", columns, challanges, 8)
  table.addTopToolbar(new AjaxFallbackHeadersToolbar(table,challanges))
  add(table)


}
