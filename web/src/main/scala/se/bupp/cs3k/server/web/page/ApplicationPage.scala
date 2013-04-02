package se.bupp.cs3k.server.web.page

import org.apache.wicket.markup.html.{WebMarkupContainer, link}
import link.BookmarkablePageLink
import org.apache.wicket.ajax.{AjaxSelfUpdatingTimerBehavior, AjaxRequestTarget}
import org.apache.wicket.markup.html.panel.{EmptyPanel, Panel}
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.markup.html.basic.Label

import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.markup.repeater.{Item, RefreshingView}
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.model.{Model, AbstractReadOnlyModel}
import org.apache.wicket.model.util.ListModel
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter
import se.bupp.cs3k.server.web._
import application.WiaSession
import auth.{LoggedInOnly, AnonymousOnly}
import component._
import competitor.CompetitorPanel
import competitor.player.PlayerPanel
import contest.{Events, ContestsPanel}
import scala.Some
import scala.Some
import org.apache.wicket.event.IEvent
import org.apache.wicket.Component
import org.apache.wicket.util.time.Duration
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.NewsItemDao
import se.bupp.cs3k.server.service.GameNewsService
import org.joda.time.Instant
import java.lang.Long


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-14
 * Time: 04:39
 * To change this template use File | Settings | File Templates.
 */

class ApplicationPage extends SessionPage {

  @SpringBean
  var gameNewsDao:NewsItemDao = _

  var MENU_ITEM_CONTEST_TEXT: String = "Contests"
  val MENU_ITEM_COMPETITORS_TEXT: String = "Competitors"
  var MENU_ITEM_ME_TEXT: String = "Me"
  val CONTENT_ID: String = "content"
  import scala.collection.JavaConversions.seqAsJavaList
  import scala.collection.JavaConversions.asScalaIterator
  import scala.collection.JavaConversions.asJavaIterator

  var notifierContainer: WebMarkupContainer = new WebMarkupContainer("notificationNotifier") {
    override def onBeforeRender() {
      super.onBeforeRender()

    }
  }
  notifierContainer.addOrReplace(new EmptyPanel("notifier").setVisible(false))

  var meMarkupId = Option.empty[String]
  notifierContainer.setOutputMarkupId(true)
  notifierContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(10)) {
    override def onPostProcessTarget(target: AjaxRequestTarget) {
      super.onPostProcessTarget(target)

      val doShow = Option(WiaSession.get().getUser) match {
        case Some(u) =>
          var count: Long = gameNewsDao.findUnreadByUserCount(u, new GameNewsService().getDefaultInterval(new Instant()))
          if(count > 0)Some(count) else None
        case None => None
      }
      val comp = doShow match {
        case Some(c) => new Label("notifier",c.toString).setVisible(true)

        case None => new EmptyPanel("notifier").setVisible(false)
      }
      notifierContainer.addOrReplace(comp)

      meMarkupId.foreach( mark =>
        target.appendJavaScript(s"""
          var notifier = $$('#${notifierContainer.getMarkupId}');
          var tab = $$('#$mark');

          notifier.offset({left:tab.offset().left+tab.outerWidth(),top:tab.offset().top});
          notifier.show('slow');
          """)
      )
    }
  })

  add(notifierContainer)

  override def onEvent(event: IEvent[_]) {
    super.onEvent(event)
    event.getPayload match {
      case e:Events.AbstractContestEvent =>
        var p = new ContestsPanel(CONTENT_ID, new Model(Some(e)))
        val newP = menuPanel.getItems.find(i => i.getModelObject._1 == MENU_ITEM_CONTEST_TEXT).get
        asdf(e.target,MENU_ITEM_CONTEST_TEXT,p,Some(newP))

      case e:Events.AbstractCompetitorEvent =>
        var p = new CompetitorPanel(CONTENT_ID, new Model(Some(e)))
        val newP = menuPanel.getItems.find(i => i.getModelObject._1 == MENU_ITEM_COMPETITORS_TEXT).get
        asdf(e.target,MENU_ITEM_COMPETITORS_TEXT,p,Some(newP))
      case _ =>
    }
  }

  val list = {


    List(
      ("Play", (s: String) => new HomePanel(s)),
      (MENU_ITEM_CONTEST_TEXT, (s: String) => new ContestsPanel(s, new Model(None))),
      (MENU_ITEM_COMPETITORS_TEXT, (s: String) => new CompetitorPanel(s, new Model(None)))
    ) ++ (
      if (WiaSession.get().getUser != null) {

        List((MENU_ITEM_ME_TEXT, (s: String) => new PlayerPanel(s,new Model(WiaSession.get().getUser))))
      } else Nil
      )
  }


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

          val comp = panelGen.apply(CONTENT_ID)

          asdf(target, labelText, comp, Some(p1))
        }
      }

      link.add(label)

      //var container: WebMarkupContainer = new WebMarkupContainer("item")
      p1.add(new AttributeAppender("class",new AbstractReadOnlyModel[String] {
        def getObject = if(selected.exists(_ == labelText)) "active" else ""
      }))

      import scala.collection.JavaConversions.asScalaBuffer
      if (labelText == MENU_ITEM_ME_TEXT) {
        //notifierContainer.remove(notifierContainer.getBehaviors:_*)
        meMarkupId = Some(p1.getMarkupId)
      }
      p1.add(link)

    }
  }

  def asdf(target: AjaxRequestTarget, labelText: String, comp: Panel, p1: Option[Component]) {
    selected.foreach(s => menuPanel.getItems.find(i => i.getModelObject._1 == s).foreach(i => target.add(i)))
    selected = Some(labelText)
    contentContainer.addOrReplace(comp)
    target.add(contentContainer)
    p1.foreach(target.add(_))
  }

  add(menuPanel)


  var contentContainer= new WebMarkupContainer("contentContainer")
  contentContainer.setOutputMarkupId(true)
  add(contentContainer)
  contentContainer.add(new HomePanel("content"))

}
