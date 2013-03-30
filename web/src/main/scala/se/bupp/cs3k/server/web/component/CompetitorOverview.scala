package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.util.ListModel
import se.bupp.cs3k.server.web.WiaSession
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.{GameNewsService, ResultService}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.IModel
import se.bupp.cs3k.server.model.{HasNewsItemFields, Team}
import org.joda.time.Instant

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

  import scala.collection.JavaConversions.seqAsJavaList

  var all  = new ListModel(resultService.findResultsByCompetitor(m.getObject))

  add(new GameResultList("lastGames", all))

  val listModel = new ListModel[HasNewsItemFields](gameNewsService.getTeamLatestMessages(m.getObject,new Instant))
  add(new NewsItemList("news",listModel))


  add(new JoinTeamPanel("joinTeamPanel", m) {
    def onUpdate(t: AjaxRequestTarget) {}
  })

}
