package se.bupp.cs3k.server.service

import dao.{TeamDao, UserDao, GameOccassionDao, GameResultDao}
import se.bupp.cs3k.example.ExampleScoreScheme.ExContestScore
import se.bupp.cs3k.example.ExampleScoreScheme
import se.bupp.cs3k.server.model._
import se.bupp.cs3k.server.model.Model._
import org.springframework.stereotype.Service
import org.apache.wicket.spring.injection.annot.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.apache.log4j.Logger
import java.lang.Long


import se.bupp.cs3k.server.model.TeamRef
import scala.Some
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-23
 * Time: 23:09
 * To change this template use File | Settings | File Templates.
 */
@Service
class ResultService {

  val log = Logger.getLogger(this.getClass)

  val om = new ObjectMapper()


  @Autowired
  var reservationService:GameReservationService = _

  @Autowired
  var gameResultDao:GameResultDao = _

  @Autowired
  var gameDao:GameOccassionDao = _

  @Autowired
  var userDao:UserDao = _

  @Autowired
  var teamDao:TeamDao = _



  @Autowired
  var gameLog:ResultLogService = _



  @Transactional
  def getCompetitorsByName(gsDetached:GameResult) = {
    import scala.collection.JavaConversions.asScalaBuffer
    import scala.collection.JavaConversions.mapAsJavaMap

    val gs = gameResultDao.em.merge(gsDetached)


    val competitorsByName = gs.game.participants.map( p => (p.id.competitor.id -> p.id.competitor.nameAccessor)).toMap //.toSet[java.lang.Long]
    log.info("competitorsByName " + competitorsByName)
    competitorsByName
  }


  def transformToRenderableTeamGame(gameSessionId: GameSessionId, serializedResult: String, teams:List[AbstractTeamRef]) = {
    val resIdToLabelList = teams.map {
      case TeamRef(u) => teamDao.find(u).get ; (u.toLong,  "${teamId:" + u + "}" )
      case VirtualTeamRef(id, nameOpt) => (id, nameOpt.getOrElse("Fix me - insert list of anon members/pids"))
    } toMap
    val resIdToLabelMap = resIdToLabelList.map { case (k,v) => (new java.lang.Long(k),v) }
    var value: ExContestScore = om.readValue(serializedResult, classOf[ExContestScore])
    import scala.collection.JavaConversions.mapAsJavaMap
    val html = ExampleScoreScheme.ExScoreScheme.renderToHtml(value, resIdToLabelMap)
    gameLog.write(html)
  }

  def transformToRenderablePlayerGame(gameSessionId: GameSessionId, serializedResult: String, users:Map[GameServerReservationId, AbstractUser]) = {

    val resIdToLabelMap = users.mapValues {
      case RegedUser(u) => userDao.find(u).get ; "${playerId:" + u + "}"
      case AnonUser(name) => name
    } map { case (k,v) => (new java.lang.Long(k),v) }
    var value: ExContestScore = om.readValue(serializedResult, classOf[ExContestScore])
    import scala.collection.JavaConversions.mapAsJavaMap
    val html = ExampleScoreScheme.ExScoreScheme.renderToHtml(value, resIdToLabelMap)
    gameLog.write(html)

  }

  def endGame(gameSessionId: GameSessionId, serializedResult: String) {
    log.info("EndGame invoked  : " + serializedResult)
    log.info("GAMES IN ENDGAME " + gameDao.findAll.mkString(","))
    reservationService.findGameSession(gameSessionId).foreach { session =>

      val gameOccassionOpt = reservationService.findByGameSessionId(gameSessionId)
      // (Map[GameServerReservationId,(AbstractUser,Option[AbstractTeamRef])], TeamsDetailsOpt) =>
      session match {
        case (players, Some(teamsDetails)) =>
          val teamRefs = teamsDetails.collect { case t: TeamRef => t }
          if (teamRefs.size == teamsDetails.size && teamRefs.size >= 2) {
            gameOccassionOpt match {
              case Some(g) =>
                import scala.collection.JavaConversions.asScalaBuffer
                if (g.participants.forall ( part => teamRefs.exists( part.id.competitor.id == _.id ))) {
                  g.result = new GameResult(1, serializedResult)
                  g.result.game = g
                  gameResultDao.insert(g.result)
                  gameDao.update(g)
                } else {
                  throw new IllegalStateException("Game session doesnt point to " + teamRefs.map( _.id).mkString(","))
                }

              case None =>

                val res = transformToRenderableTeamGame(gameSessionId, serializedResult, teamsDetails)
                log.info("Game Not found " + gameSessionId + " " + serializedResult)
            }
          } else {
            // MIXED
            //val competitorsByName = getCompetitorsByName(gs)
            //var value: ExContestScore = om.readValue(serializedResult, classOf[ExContestScore])
            //val markup = "<table class=\"table table-striped\"><tbody>" + ExampleScoreScheme.ExScoreScheme.renderToHtml(value,competitorsByName) + "</tbody></table>"
            val res = transformToRenderableTeamGame(gameSessionId, serializedResult, teamsDetails)
          }

        case (players, None) => {
          val playersRefs = players.collect {
            case (pid, (u:RegedUser ,None)) => (pid, u)
            case (pid, (u ,Some(_))) => throw new IllegalStateException("None team game cannot have team info")
          }
          if (playersRefs.size == players.size && playersRefs.size >= 2) {
            gameOccassionOpt match {
              case Some(g) =>
                import scala.collection.JavaConversions.asScalaBuffer
                if (g.participants.forall ( part => playersRefs.exists { case (reservationId, RegedUser(id)) => part.id.competitor.id == id  })) {

                  val res2pidMap = playersRefs.map {
                    case (reservationId, RegedUser(pid)) => (new java.lang.Long(reservationId), new java.lang.Long(pid))
                  }

                  var value: ExContestScore = om.readValue(serializedResult, classOf[ExContestScore])

                  import scala.collection.JavaConversions.mapAsJavaMap
                  val transformedValue = value.transformCompetitor(res2pidMap)
                  g.result = new GameResult(1,om.writeValueAsString(transformedValue))
                  g.result.game = g
                  gameResultDao.insert(g.result)
                  gameDao.update(g)
                } else {
                  throw new IllegalStateException("Game session doesnt point to " + playersRefs.map{ case (pid, p) => p.id }.mkString(","))
                }

              case None =>

                // MIXED
                //val competitorsByName = getCompetitorsByName(gs)
                var value: ExContestScore = om.readValue(serializedResult, classOf[ExContestScore])
                //val markup = "<table class=\"table table-striped\"><tbody>" + ExampleScoreScheme.ExScoreScheme.renderToHtml(value,competitorsByName) + "</tbody></table>"
                val res = transformToRenderablePlayerGame(gameSessionId, serializedResult, players. map { case (pid, (u,None)) => (pid,u) } )
            }
          } else {
            // MIXED
            //val competitorsByName = getCompetitorsByName(gs)
            //var value: ExContestScore = om.readValue(serializedResult, classOf[ExContestScore])
            //val markup = "<table class=\"table table-striped\"><tbody>" + ExampleScoreScheme.ExScoreScheme.renderToHtml(value,competitorsByName) + "</tbody></table>"
            val res = transformToRenderablePlayerGame(gameSessionId, serializedResult,  players. map { case (pid, (u,None)) => (pid,u) } )
          }
          //val teamRefs = teamsDetails.collect { case t: TeamRef => t }
        }
      }
    }

  }
}
