package se.bupp.cs3k.server.web.component.contest

import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.model.GameResult
import org.apache.wicket.markup.html.panel.Panel
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.MarkupContainer
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.ResultService
import org.apache.wicket.model.IModel
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.event.Broadcast
import Events.CreateLadderEvent

/**
  * Created with IntelliJ IDEA.
  * User: karlw
  * Date: 2013-03-08
  * Time: 03:01
  * To change this template use File | Settings | File Templates.
  */
class CompetitionMainListPanel(id:String) extends Panel(id) {

   @SpringBean
   var gameResultService:ResultService = _

   add(new CompetitionListPanel("compList"))

  add(new AjaxLink("createLadderLink") {
    def onClick(target: AjaxRequestTarget) {
      send(getPage(), Broadcast.BREADTH, new CreateLadderEvent(target));
    }
  })

 }
