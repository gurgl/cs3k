package se.bupp.cs3k.server.service

import dao.{TicketDao, GameDao, CompetitorDao, LadderDao}
import org.springframework.stereotype.Service
import se.bupp.cs3k.server.model._
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.server.model.Ladder
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.model.CompetitorType
import org.slf4j.LoggerFactory


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
  var gameDao:GameDao = _

  @Autowired
  var ticketDao:TicketDao = _


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

  @Transactional
  def challangeCompetitor(challanger:Competitor, challangee:Competitor) {
    (challanger, challangee) match {
      case (u1:User, u2:User) =>

        val occasionId = gameReservationService.allocateOccassion()
        val go = new GameOccassion(occasionId)
        val gp1 = new GameParticipation(new GameParticipationPk(u1,go))
        val gp2 = new GameParticipation(new GameParticipationPk(u1,go))
        go.participants.add(gp1)
        go.participants.add(gp2)
        gameDao.insert(go)

        val t = new Ticket()
        t.game = go
        t.user = u1
        val t2 = new Ticket()
        t2.game = go
        t2.user = u2
        ticketDao.insert(t)
        ticketDao.insert(t2)


      case _ => throw new IllegalStateException("Not implemented")
    }
  }
}
