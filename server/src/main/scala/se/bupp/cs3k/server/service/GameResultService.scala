package se.bupp.cs3k.server.service

import dao.GameResultDao
import se.bupp.cs3k.example.ExampleScoreScheme.ExContestScore
import se.bupp.cs3k.example.ExampleScoreScheme
import se.bupp.cs3k.server.model.GameResult
import org.springframework.stereotype.Service
import org.apache.wicket.spring.injection.annot.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.apache.log4j.Logger
import java.lang.Long

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-23
 * Time: 23:09
 * To change this template use File | Settings | File Templates.
 */
@Service
class GameResultService {

  val log = Logger.getLogger(this.getClass)

  @Autowired
  var gameResultDao:GameResultDao = _

  @Autowired
  var reservationService:GameReservationService = _


  @Transactional
  def getCompetitorsByName(gsDetached:GameResult) = {
    import scala.collection.JavaConversions.asScalaBuffer
    import scala.collection.JavaConversions.mapAsJavaMap

    val gs = gameResultDao.em.merge(gsDetached)


    val competitorsByName = gs.game.participants.map( p => (p.id.competitor.id -> p.id.competitor.nameAccessor)).toMap //.toSet[java.lang.Long]
    log.info("competitorsByName " + competitorsByName)
    competitorsByName
  }


  def transformToRenderable(gameSessionId: GameReservationService.GameOccassionId, serializedResult: String) {

    reservationService.findGameSession(gameSessionId).foreach {
      case gameSessionReservations =>

    }
  }


}
