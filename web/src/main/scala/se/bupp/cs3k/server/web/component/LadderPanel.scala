package se.bupp.cs3k.server.web.component

import generic.VertTabbedPanel
import org.apache.wicket.markup.html.panel.{EmptyPanel, Panel}
import org.apache.wicket.model.{Model, IModel}
import se.bupp.cs3k.server.model.{Tournament, Competition, Ladder}
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.ajax.AjaxRequestTarget
import se.bupp.cs3k.model.CompetitionState
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.{CompetitionService, CompetitorService}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-16
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
class LadderPanel(id:String, model:IModel[Competition]) extends Panel(id) {

  @SpringBean
  var competitionService:CompetitionService = _

  add(new VertTabbedPanel("tab-panel",
    List(
      ("Overview", (cId:String) => new JoinLadderPanel(cId,model) {
      def onUpdate(t: AjaxRequestTarget) {

      }
      }),
      ("Challangers", (cId:String) => new TeamListPanel(cId))
    ) ++
      {
        model.getObject match {
          case l:Ladder =>
            val ladMod = model.asInstanceOf[IModel[Ladder]]
            List(
              ("Results", (cId:String) => new LadderStandingsPanel(cId, ladMod))
            )
          case t:Tournament =>
            if (List(CompetitionState.RUNNING, CompetitionState.FINISHED).contains(t.state)) {
              List(("Results", (cId:String) => new TournamentView(cId, model.asInstanceOf[IModel[Tournament]])))
            } else {

              List(("Results", (cId:String) => {
                val numOfPlayers = competitionService.getNumberOfParticipants(model.getObject)
                new TournamentViewNotStarted(cId, new Model(new Integer(numOfPlayers)))

                  }
                )
              )
            }
        }
      }
    )
  )
}
