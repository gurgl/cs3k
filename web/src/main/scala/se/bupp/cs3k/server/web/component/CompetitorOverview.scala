package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.web.WiaSession
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.{GameNewsService, ResultService}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.{LoadableDetachableModel, IModel}
import se.bupp.cs3k.server.model.{NewsItem, UserNewsItem, HasNewsItemFields, Team}
import org.joda.time.Instant
import se.bupp.cs3k.server.service.dao.NewsItemDao
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-11
 * Time: 00:41
 * To change this template use File | Settings | File Templates.
 */
class CompetitorOverview(id:String, m:IModel[Team]) extends Panel(id) {

  @SpringBean
  var resultService:ResultService = _
  @SpringBean
  var gameNewsService:GameNewsService = _

  @SpringBean
  var newsItemDao:NewsItemDao = _


  import scala.collection.JavaConversions.seqAsJavaList

  var all  = new ListModel(resultService.findResultsByCompetitor(m.getObject))

  add(new GameResultList("lastGames", all))

  val provider = new SortableDataProvider[NewsItem,String]() {
    import scala.collection.JavaConversions.asJavaIterator

    def iterator(p1: Long, p2: Long) =
      newsItemDao.findByTeam(m.getObject,gameNewsService.getDefaultInterval(new Instant()),Range(p1.toInt,p2.toInt)).toIterator

    def size() = newsItemDao.findByTeamCount(m.getObject,gameNewsService.getDefaultInterval(new Instant()))

    def model(p1: NewsItem) = new LoadableDetachableModel[NewsItem](p1) {
      def load() = newsItemDao.find(p1.id).get
    }
  }
  //val listModel = new ListModel[HasNewsItemFields](gameNewsService.getTeamLatestMessages(m.getObject,new Instant))
  add(new NewsItemList("news",provider))


  add(new JoinTeamPanel("joinTeamPanel", m) {
    def onUpdate(t: AjaxRequestTarget) {}
  })

}
