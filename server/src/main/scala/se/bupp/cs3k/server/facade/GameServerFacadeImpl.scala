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
import se.bupp.cs3k.server.service.{ResultService, GameReservationService}
import se.bupp.cs3k.server.service.GameReservationService._
import java.lang
import se.bupp.cs3k.server.service.dao._
import org.springframework.transaction.annotation.Transactional
import se.bupp.cs3k.server.model._
import se.bupp.cs3k.server.model.Model._
import java.io.File
import io.Source
import scala.Some
import se.bupp.cs3k.server.model.VirtualTeamRef
import scala.Some
import se.bupp.cs3k.server.model.RegedUser
import se.bupp.cs3k.server.model.TeamRef
import se.bupp.cs3k.server.model.AnonUser
import se.bupp.cs3k.example.ExampleScoreScheme.ExContestScore
import se.bupp.cs3k.example.ExampleScoreScheme


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
  var gameDao:GameOccassionDao = _

  @Autowired
  var userDao:UserDao = _

  @Autowired
  var teamDao:TeamDao = _

  @Autowired
  var gameResultService:ResultService = _


  /**
   * For virtual teams
   *
   * @param pi
   * @param teamOpt
   * @return
   */
  private def getSimplePlayerInfo(pi:AbstractPlayerIdentifier, reservationIdOpt:Option[GameServerReservationId], teamOpt:Option[TeamIdentifier]) = {

    val team = teamOpt.orNull

    val nullableReservationId:lang.Long = reservationIdOpt.map(new lang.Long(_)).orNull
    pi match {
      case ru:RegisteredPlayerIdentifier => userDao.findUser(ru.getUserId).map( p => new RegisteredPlayerIdentifierWithInfo(p.id, p.username, nullableReservationId, team))
      case api:AnonymousPlayerIdentifier => Some(new AnonymousPlayerIdentifierWithInfo(api.getName, nullableReservationId, team))
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
            case (user, teamOpt) =>
              val apiTeamOpt = (user, teamOpt) match {
                case (RegedUser(id),Some(TeamRef(teamId))) => Some(new TeamIdentifier(teamId, teamDao.find(teamId).get.name))
                case (AnonUser(id),Some(TeamRef(teamId))) => throw new IllegalStateException("blaj")
                case (u,Some(VirtualTeamRef(vtid,optName))) => Some(new TeamIdentifier(vtid,optName.orNull))
                case (u,None) => None
                case (_,_) => throw new IllegalStateException("impossible state")
              }
              val apiPlayerIdentifier = user match {
                case RegedUser(id) => new RegisteredPlayerIdentifier(id)
                case AnonUser(name) => new AnonymousPlayerIdentifier(name)
              }
              //apiPlayerIdentifier
              getSimplePlayerInfo(apiPlayerIdentifier, Some(t.getReservationId), apiTeamOpt)
          }
          pi
        /*case t:Ticket => val l = ticketDao.findTicket(t.getReportableId).map( tt => new RegisteredPlayerIdentifierWithInfo(tt.user.username,tt.user.id))
          l*/
        case t:IdentifyOnlyPass => getSimplePlayerInfo(t.getUserIdentifier, None, None)
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
   * @param gameSessionId
   * @param serializedResult
   */
  @Transactional
  def endGame(gameSessionId: JLong, serializedResult: String) {
    gameResultService.endGame(gameSessionId, serializedResult)
  }





}
