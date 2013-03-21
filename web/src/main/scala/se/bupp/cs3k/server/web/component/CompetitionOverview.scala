package se.bupp.cs3k.server.web.component

import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.IModel
import se.bupp.cs3k.server.model.{GameResult, Competition}
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.web.WiaSession
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.MarkupContainer
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import se.bupp.cs3k.server.service.{CompetitionService, ResultService}
import org.apache.wicket.spring.injection.annot.SpringBean
import org.apache.wicket.ajax.markup.html.AjaxLink
import se.bupp.cs3k.server.web.auth.{AnonymousOnly, LoggedInOnly}
import org.apache.wicket.markup.html.WebMarkupContainer
import se.bupp.cs3k.model.CompetitionState
import org.apache.wicket.markup.html.link.BookmarkablePageLink
import se.bupp.cs3k.server.web.page.SigninPage

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-07
 * Time: 01:22
 * To change this template use File | Settings | File Templates.
 */
class CompetitionOverview(cId:String ,model:IModel[Competition]) extends Panel(cId){

  @SpringBean
  var resultService:ResultService = _

  @SpringBean
  var competitionService:CompetitionService = _

  @LoggedInOnly
  class COJoinLadderPanel(id:String, m:IModel[Competition]) extends JoinLadderPanel(id,m) {
    override def isVisible = super.isVisible && List(CompetitionState.SIGNUP).contains(model.getObject.state)
    def onUpdate(t: AjaxRequestTarget) {

    }
  }
  @AnonymousOnly
  class LoginToJoin(id:String) extends WebMarkupContainer(id) {
    override def isVisible = super.isVisible && List(CompetitionState.SIGNUP).contains(model.getObject.state)

    add(new BookmarkablePageLink[String]("login",classOf[SigninPage]))
  }

  add(new COJoinLadderPanel("joinPanel",model))
  add(new LoginToJoin("loginToJoin"))

  import scala.collection.JavaConversions.seqAsJavaList
  var all  = new ListModel(resultService.findByCompetition(model.getObject))

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

  add(new AjaxLink("startGameDebug") {

    def onClick(p1: AjaxRequestTarget) {
      var ladder = model.getObject

      competitionService.startCompetition(ladder)
    }
  })

}
