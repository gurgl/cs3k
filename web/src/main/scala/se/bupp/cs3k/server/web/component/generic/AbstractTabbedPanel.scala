package se.bupp.cs3k.server.web.component.generic

import org.apache.wicket.markup.html.panel.{EmptyPanel, Panel}
import org.apache.wicket.markup.repeater.{Item, RefreshingView}
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter
import org.apache.wicket.model.{AbstractReadOnlyModel, Model}
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.Component


/**
  * Created with IntelliJ IDEA.
  * User: karlw
  * Date: 2013-02-21
  * Time: 23:55
  * To change this template use File | Settings | File Templates.
  */
class AbstractTabbedPanel(id:String, tabs:List[(String, String => Component)]) extends Panel(id) {

  import scala.collection.JavaConversions.seqAsJavaList
  import scala.collection.JavaConversions.asScalaIterator
  import scala.collection.JavaConversions.asJavaIterator

  var selected = Some(tabs.head)
  var menuPanel = new RefreshingView[(String,Function1[String,Component])]("menu") {

    def getItemModels = new ModelIteratorAdapter[(String, (String) => Component)](tabs.toIterator) {
      def model(p1: (String, (String) => Component)) = new Model(p1)
    }

    def populateItem(p1: Item[(String, (String) => Component)]) {

      val (labelText, panelGen) = p1.getModelObject
      p1.setOutputMarkupId(true)

      var label = new Label("linkLabel", labelText).setOutputMarkupId(true)
      val link = new AjaxLink[String]("link") {
        def onClick(target: AjaxRequestTarget) {
          selected.foreach ( s => getItems.find(i => i.getModelObject._1 == s._1).foreach(i => target.add(i)) )
          selected = Some(p1.getModelObject)

          val comp = panelGen.apply("content")
          contentContainer.addOrReplace(comp)
          target.add(contentContainer)
          target.add(p1)
        }
      }

      link.add(label)

      //var container: WebMarkupContainer = new WebMarkupContainer("item")
      p1.add(new AttributeAppender("class",new AbstractReadOnlyModel[String] {
        def getObject = if(selected.exists(_ == p1.getModelObject)) "active" else ""
      }))
      p1.add(link)

    }
  }
  add(menuPanel)

  var contentContainer= new WebMarkupContainer("contentContainer")
  contentContainer.setOutputMarkupId(true)
  add(contentContainer)
  contentContainer.add(selected.map( _._2("content")).getOrElse(new EmptyPanel("content")))

}
