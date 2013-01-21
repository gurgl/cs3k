package se.bupp.cs3k.server.service

import dao.{UserDao, GameDao, TicketDao}
import gameserver.{GameServerPool, GameServerRepository}
import org.springframework.stereotype.Component
import se.bupp.cs3k.server.model._
import org.apache.wicket.spring.injection.annot.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.api.user.{RegisteredPlayerIdentifier, AnonymousPlayerIdentifier, AbstractPlayerIdentifier}
import se.bupp.cs3k.api.{GateGamePass, IdentifyOnlyPass, AbstractGamePass}
import se.bupp.cs3k.server.model.RunningGame
import se.bupp.cs3k.server.model.NonPersisentGameOccassion
import scala._
import se.bupp.cs3k.server.model.Model._
import se.bupp.cs3k.server.model.NonPersisentGameOccassion
import se.bupp.cs3k.server.model.RunningGame
import org.apache.log4j.Logger
import scala.Left
import se.bupp.cs3k.server.model.NonPersisentGameOccassion
import scala.Some
import se.bupp.cs3k.server.model.RunningGame
import sun.reflect.generics.reflectiveObjects.NotImplementedException

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
  type NonPersistentOccassionTicketId = Long
  var occassionSeqId:Long = 1L
  var seatSeqId:Long= 1L
  var openOccassions = collection.mutable.Map[OccassionId,collection.mutable.Map[NonPersistentOccassionTicketId,AbstractPlayerIdentifier]]()

}
@Component
class GameReservationService {

  val log = Logger.getLogger(this.getClass)


  import GameReservationService._

  @Autowired
  var ticketDao:TicketDao = _

  @Autowired
  var gameDao:GameDao = _


  @Autowired
  var userDao:UserDao = _

  def findGame(occassionId:Long) : Option[GameOccassion] = {
    gameDao.findGame(occassionSeqId)

  }

  /*def createGamePass(occassionId:OccassionId, reservationId:SeatId) : Ticket = {
    // Ticket - pre created
    new Ticket(reservationId)
  }*/

  def createGamePass(rg:RunningGame, pi:AbstractPlayerIdentifier, reservationIdOpt:Option[NonPersistentOccassionTicketId]) : Option[AbstractGamePass] = {
    rg match {
      case RunningGame(null,_) => Some(new IdentifyOnlyPass(pi))
      case RunningGame(NonPersisentGameOccassion(occasionId),_) =>
        reservationIdOpt.flatMap { res =>
          findInMemoryReservation(res).map { case (occ,part) =>
            new GateGamePass(res)
          }
        }

      case RunningGame(go:GameOccassion,_) => pi match {
        case p:RegisteredPlayerIdentifier =>
          val ticket = ticketDao.findTicketByUserAndGame(p.getUserId, go.occassionId).get
          if(ticket.game.occassionId == go.occassionId) {
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
  def reserveSeat(occassionId:OccassionId, pi:AbstractPlayerIdentifier) : NonPersistentOccassionTicketId = {
    var res:NonPersistentOccassionTicketId = seatSeqId
    openOccassions(occassionId) += (res -> pi)
    seatSeqId = seatSeqId + 1
    res
  }

  def findInMemoryReservation(id:NonPersistentOccassionTicketId) : Option[(OccassionId,Map[NonPersistentOccassionTicketId,AbstractPlayerIdentifier])] = {
    openOccassions.find {
      case (occassionId, seatMap) => seatMap.exists( s => s._1 == id)
    }.map( r => (r._1,Map.empty ++ r._2))
  }

  def findReservationPlayerIdentifer(id:NonPersistentOccassionTicketId, verifyOccId:OccassionId) : Option[(AbstractPlayerIdentifier)] = {
    findInMemoryReservation(id).flatMap { case (occassionId,map) =>
      if (occassionId == verifyOccId) {
        map.find { case (seat, pi) => seat == id }.map( p => p._2 )
      } else None
    }
  }

  def findUnplayedGamesForCompetitor(c:Competitor) = {
    gameDao.findAll
  }


  def findOrCreateServer(occassionId:OccassionId) = {
    val canSpawn = true
    var processSettings = GameServerRepository.findByProcessTemplate('TG2Player).getOrElse(throw new IllegalArgumentException("Unknown gs setting"))

    val alreadyRunningGame = GameServerPool.pool.findRunningGame(occassionId)
    val r = alreadyRunningGame.orElse {
      this.findGame(occassionId).flatMap( g =>
      // TODO: Fix me - hardcoded below
      {
        log.info("g.timeTriggerStart canSpawn " + g.timeTriggerStart + " " + canSpawn)
        if (!g.timeTriggerStart && canSpawn) {
          Some(GameServerPool.pool.spawnServer(processSettings, g))
        } else {
          None
        }
      }
      )
    }
    r.getOrElse(throw new IllegalArgumentException("Couldnt find or create game " + occassionId))
  }

  def playOpenServer(serverId:Long, playerId:AbstractPlayerIdentifier) = {
    throw new NotImplementedException()
  }

  def playNonScheduledClosed(reservationId:NonPersistentOccassionTicketId, userId:AbstractPlayerIdentifier) = {
    val occassionId = this.findInMemoryReservation(reservationId).map(_._1).getOrElse(throw new IllegalArgumentException("reservationId doenst exist " + reservationId ))
    val rg = findOrCreateServer(occassionId)
    val gp = this.createGamePass(rg, userId, Some(reservationId)).getOrElse(throw new IllegalArgumentException("reservationId doenst exist " + reservationId ))
    (rg,gp)
  }

  def playScheduledClosed(gameOccassionId:OccassionId, playerId:RegisteredPlayerIdentifier) =  {
    val rg = findOrCreateServer(gameOccassionId)
    val gp = this.createGamePass(rg, playerId, None /*?*/).getOrElse(throw new IllegalArgumentException("Unable to create game pass for " + playerId + " " + gameOccassionId))
    (rg,gp)
  }

  def getServerAndCredentials(userIdOpt:Option[UserId], reservationIdOpt: Option[Long], serverIdOpt: Option[Long], gameOccasionIdOpt:Option[NonPersistentOccassionTicketId], playerNameOpt:Option[String]) = {



    /*
    val userOpt:Option[AbstractPlayerIdentifier] = userIdOpt.flatMap(
      id => userDao.findUser(id).map(
        p => new RegisteredPlayerIdentifier(p.id)
      )
    ).orElse(
      playerNameOpt.map(
        n => new AnonymousPlayerIdentifier(n)
      )
    )

    /*val user2:Option[AbstractPlayerIdentifier] = userIdOpt.flatMap( id => dao.findUser(id)) match {
      case Some(p) => Some(new PlayerIdentifierWithInfo(p.username,p.id))
      case None => playerNameOpt.map(n => new AnonymousPlayerIdentifier(n))
    }*/

    val userValidation = userOpt.toRight("Couldnt Construct user")

    val serverValidation = userValidation.onSuccess {
      userId => serverIdOpt match {
        case Some(serverId) =>
          // Rullande / Public
          Left("Not implemented")

        case None => {

          val canSpawnServerAndOccassionIdValidation = (reservationIdOpt, gameOccasionIdOpt) match {
            case (None, Some(gameOccassionId)) => // Rullande/Public
              Right((true,gameOccassionId))
            case (Some(reservationId), _) => // Lobby
              this.findInMemoryReservation(reservationId) match {
                case Some((occassionId,_)) => Right((true, occassionId))
                case None => Left("Reservation not found")
              }
            case _ => Left("No game id sent")
          }

          var processSettings = GameServerRepository.findByProcessTemplate('TG2Player).getOrElse(throw new IllegalArgumentException("Unknown gs setting"))

          val runningGameValidation = canSpawnServerAndOccassionIdValidation.onSuccess {
            case (canSpawn, occassionId) =>

              val alreadyRunningGame = GameServerPool.pool.findRunningGame(occassionId)
              val r = alreadyRunningGame.orElse {
                gameReservationService.findGame(occassionId).flatMap( g =>
                // TODO: Fix me - hardcoded below
                {
                  log.info("g.timeTriggerStart canSpawn " + g.timeTriggerStart + " " + canSpawn)
                  if (!g.timeTriggerStart && canSpawn) {
                    Some(GameServerPool.pool.spawnServer(processSettings, g))
                  } else {
                    None
                  }
                }
                )
              }
              val s = r.map(Right(_)).getOrElse(Left("Couldnt find game"))
              s
          }
          runningGameValidation
        }
      }
    }

    val serverAndPassValidation = serverValidation.onSuccess { rg =>
      val r = this.createGamePass(rg,userOpt.get,reservationIdOpt) match {
        case Some(gamePass) => Right((rg,gamePass))
        case None => Left("Unable to acquire valid pass")
      }
      r
    }
    serverAndPassValidation
    */
  }




}

