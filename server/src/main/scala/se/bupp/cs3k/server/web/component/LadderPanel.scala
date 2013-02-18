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
import se.bupp.cs3k.server.model.{Team, Ladder}
import org.apache.wicket.Component
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.markup.ComponentTag
import se.bupp.cs3k.server.web.component.Events.LadderSelectedEvent

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-18
 * Time: 19:08
 * To change this template use File | Settings | File Templates.
 */

object Events {
  abstract class AbstractEvent(var target:AjaxRequestTarget )

  class LadderSelectedEvent(var ladder:Ladder, target:AjaxRequestTarget ) extends AbstractEvent(target)
  class TeamSelectedEvent(var team:Team, target:AjaxRequestTarget ) extends AbstractEvent(target)


}
class LadderPanel(id:String) extends Panel(id) {


  /*
  override def onEvent(event: IEvent[_]) {
    super.onEvent(event)
    println("Receiving event")
    event.getPayload match {
      case lse:LadderSelectedEvent =>
        println("Receiving event spec")
        var list = itemsModel.getObject.take(1)
        list = list :+ new BreadCrumbModel() {
          val name = lse.ladder.name
          val model = new Model[Ladder](lse.ladder)
          val createComponent = (id:String, m:Model[_]) => {
            var ladMod = m.asInstanceOf[Model[Ladder]]
            new Label(id, ladMod.getObject.name)
          }
         }
        itemsModel.setObject(new java.util.ArrayList(list.toList))
        lse.target.add(breadCrumbContainer)
        lse.target.add(contentContainer)

      case _ =>
    }
  }
  */

  add(new BreadCrumbPanel("breadCrumbPanel",new BreadCrumbModel {
    val name = "Ladder"
    val model = new Model[Ladder](null)
    val createComponent = (id:String, m:Model[_]) => {
      new LadderListPanel(id)
    }
  }) {
     override def pa:PartialFunction[Any,(BreadCrumbModel,AjaxRequestTarget,Int)] = {
       case lse:LadderSelectedEvent =>
         println("Receiving event spec")
         val newItem = new BreadCrumbModel() {
           val name = lse.ladder.name
           val model = new Model[Ladder](lse.ladder)
           val createComponent = (id:String, m:Model[_]) => {
             var ladMod = m.asInstanceOf[Model[Ladder]]
             new Label(id, ladMod.getObject.name)
           }
         }
       (newItem,lse.target,1)
     }
  })




  //contentContainer.add()
}
