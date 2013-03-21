package se.bupp.cs3k.server.web.component.contest

import org.apache.wicket.model.{LoadableDetachableModel, IModel}
import se.bupp.cs3k.server.model.{User, Team, Competition}
import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.model.CompetitorType
import se.bupp.cs3k.server.web.component.competitor.PlayerListPanel
import se.bupp.cs3k.server.web.component.TeamListPanel
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.dao.{CompetitorDao, TeamDao}
import org.apache.wicket.Component

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-20
 * Time: 23:24
 * To change this template use File | Settings | File Templates.
 */
class CompetitionPariticipantListPanel(cId:String ,mod:IModel[Competition]) extends Panel(cId) {


  @SpringBean
  var competitorDao:CompetitorDao = _

  val p:Component = mod.getObject.competitorType match {
    case CompetitorType.INDIVIDUAL =>

      val prov = new SortableDataProvider[User,String]() {
        import scala.collection.JavaConversions.asJavaIterator
        def iterator(p1: Long, p2: Long) =
          competitorDao.findCompetitionParticipants(mod.getObject, p1.toInt,p2.toInt).map(_.asInstanceOf[User]).toIterator

        def size() = competitorDao.findCompetitionParticipantsCount(mod.getObject)

        def model(p1: User) = new LoadableDetachableModel[User](p1) {
          def load() = competitorDao.find(p1.id).get.asInstanceOf[User]

        }
      }

      new PlayerListPanel("partList",prov)
    case CompetitorType.TEAM =>
      val prov = new SortableDataProvider[Team,String]() {
        import scala.collection.JavaConversions.asJavaIterator
        def iterator(p1: Long, p2: Long) =
          competitorDao.findCompetitionParticipants(mod.getObject, p1.toInt,p2.toInt).map(_.asInstanceOf[Team]).toIterator

        def size() = competitorDao.findCompetitionParticipantsCount(mod.getObject)

        def model(p1: Team) = new LoadableDetachableModel[Team](p1) {
          def load() = competitorDao.find(p1.id).get.asInstanceOf[Team]

        }
      }
      new TeamListPanel("partList",prov)
  }

  add(p);
}
