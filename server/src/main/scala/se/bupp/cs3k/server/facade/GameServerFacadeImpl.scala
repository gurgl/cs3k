package se.bupp.cs3k.server.facade

import se.bupp.cs3k.api._
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import java.rmi.server.UnicastRemoteObject
import java.lang.Integer
import java.lang.{Integer => JInt}
import org.apache.log4j.Logger
import java.rmi.RemoteException
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.server.web.MyBean
import user.{AbstractPlayerIdentifier, RegisteredPlayerIdentifier, PlayerIdentifierWithInfo, AnonymousPlayerIdentifier}
import se.bupp.cs3k.server.service.GameReservationService
import se.bupp.cs3k.server.service.GameReservationService._
import java.lang
import se.bupp.cs3k.server.service.dao.{TicketDao, UserDao}


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

  @Autowired
  var ticketDao:TicketDao = _

  @Autowired
  var userDao:UserDao = _

  private def getSimplePlayerInfo(pi:AbstractPlayerIdentifier) = {
    pi match {
      case ru:RegisteredPlayerIdentifier => userDao.findUser(ru.getUserId).map(p=> new PlayerIdentifierWithInfo(p.username,p.id))
      case api:AnonymousPlayerIdentifier => Some(api)
    }
  }


  @throws(classOf[java.rmi.RemoteException])
  def evaluateGamePass(pass: String, occassionId: lang.Long) : SimplePlayerInfo = {
    try {
      log.info("evaluateGamePass invoked")
      var absPI = om.readValue(pass, classOf[AbstractGamePass])
      log.info("evaluateGamePass invoked" + absPI)
      val r = absPI match {
        case t:GateGamePass =>
          val l = reservationService.findReservation(t.getReservationId).flatMap { case (o,map) =>
            val piOpt = if (occassionId == o) {
              map.find { case (seat, pi) => seat == t.getReservationId }.map( p => p._2 )
            } else None
            piOpt.flatMap( p => getSimplePlayerInfo(p))
          }
          l
        case t:Ticket => val l = ticketDao.findTicket(t.getId).map( tt => new PlayerIdentifierWithInfo(tt.user.username,tt.user.id))
          l
        case t:IdentifyOnlyPass =>
          getSimplePlayerInfo(t.getUserIdentifier)
      }
      r.getOrElse(null)
    } catch {
      case e:Exception => e.printStackTrace()
      throw new RemoteException("Tjenare")
    }

  }

  def startGame(occassionId: JInt, teamsByBlayers: java.util.Map[JInt, JInt]) {
    log.info("StartGame invoked")
  }

  def startGame(occassionId: JInt, players: java.util.List[JInt]) {

  }

  def endGame(occassionId: JInt, serializedScore: String) {
    log.info("EndGame invoked")

  }
}
