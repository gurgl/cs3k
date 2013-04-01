package se.bupp.cs3k.server.web.component.competitor

import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.model.{TeamMember, User}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.UserDao
import org.apache.wicket.model._
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.event.Broadcast
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.extensions.markup.html.repeater.data.table._
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import scala.Predef.String
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import se.bupp.cs3k.server.web.component.generic.AjaxLinkLabel
import se.bupp.cs3k.server.web.component.generic.table.NiceDataTable
import se.bupp.cs3k.server.web.component.contest.Events
import Events.CompetitorSelectedEvent


/**
  * Created with IntelliJ IDEA.
  * User: karlw
  * Date: 2012-11-06
  * Time: 21:18
  * To change this template use File | Settings | File Templates.
  */
class TeamMemberListPanel(id:String, provider:SortableDataProvider[TeamMember,String]) extends Panel(id) {

   @SpringBean
   var teamDao:UserDao = _

   val columns = List[IColumn[TeamMember,String]] (
     new AbstractColumn[TeamMember,String](new Model("Name"))
     {
       def populateItem(cellItem:Item[ICellPopulator[TeamMember]], componentId:String, model:IModel[TeamMember])
       {
         cellItem.add(new AjaxLinkLabel(componentId, new PropertyModel(model,"id.user.username")) {
           def onClick(target: AjaxRequestTarget) {
             send(getPage(), Broadcast.BREADTH, new CompetitorSelectedEvent(model.getObject.id.user, target));
           }
         });
       }
     },
     new PropertyColumn(new Model("Members"),"id.user.username")

   )
   add(new NiceDataTable("table", columns, provider, 8))
 }
