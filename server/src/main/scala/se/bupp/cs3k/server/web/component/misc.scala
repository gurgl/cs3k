package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.{FeedbackPanel, Panel}
import se.bupp.cs3k.server.model._
import org.apache.wicket.ajax.markup.html.AjaxLink
import se.bupp.cs3k.server.service.dao.{CompetitorDao, UserDao, TeamDao}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.web.{LoggedInOnly, WiaSession, WicketApplication}
import org.apache.wicket.ajax.AjaxRequestTarget
import se.bupp.cs3k.server.service.{LadderService, TeamService}
import org.apache.wicket.ajax.markup.html
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.{LoadableDetachableModel, AbstractReadOnlyModel}
import se.bupp.cs3k.server.model.Ladder
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.markup.repeater.data.{DataView, IDataProvider, ListDataProvider}
import org.apache.wicket.markup.repeater.Item
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-09
 * Time: 21:25
 * To change this template use File | Settings | File Templates.
 */

@LoggedInOnly
abstract class JoinTeamPanel(id:String, t:Team) extends Panel(id) {

  @SpringBean
  var ts:TeamService = _

  val fb = new FeedbackPanel("feedback")
  val label = new Label("label", new AbstractReadOnlyModel[String] {
    def getObject = {
      val u = WiaSession.get().getUser ;
      if(u != null && ts.isUserMemberOfTeam(u, t)) "leave" else "join"
    }
  })


  def onUpdate(t:AjaxRequestTarget)
  setOutputMarkupId(true)

  add(fb.setOutputMarkupId(true))
  val link = new AjaxLink[Team]("link") {

    def onClick(target: AjaxRequestTarget) {
      val a =
        if(!ts.isUserMemberOfTeam(WiaSession.get().getUser, t)) {
          ts.storeTeamMember(WiaSession.get().getUser, t)
          info("Joined team")
        } else {
          ts.leaveTeam(WiaSession.get().getUser, t)
          info("Left team")
        }

      JoinTeamPanel.this.onUpdate(target)

      target.add(JoinTeamPanel.this)
    }
  }

  link.add(label)
  add(link)
}



//@LoggedInOnly
abstract class JoinLadderPanel(id:String, t:Ladder) extends Panel(id) {

  @SpringBean
  var ts:LadderService = _

  @SpringBean
  var userDao:UserDao = _

  @SpringBean
  var competitorDao:CompetitorDao = _

  val fb = new FeedbackPanel("feedback")



  val log = LoggerFactory.getLogger(this.getClass)
  log.info("create JoinLadderPanel")

  def onUpdate(t:AjaxRequestTarget)
  setOutputMarkupId(true)

  add(fb.setOutputMarkupId(true))

  import scala.collection.JavaConversions.asJavaIterator

  val applicableProvider = new IDataProvider[Competitor]() {
    def detach() {}
    def iterator(p1: Long, p2: Long) = {
      log.info("ich vägra logga")
      ts.findApplicableCompetitors(WiaSession.get().getUser,t).take(p2.toInt).toIterator
    }
    def size() = ts.findApplicableCompetitors(WiaSession.get().getUser,t).size
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
          (if(ts.isCompetitorMemberOfLadder(comp, t)) "leave " else "join") + " as " + name
        }
      })

      val link = new AjaxLink[Ladder]("link") {

        def onClick(target: AjaxRequestTarget) {
          val a =
            if(!ts.isCompetitorMemberOfLadder(comp, t)) {
              ts.storeLadderMember(comp, t)
              info("Joined ladder")
            } else {
              ts.leaveLadder(comp, t)
              info("Left ladder")
            }

          JoinLadderPanel.this.onUpdate(target)

          target.add(JoinLadderPanel.this)
        }
      }
      link.add(label)
      item.add(link)
    }
  })


  val participantsProvider = new IDataProvider[Competitor]() {
    def detach() {}
    def iterator(p1: Long, p2: Long) = {
      log.info("ich vägra logga")
      competitorDao.findLadderParticipants(t, p1 ,p2).toIterator
    }
    def size() =       competitorDao.findLadderParticipantsCount(t)
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
          (if(ts.isCompetitorMemberOfLadder(comp, t)) "leave " else "join") + " as " + name
        }
      })
      */

      /*val link = new AjaxLink[Ladder]("link") {

        def onClick(target: AjaxRequestTarget) {
          val a =
            if(!ts.isCompetitorMemberOfLadder(comp, t)) {
              ts.storeLadderMember(comp, t)
              info("Joined ladder")
            } else {
              ts.leaveLadder(comp, t)
              info("Left ladder")
            }

          JoinLadderPanel.this.onUpdate(target)

          target.add(JoinLadderPanel.this)
        }
      }*/
      //link.add(label)
      item.add(new Label("label",new AbstractReadOnlyModel[String] {
        def getObject = comp match {
            case c:Team => c.name
            case c:User => c.username
          }
      }))
    }
  })



}