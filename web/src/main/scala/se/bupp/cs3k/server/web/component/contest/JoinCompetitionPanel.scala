package se.bupp.cs3k.server.web.component.contest

import org.apache.wicket.model.{AbstractReadOnlyModel, LoadableDetachableModel, IModel}
import se.bupp.cs3k.server.model.{User, Team, Competitor, Competition}
import org.apache.wicket.markup.html.panel.{FeedbackPanel, Panel}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.{CompetitionService, GameReservationService}
import se.bupp.cs3k.server.service.dao.{CompetitorDao, UserDao}
import org.slf4j.LoggerFactory
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.repeater.data.{DataView, IDataProvider}
import se.bupp.cs3k.server.web.WiaSession
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.ajax.markup.html.AjaxLink

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-03
 * Time: 01:50
 * To change this template use File | Settings | File Templates.
 */
//@LoggedInOnly
abstract class JoinCompetitionPanel(id:String, m:IModel[Competition]) extends Panel(id) {

  @SpringBean
  var gameReservationService:GameReservationService = _

  @SpringBean
  var competitionService:CompetitionService = _

  @SpringBean
  var userDao:UserDao = _

  @SpringBean
  var competitorDao:CompetitorDao = _

  val fb = new FeedbackPanel("feedback")



  val log = LoggerFactory.getLogger(this.getClass)
  log.info("create JoinCompetitionPanel")

  def onUpdate(t:AjaxRequestTarget)
  setOutputMarkupId(true)

  add(fb.setOutputMarkupId(true))

  import scala.collection.JavaConversions.asJavaIterator

  val applicableProvider = new IDataProvider[Competitor]() {
    def detach() {}
    def iterator(p1: Long, p2: Long) = {
      log.info("ich vägra logga")
      val t = m.getObject
      competitionService.findApplicableCompetitors(WiaSession.get().getUser,t).take(p2.toInt).toIterator
    }
    def size() = {
      val t = m.getObject
      competitionService.findApplicableCompetitors(WiaSession.get().getUser,t).size
    }
    def model(p1: Competitor) = new LoadableDetachableModel[Competitor](p1) {
      def load() =  userDao.findCompetitor(p1.id).get
    }
  }


  add(new DataView[Competitor]("applicable",applicableProvider) {

    override def isVisible = getDataProvider.size() > 0

    def populateItem(item: Item[Competitor]) {

      val comp = item.getModelObject
      val label = new Label("label", new AbstractReadOnlyModel[String] {
        def getObject = {
          val name = comp match {
            case c:Team => "Team(" + c.name + ")"
            case c:User => "User(" + c.username + ")"
          }
          val t = m.getObject
          (if(competitionService.isCompetitorInCompetition(comp, t)) "leave " else "join") + " as " + name
        }
      })

      val link = new AjaxLink[Competition]("link") {

        def onClick(target: AjaxRequestTarget) {
          val t = m.getObject
          val a =
            if(!competitionService.isCompetitorInCompetition(comp, t)) {
              competitionService.addCompetitorToCompetition(comp, t)
              info("Joined ladder")
            } else {
              competitionService.leaveCompetition(comp, t)
              info("Left ladder")
            }

          JoinCompetitionPanel.this.onUpdate(target)

          target.add(JoinCompetitionPanel.this)
        }
      }
      link.add(label)
      item.add(link)
    }
  })





  //add(new LadderRankingView("g"))


}