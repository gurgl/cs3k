package se.bupp.cs3k.server.web.component

import generic.VertTabbedPanel
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-16
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
class PlayerPanel(id:String) extends Panel(id) {

  add(new VertTabbedPanel("tab-panel",
    List(
      ("Overview", (cId:String) => new PlayerOverview(cId)),
      ("Shedule", (cId:String) => new Label(cId, "Scheudle")),
      ("Results", (cId:String) => new Label(cId, "Results")),
      ("Settings", (cId:String) => new Label(cId, "Settings"))
    )
  ))
}
