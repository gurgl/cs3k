package se.bupp.cs3k.server.service

import dao.TeamDao
import org.springframework.stereotype.Service
import se.bupp.cs3k.server.model.{TeamMember, TeamMemberPk, Team, User}
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-09
 * Time: 22:11
 * To change this template use File | Settings | File Templates.
 */
@Service
class TeamService {

  val log = LoggerFactory.getLogger(this.getClass)

  @Autowired
  var teamDao:TeamDao = _

  @Transactional
  def isUserMemberOfTeam(u:User, t:Team) = {
    import scala.collection.JavaConversions.asScalaBuffer

    val tt = teamDao.em.merge(t)

    log.debug(tt.members.size.toString)
    tt.members.exists( p => p.id.user.id == u.id)
  }

  @Transactional
  def storeTeamMember(u:User, t:Team) = {

    val pk = new TeamMemberPk



    pk.user = teamDao.em.merge(u)
    pk.team = teamDao.em.merge(t)

    val tm = new TeamMember

    tm.id = pk

    teamDao.em.persist(tm)
  }

  @Transactional
  def leaveTeam(u:User, t:Team) = {

    val pk = new TeamMemberPk
    pk.user = teamDao.em.merge(u)
    pk.team = teamDao.em.merge(t)

    val tm = teamDao.em.find(classOf[TeamMember],pk)

    teamDao.em.remove(tm)
  }
}
