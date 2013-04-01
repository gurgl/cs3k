package se.bupp.cs3k.server.web.component.contest

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.html.{list, WebMarkupContainer}
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.Model
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.ajax.markup.html.AjaxLink

import org.apache.wicket.event.IEvent
import se.bupp.cs3k.server.model._
import org.apache.wicket.Component
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.markup.ComponentTag
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.server.web.component.generic.breadcrumb.BreadCrumbPanel
import se.bupp.cs3k.server.web.component.generic.breadcrumb.BreadCrumbPanel.BreadCrumbModel
import se.bupp.cs3k.server.web.component.contest.Events.{CreateLadderEvent, AbstractContestEvent, CompetitionSelectedEvent}


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-18
 * Time: 19:08
 * To change this template use File | Settings | File Templates.
 */

object Events {
  sealed abstract class AbstractEvent(var target:AjaxRequestTarget ) extends Serializable
  sealed abstract class AbstractContestEvent(target:AjaxRequestTarget) extends AbstractEvent(target)
  sealed abstract class AbstractCompetitorEvent(target:AjaxRequestTarget) extends AbstractEvent(target)

  class CompetitionSelectedEvent(var ladder:Competition, target:AjaxRequestTarget ) extends AbstractContestEvent(target)
  //class TeamSelectedEvent(var team:Team, target:AjaxRequestTarget ) extends AbstractCompetitorEvent(target)
  //class PlayerSelectedEvent(var user:User, target:AjaxRequestTarget ) extends AbstractCompetitorEvent(target)
  class CompetitorSelectedEvent(var competitor:Competitor, target:AjaxRequestTarget ) extends AbstractCompetitorEvent(target)
  class CreateTeamEvent(target:AjaxRequestTarget ) extends AbstractCompetitorEvent(target)
  class CreateLadderEvent(target:AjaxRequestTarget ) extends AbstractContestEvent(target)



}
class ContestsPanel(id:String, eventOpt:Model[Option[AbstractContestEvent]]) extends Panel(id) {


  val panel = new BreadCrumbPanel("breadCrumbPanel",new BreadCrumbModel {
    val name = "Contests"
    val model = new Model[Ladder](null)
    val createComponent = (id:String, m:Model[_]) => {
      new CompetitionMainListPanel(id)
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
              new ContestFormPanel(id, "Propose competition")
           }
         }
         (newItem,cle.target,1)

     }
  }

  eventOpt.getObject.foreach(i => panel.onEvent(new IEvent[AbstractContestEvent] {
    def getType = ???

    def dontBroadcastDeeper() {}

    def stop() {}

    def getPayload = i

    def getSource = ???
  }))
  add(panel)




  //contentContainer.add()
}
