package se.bupp.cs3k.server.web.component

import competitor.PlayerListPanel
import generic.VertTabbedPanel
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.{Model, IModel}
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
      ("Overview", (cId:String) => new CompetitorOverview(cId,model)),
      ("Members", (cId:String) => new PlayerListPanel(cId,null)),
      ("Admin", (cId:String) => new Label(cId, "results"))
      )
    )
  )
}
