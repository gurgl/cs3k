package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.service.{ResultService, GameReservationService}
import org.apache.wicket.spring.injection.annot.SpringBean
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.model.{GameResult, User, GameOccassion}
import se.bupp.cs3k.server.web.auth.LoggedInOnly
import org.apache.wicket.markup.html.WebMarkupContainer
import se.bupp.cs3k.server.web.{WicketApplication, WiaSession}
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.request.resource.ResourceReference
import org.apache.wicket.markup.html.link.ResourceLink
import org.apache.wicket.MarkupContainer
import org.apache.wicket.markup.{ComponentTag, MarkupStream}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-05
 * Time: 23:36
 * To change this template use File | Settings | File Templates.
 */
class PlayerOverview(id:String) extends Panel(id) {

  @SpringBean
  var gameReservationService:GameReservationService = _

  @SpringBean
  var resultService:ResultService = _

  import scala.collection.JavaConversions.seqAsJavaList
  var all  = new ListModel(resultService.findResultsByUser(WiaSession.get().getUser))

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


  add(new ChallangePanel("challangePanel"))

  class ChallangePanel(id:String) extends WebMarkupContainer(id) {
    var user: User = WiaSession.get().getUser

    var challanges = new ListModel(gameReservationService.findUnplayedGamesForCompetitor(user))


    add(new ListView[GameOccassion]("challanges",challanges) {
      def populateItem(listItem: ListItem[GameOccassion]) {
        var go = listItem.getModelObject
        var parameters: PageParameters = new PageParameters()
        //parameters.add("competitor_id", selectionModel.getObject.id)
        parameters.add("user_id", user.id)
        parameters.add("game_occassion_id", go.id)


        val ref = new ResourceReference("bupp") {
          def getResource = WicketApplication.get.gameResource
        }
        listItem.add(new ResourceLink[String]("play", ref, parameters) {
          /*
                    def onClick() {
                      var parameters: PageParameters = new PageParameters()
                      //parameters.add("competitor_id", selectionModel.getObject.id)
                      parameters.add("user_id", user.id)
                      parameters.add("game_occassion_id", go.gameSessionId)

                      new RestartResponseException()
                      RequestCycle.get().replaceAllRequestHandlers(new ResourceRequestHandler(WicketApplication.get.gameResource, parameters))
                    }
            */
        })

      }
    })
  }

}
