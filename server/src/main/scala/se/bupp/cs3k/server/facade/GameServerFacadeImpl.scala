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
import user._
import se.bupp.cs3k.server.service.GameReservationService
import se.bupp.cs3k.server.service.GameReservationService._
import java.lang
import se.bupp.cs3k.server.service.dao._
import org.springframework.transaction.annotation.Transactional
import se.bupp.cs3k.server.model.{Team, AnonUser, RegedUser, GameResult}
import scala.Some
import scala.Some
import se.bupp.cs3k.server.model.RegedUser
import se.bupp.cs3k.server.model.AnonUser
import scala.Some
import se.bupp.cs3k.server.model.RegedUser
import se.bupp.cs3k.server.model.AnonUser
import scala.Some
import se.bupp.cs3k.server.model.RegedUser
import se.bupp.cs3k.server.model.AnonUser
import java.io.File
import io.Source


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

  @Autowired
  var competitorDao:TeamDao = _


  /**
   * For virtual teams
   *
   * @param pi
   * @param teamOpt
   * @return
   */
  private def getSimplePlayerInfo(pi:AbstractPlayerIdentifier, teamOpt:Option[Team]) = {

    val team = teamOpt.map( t => new TeamIdentifier(t.id, t.nameAccessor)).orNull

    pi match {
      case ru:RegisteredPlayerIdentifier => userDao.findUser(ru.getUserId).map( p => new RegisteredPlayerIdentifierWithInfo(p.id, p.username, team))
      case api:AnonymousPlayerIdentifier => Some(new AnonymousPlayerIdentifierWithInfo(api.getName,team))
    }
  }


  @throws(classOf[java.rmi.RemoteException])
  def evaluateGamePass(pass: String, gameSessionId: lang.Long) : SimplePlayerInfo = {
    try {
      log.info("evaluateGamePass invoked")
      var absGamePass = om.readValue(pass, classOf[AbstractGamePass])
      log.info("evaluateGamePass invoked" + absGamePass)
      val r = absGamePass match {
        case t:GateGamePass =>
          val pi = reservationService.findReservation(t.getReservationId, gameSessionId).flatMap {
            case (user,teamIdOpt) =>
                val apiPlayerIdentifier = user match {
                  case RegedUser(id) => new RegisteredPlayerIdentifier(id)
                  case AnonUser(name) => new AnonymousPlayerIdentifier(name)
                }
                apiPlayerIdentifier

                val teamOpt = teamIdOpt.flatMap( t => competitorDao.find(t) )
                getSimplePlayerInfo(apiPlayerIdentifier, teamOpt)
              //piOpt.flatMap( p => getSimplePlayerInfo(p))
          }
          pi
        /*case t:Ticket => val l = ticketDao.findTicket(t.getId).map( tt => new RegisteredPlayerIdentifierWithInfo(tt.user.username,tt.user.id))
          l*/
        case t:IdentifyOnlyPass => getSimplePlayerInfo(t.getUserIdentifier,None)
      }
      r.orNull
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

  /**
   *
   * @param gameSessionId
   * @param serializedResult
   */
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
        storeToDisk(gameSessionId, serializedResult)
        log.info("Game Not found " + gameSessionId + " " + serializedResult)
    }
  }
  def storeToDisk(gameSessionId: JLong, serializedResult: String) {
    reservationService.findGameSession(gameSessionId).foreach {
      case gameSessionReservations =>


    }
  }
}
