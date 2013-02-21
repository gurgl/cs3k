package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.IModel
import se.bupp.cs3k.server.model.Ladder

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-16
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
class LadderPanel(id:String, model:IModel[Ladder]) extends Panel(id) {


  add(new TeamListPanel("teamList"))


}
