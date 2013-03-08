package se.bupp.cs3k.server.web.component

import org.apache.wicket.model.IModel
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.model.GameResult
import org.apache.wicket.markup.html.panel.Panel
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.MarkupContainer
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.ResultService

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-08
 * Time: 03:01
 * To change this template use File | Settings | File Templates.
 */
class GameResultList(id:String, m:ListModel[GameResult]) extends Panel(id) {

  @SpringBean
  var gameResultService:ResultService = _

  @transient var om = new ObjectMapper()
  add(new ListView("lastGames", m) {
    def populateItem(item: ListItem[GameResult]) {
      item.add(new MarkupContainer("item") {

        override def onComponentTagBody(markupStream: MarkupStream, openTag: ComponentTag) {
          super.onComponentTagBody(markupStream, openTag)
          var gs: GameResult = item.getModelObject

          val markup = gameResultService.renderResult(gs)
          val response = getRequestCycle().getResponse();
          response.write(markup)
        }
      })
    }
  })
}
