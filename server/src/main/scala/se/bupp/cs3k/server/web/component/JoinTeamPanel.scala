package se.bupp.cs3k.server.web.component

import se.bupp.cs3k.server.web.auth.LoggedInOnly
import org.apache.wicket.model.{AbstractReadOnlyModel, IModel}
import se.bupp.cs3k.server.model.Team
import org.apache.wicket.markup.html.panel.{FeedbackPanel, Panel}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.TeamService
import org.apache.wicket.markup.html.basic.Label
import se.bupp.cs3k.server.web.WiaSession
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.markup.html.AjaxLink

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-03
 * Time: 01:51
 * To change this template use File | Settings | File Templates.
 */
@LoggedInOnly
abstract class JoinTeamPanel(id:String, m:IModel[Team]) extends Panel(id) {

  @SpringBean
  var ts:TeamService = _

  val fb = new FeedbackPanel("feedback")
  val label = new Label("label", new AbstractReadOnlyModel[String] {
    def getObject = {
      val t = m.getObject
      val u = WiaSession.get().getUser ;
      if(u != null && ts.isUserMemberOfTeam(u, t)) "leave" else "join"
    }
  })


  def onUpdate(t:AjaxRequestTarget)
  setOutputMarkupId(true)

  add(fb.setOutputMarkupId(true))
  val link = new AjaxLink[Team]("link") {

    def onClick(target: AjaxRequestTarget) {
      val t = m.getObject
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