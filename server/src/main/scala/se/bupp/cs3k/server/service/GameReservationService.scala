package se.bupp.cs3k.server.service

import org.springframework.stereotype.Component
import se.bupp.cs3k.server.model.{NonPersisentGameOccassion, RunningGame, Ticket, GameOccassion}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.web.MyBean
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.api.user.{RegisteredPlayerIdentifier, AnonymousPlayerIdentifier, AbstractPlayerIdentifier}
import se.bupp.cs3k.api.{GateGamePass, IdentifyOnlyPass, AbstractGamePass}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-14
 * Time: 18:52
 * To change this template use File | Settings | File Templates.
 */

/**
 * SÄTT ATT SPELA :
 *
 * Schmemaläggning
 * Utmaning
 * Continous / unorganized
 * Matchmaking
 *  -> anonym
 *  -> registrerad
 *
 *
 * Schmemaläggning
 * Utmaning
 * Play now
 *
 * GAME SERVER LAUNCH EVENT:
 *
 * Schema: Tid
 * Utmaning: Utmaning accepterad
 * Play Now: Lobby #Players needed met
 *
 *
 *
 * Play Now ->
 * 1. Stå i lobby kö (Med namn/playerId)
 * 2. Server skapas, reservations id'n skapas
 * 3. Lobby svarar med reservations id
 * 4. Klient laddar spel med reservationsid som parameter
 *
 *
 *
 * ???? _* Anonym ... tillräckligt många =
 *
 *
 * Sätt att starta klient (server info implicit)
 *
 * - Med reservationsid
 * - Med name
 *
 * Schmemaläggning : reservationsId
 * Utmaning : reservationsId
 * Continous / unorganized : namn
 * Matchmaking reg
 * Matchmaking anon
 *
 */

object GameReservationService {
  type OccassionId = Long
  type SeatId = Long
  var occassionSeqId:Long = 1L
  var seatSeqId:Long= 1L
  var openOccassions = collection.mutable.Map[OccassionId,collection.mutable.Map[SeatId,AbstractPlayerIdentifier]]()

}
@Component
class GameReservationService {

  import GameReservationService._

  @Autowired
  var dao:MyBean = _

  def findGame(occassionId:Long) : Option[GameOccassion] = {
    dao.findGame(occassionSeqId)

  }

  /*def createGamePass(occassionId:OccassionId, reservationId:SeatId) : Ticket = {
    // Ticket - pre created
    new Ticket(reservationId)
  }*/

  def createGamePass(rg:RunningGame, pi:AbstractPlayerIdentifier, reservationIdOpt:Option[SeatId]) : Option[AbstractGamePass] = {
    rg match {
      case RunningGame(null,_) => Some(new IdentifyOnlyPass(pi))
      case RunningGame(NonPersisentGameOccassion(occasionId),_) =>
        reservationIdOpt.flatMap { res =>
          findReservation(res).map { case (occ,part) =>
            new GateGamePass(res)
          }

        }

      case RunningGame(GameOccassion(occassionId),_) => pi match {
        case p:RegisteredPlayerIdentifier =>
          val ticket = dao.findTicketByUserAndGame(p.getUserId, occassionId).get
          if(ticket.game.occassionId == occassionId) {
            Some(ticket)
          } else None
        case _ => None
      }
    }


    /*if(rg.isPublic) {

    } else if (!rg.requiresTicket) {

    } else {

    }*/

  }

  def allocateOccassion() : OccassionId = {
    val res:OccassionId = occassionSeqId
    occassionSeqId = occassionSeqId + 1
    openOccassions += res -> collection.mutable.Map.empty
    res
  }
  def reserveSeat(occassionId:OccassionId, pi:AbstractPlayerIdentifier) : SeatId = {
    var res:SeatId = seatSeqId
    openOccassions(occassionId) += (res -> pi)
    seatSeqId = seatSeqId + 1
    res
  }

  def findReservation(id:SeatId) : Option[(OccassionId,Map[SeatId,AbstractPlayerIdentifier])] = {
    openOccassions.find {
      case (occassionId, seatMap) => seatMap.exists( s => s._1 == id)
    }.map( r => (r._1,Map.empty ++ r._2))
  }

  def findReservationPlayerIdentifer(id:SeatId, verifyOccId:OccassionId) : Option[(AbstractPlayerIdentifier)] = {
    findReservation(id).flatMap { case (occassionId,map) =>
      if (occassionId == verifyOccId) {
        map.find { case (seat, pi) => seat == id }.map( p => p._2 )
      } else None
    }
  }
}

