package se.bupp.cs3k.server.service

import dao.{GameParticipationDao, UserDao, GameDao, TicketDao}
import gameserver.{GameServerPool, GameServerRepository}
import org.springframework.stereotype.{Service, Component}
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
import org.springframework.transaction.annotation.Transactional
import java.util.Date

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
  type GameSessionId = Long
  type NonPersistentOccassionTicketId = Long
  private var occassionSeqId:Long = 100L
  var seatSeqId:Long= 1L
  var openGameSessions = collection.mutable.Map[GameSessionId,collection.mutable.Map[NonPersistentOccassionTicketId,AbstractPlayerIdentifier]]()

}
@Service
class GameReservationService {

  val log = Logger.getLogger(this.getClass)


  import GameReservationService._

  @Autowired
  var ticketDao:TicketDao = _

  @Autowired
  var gameDao:GameDao = _

  @Autowired
  var gameParicipationDao:GameParticipationDao = _


  @Autowired
  var userDao:UserDao = _

  def findGame(gameSessionId:Long) : Option[GameOccassion] = {
    log.info("query db occassion = " + gameSessionId)
    gameDao.findGame(gameSessionId)

  }

  /*def createGameServerPass(gameSessionId:OccassionId, reservationId:SeatId) : Ticket = {
    // Ticket - pre created
    new Ticket(reservationId)
  }*/

  def createGameServerPass(rg:RunningGame, pi:AbstractPlayerIdentifier, reservationIdOpt:Option[NonPersistentOccassionTicketId]) : Option[AbstractGamePass] = {
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
          val ticket = ticketDao.findTicketByUserAndGame(p.getUserId, go.gameSessionId).get
          if(ticket.game.gameSessionId == go.gameSessionId) {
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

  /*@Transactional
  def createPersistedGame(compByParticipants:List[Competitor]) {

    val game= new GameOccassion(123)

    compByParticipants.foreach { case (c) =>
        //gameParicipationDao.insert(new GameParticipation(new GameParticipationPk(c,)))
      game.participants = compByParticipants
    }

    gameDao.insert(game)

  }*/

  @Transactional
  def challangeCompetitor(challanger:Competitor, challangee:Competitor) : GameOccassion = {
    (challanger, challangee) match {
      case (u1:User, u2:User) =>

        val occasionId = allocateGameSession()
        val go = new GameOccassion(occasionId)
        val gp1 = new GameParticipation(new GameParticipationPk(u1,go))
        val gp2 = new GameParticipation(new GameParticipationPk(u2,go))
        go.participants.add(gp1)
        go.participants.add(gp2)

        gameDao.insert(go)

        gameDao.em.persist(gp1)
        gameDao.em.persist(gp2)
        val t = new Ticket()
        t.game = go
        t.user = u1
        val t2 = new Ticket()
        t2.game = go
        t2.user = u2
        ticketDao.insert(t)
        ticketDao.insert(t2)


        go

      case _ => throw new IllegalStateException("Not implemented")
    }
  }

  def allocateGameSession() : GameSessionId = {
    val res:GameSessionId = occassionSeqId
    log.info("Allocating new game sessiond id " + res)
    occassionSeqId = occassionSeqId + 1
    openGameSessions += res -> collection.mutable.Map.empty
    res
  }
  def reserveSeat(gameSessionId:GameSessionId, pi:AbstractPlayerIdentifier) : NonPersistentOccassionTicketId = {
    var res:NonPersistentOccassionTicketId = seatSeqId
    openGameSessions(gameSessionId) += (res -> pi)
    seatSeqId = seatSeqId + 1
    res
  }

  def findInMemoryReservation(id:NonPersistentOccassionTicketId) : Option[(GameSessionId,Map[NonPersistentOccassionTicketId,AbstractPlayerIdentifier])] = {
    openGameSessions.find {
      case (gameSessionId, seatMap) => seatMap.exists( s => s._1 == id)
    }.map( r => (r._1,Map.empty ++ r._2))
  }

  def findReservationPlayerIdentifer(id:NonPersistentOccassionTicketId, verifyOccId:GameSessionId) : Option[(AbstractPlayerIdentifier)] = {
    findInMemoryReservation(id).flatMap { case (gameSessionId,map) =>
      if (gameSessionId == verifyOccId) {
        map.find { case (seat, pi) => seat == id }.map( p => p._2 )
      } else None
    }
  }

  def findUnplayedGamesForCompetitor(c:Competitor) = {
    gameDao.findAll
  }

  @Transactional
  def startPersistedGameServer(g:GameOccassion) = {
    log.info("g.timeTriggerStart canSpawn " + g.timeTriggerStart)
    if (!g.timeTriggerStart) {
      var processSettings = GameServerRepository.findBy(g.gameAndRulesId).getOrElse(throw new IllegalArgumentException("Unknown gs setting"))
      log.info("GAMES IN startPersistedGameServer " + gameDao.findAll.mkString(","))
      var server: RunningGame = GameServerPool.pool.spawnServer(processSettings, g)


      g.gameServerStartedAt = new Date()

      gameDao.update(g)

      Some(server)
    } else {
      None
    }
  }

  def findOrCreateServer(gameSessionId:GameSessionId) = {
    val canSpawn = true


    val alreadyRunningGame = GameServerPool.pool.findRunningGame(gameSessionId)
    val r = alreadyRunningGame.orElse {
      this.findGame(gameSessionId).flatMap( g =>
        startPersistedGameServer(g)
      )
    }
    r.getOrElse(throw new IllegalArgumentException("Couldnt find or create game " + gameSessionId))
  }

  def playOpenServer(serverId:Long, playerId:AbstractPlayerIdentifier) = {
    throw new NotImplementedException()
  }

  def playNonScheduledClosed(reservationId:NonPersistentOccassionTicketId, userId:AbstractPlayerIdentifier) = {
    val gameSessionId = this.findInMemoryReservation(reservationId).map(_._1).getOrElse(throw new IllegalArgumentException("reservationId doenst exist " + reservationId ))
    val rg = findOrCreateServer(gameSessionId)
    val gp = this.createGameServerPass(rg, userId, Some(reservationId)).getOrElse(throw new IllegalArgumentException("reservationId doenst exist " + reservationId ))
    (rg,gp)
  }

  def playScheduledClosed(gameOccassionId:GameSessionId, playerId:RegisteredPlayerIdentifier) =  {
    // TODO, check eligable
    val rg = findOrCreateServer(gameOccassionId)
    val gp = this.createGameServerPass(rg, playerId, None /*?*/).getOrElse(throw new IllegalArgumentException("Unable to create game pass for " + playerId + " " + gameOccassionId))
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
                case Some((gameSessionId,_)) => Right((true, gameSessionId))
                case None => Left("Reservation not found")
              }
            case _ => Left("No game id sent")
          }

          var processSettings = GameServerRepository.findByProcessTemplate('TG2Player).getOrElse(throw new IllegalArgumentException("Unknown gs setting"))

          val runningGameValidation = canSpawnServerAndOccassionIdValidation.onSuccess {
            case (canSpawn, gameSessionId) =>

              val alreadyRunningGame = GameServerPool.pool.findRunningGame(gameSessionId)
              val r = alreadyRunningGame.orElse {
                gameReservationService.findGame(gameSessionId).flatMap( g =>
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
      val r = this.createGameServerPass(rg,userOpt.get,reservationIdOpt) match {
        case Some(gamePass) => Right((rg,gamePass))
        case None => Left("Unable to acquire valid pass")
      }
      r
    }
    serverAndPassValidation
    */
  }




}

