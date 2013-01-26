package se.bupp.cs3k.server.facade

import se.bupp.cs3k.api._
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import java.rmi.server.UnicastRemoteObject
import java.lang.Integer
import java.lang.{Integer => JInt,Long => JLong}
import org.apache.log4j.Logger
import java.rmi.RemoteException
import org.springframework.beans.factory.annotation.Autowired
import user.{AbstractPlayerIdentifier, RegisteredPlayerIdentifier, PlayerIdentifierWithInfo, AnonymousPlayerIdentifier}
import se.bupp.cs3k.server.service.GameReservationService
import se.bupp.cs3k.server.service.GameReservationService._
import java.lang
import se.bupp.cs3k.server.service.dao.{GameDao, GameResultDao, UserDao}
import org.springframework.transaction.annotation.Transactional
import se.bupp.cs3k.server.model.GameResult
import scala.Some


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 21:30
 * To change this template use File | Settings | File Templates.
 */
@Service("gameServerFacade")
class GameServerFacadeImpl() extends GameServerFacadeRemote with GameServerFacade {

  val log = Logger.getLogger(classOf[GameServerFacadeImpl])

  val om = new ObjectMapper()


  @Autowired
  var reservationService:GameReservationService = _

  /*@Autowired
  var ticketDao:TicketDao = _*/

  @Autowired
  var gameResultDao:GameResultDao = _

  @Autowired
  var gameDao:GameDao = _

  @Autowired
  var userDao:UserDao = _

  private def getSimplePlayerInfo(pi:AbstractPlayerIdentifier) = {
    pi match {
      case ru:RegisteredPlayerIdentifier => userDao.findUser(ru.getUserId).map(p=> new PlayerIdentifierWithInfo(p.username,p.id))
      case api:AnonymousPlayerIdentifier => Some(api)
    }
  }


  @throws(classOf[java.rmi.RemoteException])
  def evaluateGamePass(pass: String, gameSessionId: lang.Long) : SimplePlayerInfo = {
    try {
      log.info("evaluateGamePass invoked")
      var absPI = om.readValue(pass, classOf[AbstractGamePass])
      log.info("evaluateGamePass invoked" + absPI)
      val r = absPI match {
        case t:GateGamePass =>
          val l = reservationService.findInMemoryReservation(t.getReservationId).flatMap { case (o,map) =>
            val piOpt = if (gameSessionId == o) {
              map.find { case (seat, pi) => seat == t.getReservationId }.map( p => p._2 )
            } else None
            piOpt.flatMap( p => getSimplePlayerInfo(p))
          }
          l
        /*case t:Ticket => val l = ticketDao.findTicket(t.getId).map( tt => new PlayerIdentifierWithInfo(tt.user.username,tt.user.id))
          l*/
        case t:IdentifyOnlyPass =>
          getSimplePlayerInfo(t.getUserIdentifier)
      }
      r.getOrElse(null)
    } catch {
      case e:Exception => e.printStackTrace()
      throw new RemoteException("Tjenare")
    }

  }

  def startGame(gameSessionId: JLong, teamsByBlayers: java.util.Map[JInt, JInt]) {
    log.info("StartGame invoked")
  }

  def startGame(gameSessionId: JLong, players: java.util.List[JInt]) {

  }

  @Transactional
  def endGame(gameSessionId: JLong, serializedResult: String) {
    log.info("EndGame invoked  : " + serializedResult)
    log.info("GAMES IN ENDGAME " + gameDao.findAll.mkString(","))
    reservationService.findByGameSessionId(gameSessionId) match {
      case Some(g) =>
        g.result = new GameResult(1, serializedResult)
        g.result.game = g
        gameResultDao.insert(g.result)
        gameDao.update(g)

      case None =>
        log.info("Game Not found " + gameSessionId + " " + serializedResult)
    }
  }
}
