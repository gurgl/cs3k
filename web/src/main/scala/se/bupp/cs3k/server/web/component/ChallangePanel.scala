package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.repeater.data.{DataView, IDataProvider}
import se.bupp.cs3k.server.model.{Competition, User, Team, Competitor}
import org.apache.wicket.model.{IModel, AbstractReadOnlyModel, LoadableDetachableModel}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.ajax.AjaxRequestTarget
import se.bupp.cs3k.server.web.WiaSession
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{CompetitorDao, UserDao}
import se.bupp.cs3k.server.service.GameReservationService
import org.apache.wicket.markup.html.panel.Panel
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-08
 * Time: 01:25
 * To change this template use File | Settings | File Templates.
 */
class ChallangePanel(id:String,m:IModel[Competition]) extends Panel(id) {

  @SpringBean
  var gameReservationService:GameReservationService = _

  @SpringBean
  var userDao:UserDao = _

  @SpringBean
  var competitorDao:CompetitorDao = _

  val log = LoggerFactory.getLogger(this.getClass)


  import scala.collection.JavaConversions.asJavaIterator
  val participantsProvider = new IDataProvider[Competitor]() {
    def detach() {}
    def iterator(p1: Long, p2: Long) = {
      log.info("ich vägra logga")
      val t = m.getObject
      competitorDao.findCompetitionParticipants(t, p1 ,p2).toIterator
    }
    def size() =  {
      val t = m.getObject
      competitorDao.findCompetitionParticipantsCount(t)
    }
    def model(p1: Competitor) = new LoadableDetachableModel[Competitor](p1) {
      def load() =  userDao.findCompetitor(p1.id).get
    }
  }

  add(new DataView[Competitor]("participants",participantsProvider) {
    def populateItem(item: Item[Competitor]) {

      val comp = item.getModelObject
      /*val label = new Label("label", new AbstractReadOnlyModel[String] {
        def getObject = {
          val name = comp match {
            case c:Team => "Team(" + c.name + ")"
            case c:User => "User(" + c.username + ")"
          }
          (if(ts.isCompetitorInCompetition(comp, t)) "leave " else "join") + " as " + name
        }
      })
      */

      /*val link = new AjaxLink[Ladder]("link") {

        def onClick(target: AjaxRequestTarget) {
          val a =
            if(!ts.isCompetitorInCompetition(comp, t)) {
              ts.addCompetitorToCompetition(comp, t)
              info("Joined ladder")
            } else {
              ts.leaveCompetition(comp, t)
              info("Left ladder")
            }

          JoinCompetitionPanel.this.onUpdate(target)

          target.add(JoinCompetitionPanel.this)
        }
      }*/
      //link.add(label)
      item.add(new Label("label",new AbstractReadOnlyModel[String] {
        def getObject = comp match {
          case c:Team => c.name
          case c:User => c.username
        }
      }))

      item.add(new AjaxLink("challangeLink") {
        def onClick(p1: AjaxRequestTarget) {
          // TODO fix me
          gameReservationService.challangeCompetitor(WiaSession.get().getUser, comp, null)
        }
      })
    }
  })
}
