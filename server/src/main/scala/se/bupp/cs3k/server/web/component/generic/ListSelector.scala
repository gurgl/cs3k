package se.bupp.cs3k.server.web.component.generic

import org.apache.wicket.markup.repeater.data.{IDataProvider, DataView}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.model.AbstractReadOnlyModel
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.model.Same

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-08
 * Time: 20:42
 * To change this template use File | Settings | File Templates.
 */
abstract class ListSelector[G, T <: Same[G]](id:String, provider:IDataProvider[T]) extends Panel(id) {


  var selected = Option.empty[T]
  add(new DataView[T]("list",provider) {

    def populateItem(p1: Item[T]) {

      p1.setOutputMarkupId(true)
      val label = new Label("label",renderItem(p1.getModelObject)).add(new AttributeAppender("class",new AbstractReadOnlyModel[String] {
        def getObject = if(selected.exists(_.isSame(p1.getModelObject))) "bold" else ""
      }))

      import scala.collection.JavaConversions.asScalaIterator
      val link = new AjaxLink("link") {
        def onClick(target: AjaxRequestTarget) {
          selected.foreach (l => getItems.find(_.getModelObject.isSame(l)).foreach ( c => {target.add(c)  }))
          selected = Some(p1.getModelObject)


          ListSelector.this.onClick(target, p1.getModelObject)
          target.add(p1)

        }
      }
      link.add(label)
      p1.add(link)

    }
  })

  def onClick(target: AjaxRequestTarget, modelObject:T)
  def renderItem(t:T) : String
}
