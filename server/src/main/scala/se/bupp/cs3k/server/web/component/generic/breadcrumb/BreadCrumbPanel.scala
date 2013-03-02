package se.bupp.cs3k.server.web.component.generic.breadcrumb

import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.Model
import org.apache.wicket.Component
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.event.IEvent
import se.bupp.cs3k.server.model.Ladder
import se.bupp.cs3k.server.web.component.CompetitionListPanel
import org.apache.wicket.markup.html.panel.Panel

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-18
 * Time: 23:03
 * To change this template use File | Settings | File Templates.
 */

object BreadCrumbPanel {
  trait BreadCrumbModel extends Serializable {

    val createComponent: (String,Model[_]) => Component
    val model:Model[_]
    val name:String

  }
}
import BreadCrumbPanel._

abstract class BreadCrumbPanel(id:String, rootBreadCrumbModel:BreadCrumbModel) extends Panel(id) {

  import scala.collection.JavaConversions.asScalaBuffer
  import scala.collection.JavaConversions.seqAsJavaList

  var crumbItems = new java.util.ArrayList[BreadCrumbModel]()
  var itemsModel = new Model(crumbItems)

  var breadCrumbContainer = new WebMarkupContainer("breadCrumbContainer")
  var contentContainer = new WebMarkupContainer("contentContainer")

  override def onEvent(event: IEvent[_]) {
    super.onEvent(event)
    println("Receiving event")
    val pl = event.getPayload
    pa.lift.apply(pl).foreach { case (newItem,target,level) =>

        //var level = 1
        var list = itemsModel.getObject.take(level)
        list = list :+ newItem
        itemsModel.setObject(new java.util.ArrayList(list.toList))
        target.add(breadCrumbContainer)
        target.add(contentContainer)
    }
  }

  /*

   */

  def pa:PartialFunction[Any, (BreadCrumbModel,AjaxRequestTarget,Int)]


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


  crumbItems.add(rootBreadCrumbModel)

  add(contentContainer)
  contentContainer.setOutputMarkupId(true)

}
