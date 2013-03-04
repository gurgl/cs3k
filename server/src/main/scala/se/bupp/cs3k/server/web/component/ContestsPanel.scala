package se.bupp.cs3k.server.web.component

import generic.breadcrumb.{BreadCrumbPanel, BreadCrumbItem}
import generic.breadcrumb.BreadCrumbPanel.BreadCrumbModel
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.html.{list, WebMarkupContainer}
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.Model
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.ajax.markup.html.AjaxLink

import org.apache.wicket.event.IEvent
import se.bupp.cs3k.server.model.{Competition, Team, Ladder}
import org.apache.wicket.Component
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.markup.ComponentTag
import se.bupp.cs3k.server.web.component.Events.{CreateLadderEvent, CompetitionSelectedEvent}
import se.bupp.cs3k.server.web.component.LadderFormPanel

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-18
 * Time: 19:08
 * To change this template use File | Settings | File Templates.
 */

object Events {
  abstract class AbstractEvent(var target:AjaxRequestTarget )

  class CompetitionSelectedEvent(var ladder:Competition, target:AjaxRequestTarget ) extends AbstractEvent(target)
  class TeamSelectedEvent(var team:Team, target:AjaxRequestTarget ) extends AbstractEvent(target)
  class CreateTeamEvent(target:AjaxRequestTarget ) extends AbstractEvent(target)
  class CreateLadderEvent(target:AjaxRequestTarget ) extends AbstractEvent(target)



}
class ContestsPanel(id:String) extends Panel(id) {


  add(new BreadCrumbPanel("breadCrumbPanel",new BreadCrumbModel {
    val name = "Contests"
    val model = new Model[Ladder](null)
    val createComponent = (id:String, m:Model[_]) => {
      new CompetitionListPanel(id)
    }
  }) {
     override def pa:PartialFunction[Any,(BreadCrumbModel,AjaxRequestTarget,Int)] = {
       case lse:CompetitionSelectedEvent =>

         val newItem = new BreadCrumbModel() {
           val name = lse.ladder.name
           val model = new Model(lse.ladder)
           val createComponent = (id:String, m:Model[_]) => {
             var ladMod = m.asInstanceOf[Model[Competition]]
             new LadderPanel(id, ladMod)
           }
         }
         (newItem,lse.target,1)
       case cle:CreateLadderEvent =>
         val newItem = new BreadCrumbModel() {
           val name = "Propose a competition"
           val model = new Model[Ladder](null)
           val createComponent = (id:String, m:Model[_]) => {
              new LadderFormPanel(id, "Propose competition")
           }
         }
         (newItem,cle.target,1)

     }
  })




  //contentContainer.add()
}
