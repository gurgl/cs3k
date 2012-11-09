package se.bupp.cs3k.server.web.component

import generic.ListSelector
import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.model.Team
import org.apache.wicket.markup.repeater.data.IDataProvider
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.TeamDao
import org.apache.wicket.model.LoadableDetachableModel
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import se.bupp.cs3k.server.web.WiaSession
import org.springframework.transaction.PlatformTransactionManager
import se.bupp.cs3k.server.Util
import se.bupp.cs3k.server.service.TeamService


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-06
 * Time: 21:18
 * To change this template use File | Settings | File Templates.
 */
class TeamPanel(id:String) extends Panel(id) {

  @SpringBean
  var teamDao:TeamDao = _

  @SpringBean
  var ts:TeamService = _
  //var tm:PlatformTransactionManager = _

  val provider = new IDataProvider[Team]() {
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = teamDao.selectRange(p1.toInt,p2.toInt).toIterator

    def size() = teamDao.selectRangeCount

    def model(p1: Team) = new LoadableDetachableModel[Team](p1) {
      def load() = teamDao.find(p1.id).get

    }

    def detach() {}
  }


  var selector:WebMarkupContainer = _
    selector = new ListSelector[java.lang.Long,Team]("listSelector", provider) {
    override def onClick(target: AjaxRequestTarget, modelObject: Team) {

        contentContainer.addOrReplace(new JoinTeamPanel("content",modelObject) {
          def onUpdate(t: AjaxRequestTarget) {

            t.add(selector)
          }
        })
        target.add(contentContainer)

    }

    def renderItem(t: Team) : String = {
      val me = WiaSession.get().getUser

      val res:String = t.name + (if(me != null && ts.isUserMemberOfTeam(me,t)) " [member]" else "")
      res


    }
  }
  selector.setOutputMarkupId(true)
  add(selector)



  var contentContainer= new WebMarkupContainer("contentContainer")
  contentContainer.setOutputMarkupId(true)
  add(contentContainer)
  contentContainer.add(new TeamFormPanel("content") )
}
