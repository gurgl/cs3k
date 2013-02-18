package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.html.{list, WebMarkupContainer}
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.Model
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.ajax.markup.html.AjaxLink

import org.apache.wicket.event.IEvent
import se.bupp.cs3k.server.model.Ladder
import org.apache.wicket.Component
import se.bupp.cs3k.server.web.component.LadderPanel.LadderSelectedEvent
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.markup.ComponentTag

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-18
 * Time: 19:08
 * To change this template use File | Settings | File Templates.
 */

object LadderPanel {
  abstract class AbstractEvent(var target:AjaxRequestTarget )

  class LadderSelectedEvent(var ladder:Ladder, target:AjaxRequestTarget ) extends AbstractEvent(target)


}
class LadderPanel(id:String) extends Panel(id) {

  import scala.collection.JavaConversions.asScalaBuffer
  import scala.collection.JavaConversions.seqAsJavaList

  var crumbItems = new java.util.ArrayList[BreadCrumbModel]()
  var itemsModel = new Model(crumbItems)

  var breadCrumbContainer = new WebMarkupContainer("breadCrumbContainer")
  var contentContainer = new WebMarkupContainer("contentContainer")

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


  trait BreadCrumbModel extends Serializable {

    val createComponent: (String,Model[_]) => Component
    val model:Model[_]
    val name:String

  }


  add(breadCrumbContainer)
  var breadCrumbPanel = new ListView[BreadCrumbModel]("breadCrumb",itemsModel) {


    def populateItem(p1: ListItem[BreadCrumbModel]) {
        var modelObject = p1.getModelObject
      if (this.getModelObject.size() == p1.getIndex + 1) {
        var label = new Label("item", p1.getModelObject.name)
        p1.add(new AttributeAppender("class","active"))
        p1.add(label)
        contentContainer.addOrReplace(modelObject.createComponent("content",modelObject.model))
        println("Rerendering head")
      } else {
        p1.add(new BreadCrumbItem("item", modelObject.name) {
          def onCrumbClick(target: AjaxRequestTarget) {
            val items  = getModelObject.take(p1.getIndex + 1)
            setModelObject(items)
            var modelObject2 = p1.getModelObject
            contentContainer.addOrReplace(modelObject2.createComponent("content",modelObject2.model))
            //send(getPage(), Broadcast.BREADTH, new LadderSelectedEvent(target));
            target.add(breadCrumbContainer)
            target.add(contentContainer)
          }
        })
      }
    }
  }
  breadCrumbPanel.setOutputMarkupId(true)
  breadCrumbContainer.setOutputMarkupId(true)
  breadCrumbContainer.add(breadCrumbPanel)

  crumbItems.add(new BreadCrumbModel() {
    val name = "Ladder"
    val model = new Model[Ladder](null)
    val createComponent = (id:String, m:Model[_]) => {
      new LadderListPanel(id)
    }
    })

  add(contentContainer)
  contentContainer.setOutputMarkupId(true)

  //contentContainer.add()
}
