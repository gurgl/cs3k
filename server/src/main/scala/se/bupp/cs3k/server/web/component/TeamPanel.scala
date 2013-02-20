package se.bupp.cs3k.server.web.component

import generic.breadcrumb.BreadCrumbPanel
import generic.breadcrumb.BreadCrumbPanel.BreadCrumbModel
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.Model
import se.bupp.cs3k.server.model.{Team, Ladder}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label
import se.bupp.cs3k.server.web.component.Events.{CreateTeamEvent, TeamSelectedEvent}
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.event.Broadcast

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-18
 * Time: 23:48
 * To change this template use File | Settings | File Templates.
 */


class TeamPanel(id:String) extends Panel(id) {

  add(new BreadCrumbPanel("breadCrumbPanel",new BreadCrumbModel {
    val name = "Team"
    val model = new Model[Team](null)
    val createComponent = (id:String, m:Model[_]) => {
      new TeamListPanel(id)
    }
  }) {
    override def pa:PartialFunction[Any,(BreadCrumbModel,AjaxRequestTarget,Int)] = {
      case lse:TeamSelectedEvent =>
        println("Receiving event spec")
        val newItem = new BreadCrumbModel() {
          val name = lse.team.name
          val model = new Model[Team](lse.team)
          val createComponent = (id:String, m:Model[_]) => {
            var ladMod = m.asInstanceOf[Model[Team]]
            new Label(id, ladMod.getObject.name)
          }
        }
        (newItem,lse.target,1)

      case lse:CreateTeamEvent =>
        println("Receiving event spec")
        val newItem = new BreadCrumbModel() {
          val name = "Create Team"
          val model = new Model[String]("")
          val createComponent = (id:String, m:Model[_]) => {

            new TeamFormPanel(id, "Create Team")
          }
        }
        (newItem,lse.target,1)
    }
  })

  add(new AjaxLink("createTeamLink") {
    def onClick(target: AjaxRequestTarget) {
      send(getPage(), Broadcast.BREADTH, new CreateTeamEvent( target));
    }
  })

}
