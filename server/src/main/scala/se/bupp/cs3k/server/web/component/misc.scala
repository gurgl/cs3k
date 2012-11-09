package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.{FeedbackPanel, Panel}
import se.bupp.cs3k.server.model.{Ladder, TeamMember, TeamMemberPk, Team}
import org.apache.wicket.ajax.markup.html.AjaxLink
import se.bupp.cs3k.server.service.dao.TeamDao
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.web.{LoggedInOnly, WiaSession, WicketApplication}
import org.apache.wicket.ajax.AjaxRequestTarget
import se.bupp.cs3k.server.service.TeamService
import org.apache.wicket.ajax.markup.html
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.AbstractReadOnlyModel

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-09
 * Time: 21:25
 * To change this template use File | Settings | File Templates.
 */

@LoggedInOnly
abstract class JoinTeamPanel(id:String, t:Team) extends Panel(id) {

  @SpringBean
  var ts:TeamService = _

  val fb = new FeedbackPanel("feedback")
  val label = new Label("label", new AbstractReadOnlyModel[String] {
    def getObject = {
      val u = WiaSession.get().getUser ;
      if(u != null && ts.isUserMemberOfTeam(u, t)) "leave" else "join"
    }
  })


  def onUpdate(t:AjaxRequestTarget)
  setOutputMarkupId(true)

  add(fb.setOutputMarkupId(true))
  val link = new AjaxLink[Team]("link") {

    def onClick(target: AjaxRequestTarget) {
      val a =
        if(!ts.isUserMemberOfTeam(WiaSession.get().getUser, t)) {
          ts.storeTeamMember(WiaSession.get().getUser, t)
          info("Joined team")
        } else {
          ts.leaveTeam(WiaSession.get().getUser, t)
          info("Left team")
        }

      JoinTeamPanel.this.onUpdate(target)

      target.add(JoinTeamPanel.this)
    }
  }

  link.add(label)
  add(link)
}

