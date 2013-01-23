package se.bupp.cs3k.server.service

import dao.{TeamDao, UserDao, GameResultDao}
import se.bupp.cs3k.server.model.{Team, User, GameResult}
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-23
 * Time: 23:09
 * To change this template use File | Settings | File Templates.
 */
@Service
class CompetitorService {

  val log = Logger.getLogger(this.getClass)

  @Autowired
  var gameResultDao:GameResultDao = _


  @Autowired
  var userDao:UserDao = _
  @Autowired
  var teamDao:TeamDao = _


  @Transactional
  def createUser(name:String) = {
    val user = new User(name)
    userDao.insert(user)
  }

  @Transactional
  def createTeam(t:Team) = {
    teamDao.insert(t)
  }


  @Transactional
  def getCompetitorsByName(gsDetached:GameResult) = {
    import scala.collection.JavaConversions.asScalaBuffer

    val gs = gameResultDao.em.merge(gsDetached)


    val competitorsByName = gs.game.participants.map( p => (p.id.competitor.id -> p.id.competitor.nameAccessor)).toMap //.toSet[java.lang.Long]
    log.info("competitorsByName " + competitorsByName)
    competitorsByName
  }


}
