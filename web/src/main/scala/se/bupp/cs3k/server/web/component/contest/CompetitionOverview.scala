package se.bupp.cs3k.server.web.component.contest

import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.{LoadableDetachableModel, IModel}
import se.bupp.cs3k.server.model.{HasNewsItemFields, GameResult, Competition}
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.web.WiaSession
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.MarkupContainer
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import se.bupp.cs3k.server.service.{GameNewsService, CompetitionService, ResultService}
import org.apache.wicket.spring.injection.annot.SpringBean
import org.apache.wicket.ajax.markup.html.AjaxLink
import se.bupp.cs3k.server.web.auth.{AnonymousOnly, LoggedInOnly}
import org.apache.wicket.markup.html.WebMarkupContainer
import se.bupp.cs3k.model.CompetitionState
import org.apache.wicket.markup.html.link.BookmarkablePageLink
import se.bupp.cs3k.server.web.page.SigninPage
import org.joda.time.Instant
import se.bupp.cs3k.server.service.dao.NewsItemDao
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import se.bupp.cs3k.server.web.component.{NewsItemList}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-07
 * Time: 01:22
 * To change this template use File | Settings | File Templates.
 */
class CompetitionOverview(cId:String ,modl:IModel[Competition]) extends Panel(cId){

  @SpringBean
  var resultService:ResultService = _

  @SpringBean
  var competitionService:CompetitionService = _

  @SpringBean
  var gameNewsService:GameNewsService = _

  @SpringBean
  var newsItemDao:NewsItemDao = _

  @LoggedInOnly
  class COJoinLadderPanel(id:String, m:IModel[Competition]) extends JoinCompetitionPanel(id,m) {
    override def isVisible = super.isVisible && List(CompetitionState.SIGNUP).contains(modl.getObject.state)
    def onUpdate(t: AjaxRequestTarget) {

    }
  }
  @AnonymousOnly
  class LoginToJoin(id:String) extends WebMarkupContainer(id) {
    override def isVisible = super.isVisible && List(CompetitionState.SIGNUP).contains(modl.getObject.state)

    add(new BookmarkablePageLink[String]("login",classOf[SigninPage]))
  }

  add(new COJoinLadderPanel("joinPanel",modl))
  add(new LoginToJoin("loginToJoin"))

  import scala.collection.JavaConversions.seqAsJavaList
  var all  = new ListModel(resultService.findByCompetition(modl.getObject))

  add(new ListView("lastGames", all) {
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
  })

  import scala.collection.JavaConversions.seqAsJavaList

  val provider = new SortableDataProvider[HasNewsItemFields,String]() {
    import scala.collection.JavaConversions.asJavaIterator

    def iterator(p1: Long, p2: Long) =
      newsItemDao.findByCompetition(modl.getObject,gameNewsService.getDefaultInterval(new Instant()),Range(p1.toInt,p2.toInt)).toIterator

    def size() = newsItemDao.findByCompetitionCount(modl.getObject,gameNewsService.getDefaultInterval(new Instant()))

    def model(p1: HasNewsItemFields) = new LoadableDetachableModel[HasNewsItemFields](p1) {
      def load() = newsItemDao.find(p1.id).get
    }
  }
  //val listModel = new ListModel[HasNewsItemFields](newsItemDao.find(modl.getObject,new Instant))
  add(new NewsItemList("news",provider))


  add(new AjaxLink("startGameDebug") {

    def onClick(p1: AjaxRequestTarget) {
      var ladder = modl.getObject

      competitionService.startCompetition(ladder)
    }
  })

}
