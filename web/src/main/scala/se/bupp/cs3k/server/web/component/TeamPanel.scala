package se.bupp.cs3k.server.web.component

import competitor.{TeamMemberListPanel, PlayerListPanel}
import generic.VertTabbedPanel
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.{LoadableDetachableModel, Model, IModel}
import se.bupp.cs3k.server.model.{TeamMember, Team}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{TeamMemberDao, TeamDao}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-16
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
class TeamPanel(id:String, mod:IModel[Team]) extends Panel(id) {


  @SpringBean
  var teamDao:TeamDao = _
  @SpringBean
  var teamMemberDao:TeamMemberDao = _

  val teamPlayers = new SortableDataProvider[TeamMember,String]() {
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = teamDao.findTeamMemberships(mod.getObject, p1.toInt,p2.toInt).toIterator

    def size() = teamDao.findTeamMembershipsCount(mod.getObject)

    def model(p1: TeamMember) = new LoadableDetachableModel[TeamMember](p1) {
      def load() = teamMemberDao.find(p1.id).get

    }
  }

  add(new VertTabbedPanel("tab-panel",
    List(
      ("Overview", (cId:String) => new CompetitorOverview(cId,mod)),
      ("Members", (cId:String) => new TeamMemberListPanel(cId,teamPlayers)),
      ("Admin", (cId:String) => new Label(cId, "results"))
      )
    )
  )
}
