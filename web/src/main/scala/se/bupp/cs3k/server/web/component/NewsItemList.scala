package se.bupp.cs3k.server.web.component

import contest.Events
import generic.table.navigation.simple.AjaxNavigationToolbarSimple
import generic.table.{AjaxNavigationToolbar}
import generic.{DateLabel, AjaxLinkLabel}
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
import org.apache.wicket.model.{IModel, Model}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.WebMarkupContainer

import se.bupp.cs3k.server.web.generic.datetime.{RelativeDateConverter, StyleDateConverter, DateConverter}
import java.util.Locale
import org.joda.time.format.DateTimeFormatter
import org.apache.wicket.event.Broadcast
import Events.{CompetitionSelectedEvent, CompetitorSelectedEvent, CreateTeamEvent}
import org.apache.wicket.extensions.markup.html.repeater.data.table.{DataTable, AbstractColumn, IColumn, DefaultDataTable}
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import org.apache.wicket.behavior.AttributeAppender


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

class NewsItemList[T <: HasNewsItemFields](id:String, m:SortableDataProvider[T,String]) extends Panel(id) {
    import NewsItemList._

  val dataConv = new RelativeDateConverter(true)

  @SpringBean
  var gameResultService:ResultService = _

  @transient var om = new ObjectMapper()

  val cols: List[_ <: IColumn[T, String]] = List(
    new AbstractColumn[T, String](new Model("blubb")) {
      def populateItem(item: Item[ICellPopulator[T]], p2: String, p3: IModel[T]) {
        val news: HasNewsItemFields = p3.getObject
        val component: Component = createMessage(news, p2)
        //container.add(component)
        item.add(component)


      }
    },
    new AbstractColumn[T, String](new Model("blubb")) {

      def populateItem(item: Item[ICellPopulator[T]], p2: String, p3: IModel[T]) {
        val news: HasNewsItemFields = p3.getObject
        //val container: WebMarkupContainer = new WebMarkupContainer("item")
        val label = new DateLabel(p2, new Model(news.dateTime), dataConv)
        //label.add(new AttributeAppender("class",new Model("small"),";"))
        label.add(new AttributeAppender("style",new Model("color:#aaa;font-size:11px;"),";"))
        item.add(label)
        item.add(new AttributeAppender("style",new Model("text-align:right;"),";"))
      }
    }
  )

  import scala.collection.JavaConversions.seqAsJavaList

  var table: DataTable[T, String] = new DataTable("lastGames", cols, m, 10)
  table.setOutputMarkupId(true);
  table.setVersioned(false);

  table.addBottomToolbar(new AjaxNavigationToolbarSimple(table));
  add(table)

  def createMessage(news: HasNewsItemFields, componentId: String): Component = {
    val component: Component = news.messageType match {
      case NewsItemType.USER_JOINED_TEAM | NewsItemType.USER_LEFT_TEAM =>

        val f = new Fragment(componentId, typeToFragment(news.messageType), NewsItemList.this)
        f.add(new AjaxLinkLabel("playerLink", new Model(news.competitor2.nameAccessor)) {
          def onClick(p1: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new CompetitorSelectedEvent(news.competitor2,p1));
          }
        })
        f.add(new AjaxLinkLabel("teamLink", new Model(news.competitor1.nameAccessor)) {
          def onClick(p1: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new CompetitorSelectedEvent(news.competitor1,p1));
          }
        })
        f

      case NewsItemType.COMPETITOR_JOINED_COMPETITION | NewsItemType.COMPETITOR_LEFT_COMPETITION =>
        val f = new Fragment(componentId, typeToFragment(news.messageType), NewsItemList.this)
        f.add(new AjaxLinkLabel("compLink", new Model(news.competitor1.nameAccessor)) {
          def onClick(p1: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new CompetitorSelectedEvent(news.competitor1,p1));
          }
        })
        f.add(new AjaxLinkLabel("contLink", new Model(news.competition.name)) {
          def onClick(p1: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new CompetitionSelectedEvent(news.competition,p1));
          }
        })
        f

      case NewsItemType.COMPETITOR_COMPETITION_GAME_VICTORY =>
        val f = new Fragment(componentId, typeToFragmentAndInstance(news.messageType, news.competition), NewsItemList.this)
        f.add(new AjaxLinkLabel("compLink", new Model(news.competitor1.nameAccessor)) {
          def onClick(p1: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new CompetitorSelectedEvent(news.competitor1,p1));
          }
        })
        f.add(new AjaxLinkLabel("contLink", new Model(news.competition.name)) {
          def onClick(p1: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new CompetitionSelectedEvent(news.competition,p1));
          }
        })
        f

      case NewsItemType.COMPETITION_STATE_CHANGE =>
        val f = new Fragment(componentId, typeToFragmentAndInstance(news.messageType, news.competition), NewsItemList.this)
        f.add(new AjaxLinkLabel("compLink", new Model(news.competition.name)) {
          def onClick(p1: AjaxRequestTarget) {
            send(getPage(), Broadcast.BREADTH, new CompetitionSelectedEvent(news.competition,p1));
          }
        })
        f.add(new Label("contState", new Model(news.competitionState.toString)))
        f
    }
    component
  }
}
