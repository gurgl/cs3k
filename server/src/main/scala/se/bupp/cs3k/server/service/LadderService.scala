package se.bupp.cs3k.server.service

import dao.{GameOccassionDao, CompetitorDao, LadderDao}
import org.springframework.stereotype.Service
import se.bupp.cs3k.server.model._
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.server.model.Ladder
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.model.CompetitorType
import org.slf4j.LoggerFactory
import se.bupp.cs3k.example.ExampleScoreScheme.{ExContestScore, ExScoreScheme, ExCompetitorScore}


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-09
 * Time: 22:11
 * To change this template use File | Settings | File Templates.
 */
@Service
class LadderService {

  val log = LoggerFactory.getLogger(this.getClass)

  @Autowired
  var ladderDao:LadderDao = _
  @Autowired
  var competitorDao:CompetitorDao = _

  @Autowired
  var gameDao:GameOccassionDao = _


  @Autowired
  var gameReservationService:GameReservationService = _


  @Transactional
  def isUserMemberOfLadder(t:Competitor, l:Ladder) = {
    import scala.collection.JavaConversions.asScalaBuffer

    val ll = ladderDao.em.merge(l)

    log.debug("ll.participants.size" + ll.participants.size)
    ll.participants.exists( p => p.id.competitor.id == t.id)
  }

  def findApplicableCompetitors(t:User, l:Ladder) = {
    val allCompetitorsByUser = competitorDao.findByUser(t)

    import scala.collection.JavaConversions.asScalaBuffer
    log.info("allCompetitorsByUser " + allCompetitorsByUser.size)
    allCompetitorsByUser.filter {
      case cc:Team => l.competitorType == CompetitorType.TEAM
      case cc:User => l.competitorType == CompetitorType.INDIVIDUAL
    }
  }

  def findParticipants(ladder:Ladder, p1: Long, p2: Long) = {
     competitorDao.findLadderParticipants(ladder,p1,p2)
  }

  @Transactional
  def isCompetitorMemberOfLadder(t:Competitor, l:Ladder) = {
    import scala.collection.JavaConversions.asScalaBuffer
    log.debug("isCompetitorMemberOfLadder")

    val ll = ladderDao.em.merge(l)

    log.debug("ll.participants.size" + ll.participants.size)
    ll.participants.exists( p => p.id.competitor.id == t.id)
  }

  @Transactional
  def storeLadderMember(u:Competitor, t:Ladder) = {

    val pk = new LadderEnrollmentPk



    pk.competitor = ladderDao.em.merge(u)
    pk.ladder = ladderDao.em.merge(t)

    val tm = new LadderEnrollment

    tm.id = pk

    ladderDao.em.persist(tm)
  }

  @Transactional
  def leaveLadder(u:Competitor, t:Ladder) = {

    val pk = new LadderEnrollmentPk
    pk.competitor = ladderDao.em.merge(u)
    pk.ladder = ladderDao.em.merge(t)

    val tm = ladderDao.em.find(classOf[LadderEnrollment],pk)

    ladderDao.em.remove(tm)
  }
  @Autowired
  var resultService:ResultService = _

  def decoratePariticpantResults(participants:List[Competitor], ladder:Ladder) = {
    //val participants = competitorDao.findLadderParticipants(ladder, start, stop)
    val results = ladderDao.findLadderResults(ladder)
    var resultMap = results.groupBy(_._1).map { case (k,v) => (k, v.map(_._2))}

    participants.map { p =>
      resultMap.get(p) match {
        case Some(l) =>
          val total = resultService.getParticipantResult(p,l)
        Pair(p,Some(total))
        case None => (p,None)
      }

    }


  }

}
