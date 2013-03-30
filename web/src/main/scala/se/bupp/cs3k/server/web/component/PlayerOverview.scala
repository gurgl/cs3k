package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.service.{GameNewsService, ResultService, GameReservationService}
import org.apache.wicket.spring.injection.annot.SpringBean
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.model.{HasNewsItemFields, GameResult, User, GameOccassion}
import se.bupp.cs3k.server.web.auth.LoggedInOnly
import org.apache.wicket.markup.html.WebMarkupContainer
import se.bupp.cs3k.server.web.{WicketApplication, WiaSession}
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.request.resource.ResourceReference
import org.apache.wicket.markup.html.link.ResourceLink
import org.apache.wicket.MarkupContainer
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import org.apache.wicket.model.{IModel, Model}
import org.joda.time.Instant

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-05
 * Time: 23:36
 * To change this template use File | Settings | File Templates.
 */
class PlayerOverview(id:String, model:IModel[User]) extends Panel(id) {

  @SpringBean
  var gameReservationService:GameReservationService = _

  @SpringBean
  var resultService:ResultService = _

  @SpringBean
  var gameNewsService:GameNewsService = _


  import scala.collection.JavaConversions.seqAsJavaList
  var all  = new ListModel(resultService.findResultsByCompetitor(model.getObject))

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

  add(new TeamMembershipList("teams", model))

  add(new CompetitionParticipationList("compParticipation",model))

  val listModel = new ListModel[HasNewsItemFields](gameNewsService.getPlayerLatestMessages(model.getObject,new Instant))
  add(new NewsItemList("userNews",listModel));
}
