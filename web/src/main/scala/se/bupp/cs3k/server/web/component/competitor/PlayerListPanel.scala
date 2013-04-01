package se.bupp.cs3k.server.web.component.competitor

import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.model.{User, Team}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{UserDao, TeamDao}
import org.apache.wicket.model._
import org.apache.wicket.ajax.AjaxRequestTarget
import se.bupp.cs3k.server.service.TeamService
import org.apache.wicket.event.Broadcast
import se.bupp.cs3k.server.web.component.contest.Events
import Events.{CompetitorSelectedEvent, CreateTeamEvent}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.extensions.markup.html.repeater.data.table._
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import scala.Predef.String
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import se.bupp.cs3k.server.web.component.generic.AjaxLinkLabel
import se.bupp.cs3k.server.web.component.generic.table.NiceDataTable


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-06
 * Time: 21:18
 * To change this template use File | Settings | File Templates.
 */
class PlayerListPanel(id:String, provider:SortableDataProvider[User,String]) extends Panel(id) {

  @SpringBean
  var teamDao:UserDao = _

  val columns = List[IColumn[User,String]] (
    new AbstractColumn[User,String](new Model("Name"))
    {
      def populateItem(cellItem:Item[ICellPopulator[User]], componentId:String, model:IModel[User])
      {
        cellItem.add(new AjaxLinkLabel(componentId, new PropertyModel(model,"username")) {
          def onClick(target: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new CompetitorSelectedEvent(model.getObject, target));
          }
        });
      }
    },
    new PropertyColumn(new Model("Members"),"username")

  )
  add(new NiceDataTable("table", columns, provider, 8))
}
