package se.bupp.cs3k.server.web.component

import generic.VertTabbedPanel
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.IModel
import se.bupp.cs3k.server.model.Team
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-16
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
class TeamPanel(id:String, model:IModel[Team]) extends Panel(id) {

  add(new VertTabbedPanel("tab-panel",
    List(
      ("Overview", (cId:String) => new JoinTeamPanel(cId,model) {
        def onUpdate(t: AjaxRequestTarget) {

        }
      }),
      ("Members", (cId:String) => new TeamListPanel(cId)),
      ("Results", (cId:String) => new TournamentView(cId)),
      ("Admin", (cId:String) => new Label(cId, "results"))
      )
    )
  )
}
