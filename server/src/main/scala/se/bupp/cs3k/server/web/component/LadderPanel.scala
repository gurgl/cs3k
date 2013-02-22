package se.bupp.cs3k.server.web.component

import generic.VertTabbedPanel
import org.apache.wicket.markup.html.panel.{EmptyPanel, Panel}
import org.apache.wicket.model.IModel
import se.bupp.cs3k.server.model.Ladder
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel
import org.apache.wicket.markup.html.basic.Label

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-16
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
class LadderPanel(id:String, model:IModel[Ladder]) extends Panel(id) {


  add(new VertTabbedPanel("tab-panel",
    List(
      ("Overview", (cId:String) => new Label(cId,"signup etc")),
      ("Challangers", (cId:String) => new TeamListPanel(cId)),
      ("Results", (cId:String) => new Label(cId, "results"))
    )
  ))


}
