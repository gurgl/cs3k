package se.bupp.cs3k.server.web.page

import org.apache.wicket.markup.html.{WebMarkupContainer, link}
import link.BookmarkablePageLink
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.markup.html.basic.Label

import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.markup.repeater.{Item, RefreshingView}
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.model.{Model, AbstractReadOnlyModel}
import org.apache.wicket.model.util.ListModel
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter
import se.bupp.cs3k.server.web._
import auth.{LoggedInOnly, AnonymousOnly}
import component._
import scala.Some
import scala.Some


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-14
 * Time: 04:39
 * To change this template use File | Settings | File Templates.
 */

class ApplicationPage extends SessionPage {


  import scala.collection.JavaConversions.seqAsJavaList
  import scala.collection.JavaConversions.asScalaIterator
  import scala.collection.JavaConversions.asJavaIterator

  val list = List(
    ("Play", (s:String) => new PlayPanel(s)),
    ("Contests", (s:String) => new ContestsPanel(s)),
    ("Competitors", (s:String) => new CompetitorPanel(s))
  ) ++ (
    if (WiaSession.get().getUser != null) {
      List(("Me", (s:String) => new PlayerPanel(s)))
    } else Nil
  )

  var selected = Some(list.head._1)
  var menuPanel = new RefreshingView[(String,Function1[String,Panel])]("menu") {

    def getItemModels = new ModelIteratorAdapter[(String, (String) => Panel)](list.toIterator) {
      def model(p1: (String, (String) => Panel)) = new Model(p1)
    }

    def populateItem(p1: Item[(String, (String) => Panel)]) {

      val (labelText, panelGen) = p1.getModelObject
          p1.setOutputMarkupId(true)

      var label = new Label("linkLabel", labelText).setOutputMarkupId(true)
      val link = new AjaxLink[String]("link") {
        def onClick(target: AjaxRequestTarget) {
          selected.foreach ( s => getItems.find(i => i.getModelObject._1 == s).foreach(i => target.add(i)) )
          selected = Some(labelText)

          val comp = panelGen.apply("content")
          contentContainer.addOrReplace(comp)
          target.add(contentContainer)
          target.add(p1)
        }
      }

      link.add(label)

      //var container: WebMarkupContainer = new WebMarkupContainer("item")
      p1.add(new AttributeAppender("class",new AbstractReadOnlyModel[String] {
        def getObject = if(selected.exists(_ == labelText)) "active" else ""
      }))
      p1.add(link)

    }
  }
  add(menuPanel)


  var contentContainer= new WebMarkupContainer("contentContainer")
  contentContainer.setOutputMarkupId(true)
  add(contentContainer)
  contentContainer.add(new PlayPanel("content"))

}
