package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.GameReservationService
import se.bupp.cs3k.server.model.{TeamMember, Team, GameOccassion, User}
import se.bupp.cs3k.server.web.{WicketApplication, WiaSession}
import org.apache.wicket.extensions.markup.html.repeater.data.table._
import org.apache.wicket.model.{LoadableDetachableModel, IModel, Model}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.request.resource.ResourceReference
import org.apache.wicket.markup.repeater.data.ListDataProvider
import org.apache.wicket.extensions.markup.html.repeater.util.{SortParam, SingleSortState}
import org.apache.wicket.extensions.markup.html.repeater.data.sort.{SortOrder, ISortState}
import se.bupp.cs3k.server.service.dao.{TeamMemberDao, CompetitorDao, TeamDao}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-17
 * Time: 19:31
 * To change this template use File | Settings | File Templates.
 */
class TeamMembershipList(id:String, mod:IModel[User]) extends Panel(id) {

  @SpringBean
  var teamDao:TeamDao = _

  @SpringBean
  var teamMemberDao:TeamMemberDao = _

  import scala.collection.JavaConversions.seqAsJavaList

  val columns = List[IColumn[TeamMember,String]] (
    /*new AbstractColumn[GameOccassion,String](new Model(""))
    {
      def populateItem(cellItem:Item[ICellPopulator[GameOccassion]], componentId:String, model:IModel[GameOccassion])
      {

        cellItem.add(new ResourceLinkComp(componentId,ref,parameters))
      }
    },*/
    new PropertyColumn(new Model("Team"),"id.team.nameAccessor")

  )

  var challanges = new ISortableDataProvider[TeamMember,String] {
    def detach() {}
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = teamDao.findUserTeamMemberships(mod.getObject,p1,p2).toIterator

    def size() = teamDao.findUserTeamMembershipsCount(mod.getObject)

    def model(p1: TeamMember) = new LoadableDetachableModel[TeamMember]() {
      def load() = teamMemberDao.find(p1.id).get
    }

    def getSortState = null
  }

  var table = new DataTable("table", columns, challanges, 8)
  add(table)
}
