package se.bupp.cs3k.server.web.component.competitor.player

import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.service.{GameNewsService, ResultService, GameReservationService}
import org.apache.wicket.spring.injection.annot.SpringBean
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.model._
import se.bupp.cs3k.server.web.auth.LoggedInOnly
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.request.resource.ResourceReference
import org.apache.wicket.markup.html.link.ResourceLink
import org.apache.wicket.MarkupContainer
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import org.apache.wicket.model.{LoadableDetachableModel, IModel, Model}
import org.joda.time.Instant
import se.bupp.cs3k.server.service.dao.{UserNewsItemDao, NewsItemDao}
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.server.web.component.game.GameResultList
import se.bupp.cs3k.server.web.component.{NewsItemList, TeamMembershipList, PlayerOpenLobbiesPanel}
import se.bupp.cs3k.server.web.component.contest.CompetitionParticipationList

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-05
 * Time: 23:36
 * To change this template use File | Settings | File Templates.
 */
class PlayerOverview(id:String, modl:IModel[User]) extends Panel(id) {

  @SpringBean
  var gameReservationService:GameReservationService = _

  @SpringBean
  var resultService:ResultService = _

  @SpringBean
  var gameNewsService:GameNewsService = _

  @SpringBean
  var newsItemDao:NewsItemDao = _
  @SpringBean
  var userNewsItemDao:UserNewsItemDao = _

  import scala.collection.JavaConversions.seqAsJavaList
  var all  = new ListModel(resultService.findResultsByCompetitor(modl.getObject))

  /*add(new ListView("lastGames", all) {
    def populateItem(item: ListItem[GameResult]) {
      item.add(new MarkupContainer("item") {

        override def onComponentTagBody(markupStream: MarkupStream, openTag: ComponentTag) {
          super.onComponentTagBody(markupStream, openTag)


          val markup = resultService.renderResult(item.getModelObject)
          val response = getRequestCycle().getResponse();
          response.write(markup)
        }
      })
    }
  })*/

  add(new GameResultList("lastGames", all))


  add(new PlayerOpenLobbiesPanel("openLobbies"))

  add(new TeamMembershipList("teams", modl))

  add(new CompetitionParticipationList("compParticipation",modl))

  val provider = new SortableDataProvider[UserNewsItem,String]() {
    import scala.collection.JavaConversions.asJavaIterator

    def iterator(p1: Long, p2: Long) =
      newsItemDao.findByUser(modl.getObject, gameNewsService.getDefaultInterval(new Instant()),Range(p1.toInt,p2.toInt)).toIterator

    def size() = newsItemDao.findByUserCount(modl.getObject, gameNewsService.getDefaultInterval(new Instant()))

    def model(p1: UserNewsItem) = new LoadableDetachableModel[UserNewsItem](p1) {
      def load() = userNewsItemDao.find(p1.id).get
    }
  }
  //val listModel = new ListModel[HasNewsItemFields](gameNewsService.getPlayerLatestMessages(model.getObject,new Instant))
  add(new NewsItemList("userNews",provider));
}
