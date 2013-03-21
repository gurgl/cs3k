package se.bupp.cs3k.server.web.component

import generic.breadcrumb.BreadCrumbPanel
import generic.breadcrumb.BreadCrumbPanel.BreadCrumbModel
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.{LoadableDetachableModel, Model}
import se.bupp.cs3k.server.model.{Team, Ladder}
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label
import se.bupp.cs3k.server.web.component.Events.{AbstractCompetitorEvent, AbstractContestEvent, CreateTeamEvent, TeamSelectedEvent}
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.event.{IEvent, Broadcast}
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.TeamDao

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-18
 * Time: 23:48
 * To change this template use File | Settings | File Templates.
 */


class CompetitorPanel(id:String,eventOpt:Model[Option[AbstractCompetitorEvent]]) extends Panel(id) {

  @SpringBean
  var teamDao:TeamDao = _

  val allTeamsProvider = new SortableDataProvider[Team,String]() {
    import scala.collection.JavaConversions.asJavaIterator
    def iterator(p1: Long, p2: Long) = teamDao.selectRange(p1.toInt,p2.toInt).toIterator

    def size() = teamDao.selectRangeCount

    def model(p1: Team) = new LoadableDetachableModel[Team](p1) {
      def load() = teamDao.find(p1.id).get

    }
    //def detach() {}


  }


  val panel = new BreadCrumbPanel("breadCrumbPanel",new BreadCrumbModel {
    val name = "Competitors"
    val model = new Model[Team](null)
    val createComponent = (id:String, m:Model[_]) => {
      new TeamListPanel(id,allTeamsProvider)
    }
  }) {
    override def pa:PartialFunction[Any,(BreadCrumbModel,AjaxRequestTarget,Int)] = {
      case lse:TeamSelectedEvent =>
        println("Receiving event spec")
        val newItem = new BreadCrumbModel() {
          val name = lse.team.name
          val model = new Model[Team](lse.team)
          val createComponent = (id:String, m:Model[_]) => {
            var ladMod = m.asInstanceOf[Model[Team]]
            new TeamPanel(id, ladMod)
          }
        }
        (newItem,lse.target,1)

      case lse:CreateTeamEvent =>
        println("Receiving event spec")
        val newItem = new BreadCrumbModel() {
          val name = "Create Team"
          val model = new Model[String]("")
          val createComponent = (id:String, m:Model[_]) => {

            new TeamFormPanel(id, "Create Team")
          }
        }
        (newItem,lse.target,1)
    }
  }

  eventOpt.getObject.foreach(i => panel.onEvent(new IEvent[AbstractCompetitorEvent] {
    def getType = ???

    def dontBroadcastDeeper() {}

    def stop() {}

    def getPayload = i

    def getSource = ???
  }))
  add(panel)

}
