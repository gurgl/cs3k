package se.bupp.cs3k.server.service

import org.springframework.stereotype.Component
import se.bupp.cs3k.server.model.{Ticket, GameOccassion}
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.web.MyBean
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.api.AbstractPlayerInfo

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
  var openOccassions = collection.mutable.Map[OccassionId,collection.mutable.Map[SeatId,AbstractPlayerInfo]]()

}
@Component
class GameReservationService {

  import GameReservationService._

  @Autowired
  var dao:MyBean = _

  def findGame(occassionId:Long) : Option[GameOccassion] = {
    dao.findGame(occassionSeqId)
  }

  def createGamePass(occassionId:OccassionId, reservationId:SeatId) : Ticket = {
    // Ticket - pre created
    new Ticket(reservationId)
  }

  def allocateOccassion() : OccassionId = {
    val res:OccassionId = occassionSeqId
    occassionSeqId = occassionSeqId + 1
    openOccassions += res -> collection.mutable.Map.empty
    res
  }
  def reserveSeat(occassionId:OccassionId, pi:AbstractPlayerInfo) : SeatId = {
    var res:SeatId = seatSeqId
    openOccassions(occassionId) = openOccassions(occassionId) + (res -> pi)
    seatSeqId = seatSeqId + 1
    res
  }

  def findReservation(id:SeatId) : Option[(OccassionId,Map[SeatId,AbstractPlayerInfo])] = {
    openOccassions.find {
      case (occassionId, seats) => seats.exists( s => s == id)
    }.map( r => (r._1,Map.empty ++ r._2))
  }
}

