package se.bupp.cs3k.server.web.component

import generic.AjaxLinkLabel
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.model._
import org.apache.wicket.markup.html.panel.{Fragment, Panel}
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.{Component, MarkupContainer}
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.ResultService
import se.bupp.cs3k.model.NewsItemType
import org.apache.wicket.model.Model
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.datetime.markup.html.basic.DateLabel


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-08
 * Time: 03:01
 * To change this template use File | Settings | File Templates.
 */
object NewsItemList {
  val typeToFragment = Map(
    NewsItemType.USER_JOINED_TEAM -> "playerJoinedTeam",
    NewsItemType.USER_LEFT_TEAM -> "playerLeftTeam",
    NewsItemType.COMPETITOR_JOINED_COMPETITION -> "competitorJoinedCompetition",
    NewsItemType.COMPETITOR_LEFT_COMPETITION -> "competitorLeftCompetition"

  )

  def typeToFragmentAndInstance(n:NewsItemType, t:Any) = (t,n) match {
    case (c:Tournament, NewsItemType.COMPETITOR_COMPETITION_GAME_VICTORY) => "tournamentVictory"
    case (c:Ladder, NewsItemType.COMPETITOR_COMPETITION_GAME_VICTORY) => "ladderVictory"
    case (c:Tournament, NewsItemType.COMPETITION_STATE_CHANGE) => "tournamentState"
    case (c:Ladder, NewsItemType.COMPETITION_STATE_CHANGE) => "ladderState"
  }
}
class NewsItemList(id:String, m:ListModel[HasNewsItemFields]) extends Panel(id) {
    import NewsItemList._

  @SpringBean
  var gameResultService:ResultService = _

  @transient var om = new ObjectMapper()
  add(new ListView("lastGames", m) {
    def populateItem(item: ListItem[HasNewsItemFields]) {

      val news: HasNewsItemFields = item.getModelObject
      var container: WebMarkupContainer = new WebMarkupContainer("item")
      val component: Component = createMessage(news, "newsItem")
      var label: Label = new Label("time", news.dateTime)

      container.add(label   )
        container.add(component)
      item.add(container)
    }
  })

  def createMessage(news: HasNewsItemFields, componentId: String): Component = {
    val component: Component = news.messageType match {
      case NewsItemType.USER_JOINED_TEAM | NewsItemType.USER_LEFT_TEAM =>

        val f = new Fragment(componentId, typeToFragment(news.messageType), NewsItemList.this)
        f.add(new AjaxLinkLabel("playerLink", new Model(news.competitor2.nameAccessor)) {
          def onClick(p1: AjaxRequestTarget) {}
        })
        f.add(new AjaxLinkLabel("teamLink", new Model(news.competitor1.nameAccessor)) {
          def onClick(p1: AjaxRequestTarget) {}
        })
        f

      case NewsItemType.COMPETITOR_JOINED_COMPETITION | NewsItemType.COMPETITOR_LEFT_COMPETITION =>
        val f = new Fragment(componentId, typeToFragment(news.messageType), NewsItemList.this)
        f.add(new AjaxLinkLabel("compLink", new Model(news.competitor1.nameAccessor)) {
          def onClick(p1: AjaxRequestTarget) {}
        })
        f.add(new AjaxLinkLabel("contLink", new Model(news.competition.name)) {
          def onClick(p1: AjaxRequestTarget) {}
        })
        f

      case NewsItemType.COMPETITOR_COMPETITION_GAME_VICTORY =>
        val f = new Fragment(componentId, typeToFragmentAndInstance(news.messageType, news.competition), NewsItemList.this)
        f.add(new AjaxLinkLabel("compLink", new Model(news.competitor1.nameAccessor)) {
          def onClick(p1: AjaxRequestTarget) {}
        })
        f.add(new AjaxLinkLabel("contLink", new Model(news.competition.name)) {
          def onClick(p1: AjaxRequestTarget) {}
        })
        f

      case NewsItemType.COMPETITION_STATE_CHANGE =>
        val f = new Fragment(componentId, typeToFragmentAndInstance(news.messageType, news.competition), NewsItemList.this)
        f.add(new AjaxLinkLabel("compLink", new Model(news.competition.name)) {
          def onClick(p1: AjaxRequestTarget) {}
        })
        f.add(new Label("contState", new Model(news.competitionState.toString)))
        f
    }
    component
  }
}
