package se.bupp.cs3k.server.web.component

import generic.table.NiceDataTable
import generic.{AjaxLinkLabel, ListSelector}
import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.model.Team
import org.apache.wicket.markup.repeater.data.{DataView, IDataProvider}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.TeamDao
import org.apache.wicket.model._
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import se.bupp.cs3k.server.web.WiaSession
import org.springframework.transaction.PlatformTransactionManager
import se.bupp.cs3k.server.Util
import se.bupp.cs3k.server.service.TeamService
import org.apache.wicket.event.Broadcast
import se.bupp.cs3k.server.web.component.Events.{TeamSelectedEvent, LadderSelectedEvent}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.extensions.markup.html.repeater.data.table._
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import java.lang.String
import scala.Predef.String
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-06
 * Time: 21:18
 * To change this template use File | Settings | File Templates.
 */
class TeamListPanel(id:String) extends Panel(id) {

  @SpringBean
  var teamDao:TeamDao = _

  @SpringBean
  var ts:TeamService = _
  //var tm:PlatformTransactionManager = _

  val provider = new SortableDataProvider[Team,String]() {
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = teamDao.selectRange(p1.toInt,p2.toInt).toIterator

    def size() = teamDao.selectRangeCount

    def model(p1: Team) = new LoadableDetachableModel[Team](p1) {
      def load() = teamDao.find(p1.id).get

    }
    //def detach() {}


  }


  val columns = List[IColumn[Team,String]] (
    new AbstractColumn[Team,String](new Model("Actions"))
    {
      def populateItem(cellItem:Item[ICellPopulator[Team]], componentId:String, model:IModel[Team])
      {
        cellItem.add(new AjaxLinkLabel(componentId, new PropertyModel(model,"name")) {
          def onClick(target: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new TeamSelectedEvent(model.getObject, target));
          }
        });
      }
    },
    new PropertyColumn(new Model("Tja"),"name")
  )

  add(new NiceDataTable("table", columns, provider, 8))



  /*def renderItem(t: Team) : String = {
      val me = WiaSession.get().getUser

      val res:String = t.name + (if(me != null && ts.isUserMemberOfTeam(me,t)) " [member]" else "")
      res


    }*/


  /*var selector:WebMarkupContainer = _
    selector = new ListSelector[java.lang.Long,Team]("listSelector", provider) {
    override def onClick(target: AjaxRequestTarget, modelObject: Team) {



      contentContainer.addOrReplace(new JoinTeamPanel("content",modelObject) {
          def onUpdate(t: AjaxRequestTarget) {

            t.add(selector)
          }
        })
        target.add(contentContainer)

    }
  }*/

  //selector.setOutputMarkupId(true)
  //add(selector)



  /*var contentContainer= new WebMarkupContainer("contentContainer")
  contentContainer.setOutputMarkupId(true)
  add(contentContainer)
  contentContainer.add(new TeamFormPanel("content", "Create team") )*/
}
