package se.bupp.cs3k.server.service

import dao.{CompetitorDao, GameParticipationDao, UserDao, GameDao}
import gameserver.{GameServerPool, GameServerRepository}
import org.springframework.stereotype.{Service, Component}
import se.bupp.cs3k.server.model._
import org.apache.wicket.spring.injection.annot.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.api.user.{RegisteredPlayerIdentifier, AnonymousPlayerIdentifier, AbstractPlayerIdentifier}
import se.bupp.cs3k.api.{GateGamePass, IdentifyOnlyPass, AbstractGamePass}
import scala._
import se.bupp.cs3k.server.model.Model._
import org.apache.log4j.Logger
import scala.Left
import scala.Some
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import org.springframework.transaction.annotation.Transactional
import java.util.Date
import java.lang
import se.bupp.cs3k.server.model.NonPersisentGameOccassion
import se.bupp.cs3k.server.model.GameParticipationPk
import scala.Some
import se.bupp.cs3k.server.model.RegedUser
import se.bupp.cs3k.server.model.RunningGame
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.server.model.AnonUser

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
 * 1. Stå i lobby kö (Med namn/player)
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
  type GameOccassionId = Long
  type ReservationDetails = (AbstractUser, Option[TeamId])
  // TODO rename me
  type GameServerReservationId = Long
  private var occassionSeqId:Long = 100L
  var seatSeqId:Long= 1L
  var openGameSessions =
    collection.mutable.Map[GameSessionId,
      collection.mutable.Map[GameServerReservationId,(AbstractUser,Option[TeamId])]
      ]()

}
@Service
class GameReservationService {

  val log = Logger.getLogger(this.getClass)


  import GameReservationService._

  @Autowired
  var gameDao:GameDao = _

  @Autowired
  var gameParicipationDao:GameParticipationDao = _


  @Autowired
  var userDao:UserDao = _

  def findByGameSessionId(gameSessionId:Long) : Option[GameOccassion] = {
    log.info("query db occassion = " + gameSessionId)
    gameDao.findGame(gameSessionId)
  }


  def findGame(id:Long) : Option[GameOccassion] = {
    gameDao.find(new lang.Long(id))
  }


  /*def createGameServerPass(gameSessionId:OccassionId, reservationId:SeatId) : Ticket = {
    // Ticket - pre created
    new Ticket(reservationId)
  }*/

  def createGameServerPass(rg:RunningGame, pi:AbstractUser, reservationIdOpt:Option[GameServerReservationId], teamOpt:Option[Team]) : Option[AbstractGamePass] = {
    rg match {
      case RunningGame(null,_) =>
        val apiIdentifier = pi match {
          case RegedUser(i) => new RegisteredPlayerIdentifier(i)
          case AnonUser(s) => new AnonymousPlayerIdentifier(s)
        }

        Some(new IdentifyOnlyPass(apiIdentifier))
      case RunningGame(NonPersisentGameOccassion(occasionId),_) =>
        reservationIdOpt.flatMap { res =>
          findInMemoryReservation(res).map { case (occ,part) =>
            new GateGamePass(res)
          }
        }

      case RunningGame(go:GameOccassion,_) => pi match {
        case p:RegedUser =>
          // Might be wrong?
          Some(new GateGamePass(reserveSeat(go.gameSessionIdOpt.get, pi, teamOpt.map(_.id))))
          /*val ticket = ticketDao.findTicketByUserAndGame(p.getUserId, go.gameSessionId).get
          if(ticket.game.gameSessionId == go.gameSessionId) {
            Some(ticket)
          } else None*/
        case _ => None
      }
    }


    /*if(rg.isPublic) {

    } else if (!rg.requiresTicket) {

    } else {

    }*/
  }


  @Transactional
  def challangeCompetitor(challanger:Competitor, challangee:Competitor) : GameOccassion = {
    val go = (challanger, challangee) match {
      case (u1:User, u2:User) =>

        //val occasionId = allocateGameSession()
        val go = new GameOccassion()
        go.competitorType = "individual"

        val gp1 = new GameParticipation(new GameParticipationPk(u1,go))
        val gp2 = new GameParticipation(new GameParticipationPk(u2,go))
        go.participants.add(gp1)
        go.participants.add(gp2)

        /*val t = new Ticket()
        t.game = go
        t.user = u1
        val t2 = new Ticket()
        t2.game = go
        t2.user = u2
        ticketDao.insert(t)
        ticketDao.insert(t2)*/


        go

      case (t1:Team, t2:Team) =>
        val go = new GameOccassion()
        go.competitorType = "team"

        val gp1 = new GameParticipation(new GameParticipationPk(t1,go))
        val gp2 = new GameParticipation(new GameParticipationPk(t2,go))
        go.participants.add(gp1)
        go.participants.add(gp2)
        go

    }


    gameDao.insert(go)

    import scala.collection.JavaConversions.asScalaBuffer
    go.participants.foreach(gameDao.em.persist(_))

    go

  }

  def allocateGameSession() : GameSessionId = {
    val res:GameSessionId = occassionSeqId
    log.info("Allocating new game sessiond id " + res)
    occassionSeqId = occassionSeqId + 1
    openGameSessions += res -> collection.mutable.Map.empty
    res
  }
  def reserveSeat(gameSessionId:GameSessionId, pi:AbstractUser, teamOpt:Option[TeamId]) : GameServerReservationId = {
    var res:GameServerReservationId = seatSeqId
    openGameSessions(gameSessionId) += (res -> (pi,teamOpt))
    seatSeqId = seatSeqId + 1
    res
  }

  def findInMemoryReservation(id:GameServerReservationId) : Option[(GameSessionId,Map[GameServerReservationId,ReservationDetails])] = {
    openGameSessions.find {
      case (gameSessionId, seatMap) => seatMap.exists( s => s._1 == id)
    }.map( r => (r._1,Map.empty ++ r._2))
  }

  def findInMemoryReservation(reservationId:GameServerReservationId, reservationGameSessionId: GameSessionId) : Option[ReservationDetails] = {
    openGameSessions.get(reservationGameSessionId).flatMap( m => m.get(reservationId))
  }

  def findReservationPlayerIdentifer(id:GameServerReservationId, verifyOccId:GameSessionId) : Option[ReservationDetails] = {
    findInMemoryReservation(id).flatMap { case (gameSessionId,map) =>
      if (gameSessionId == verifyOccId) {
        map.find { case (seat, user) => seat == id }.map( p => p._2 )
      } else {
        None
      }
    }
  }

  def findUnplayedGamesForCompetitor(c:Competitor) = {
    gameDao.findAll
  }

  @Transactional
  def startPersistedGameServer(g:GameOccassion) = {
    log.info("g.timeTriggerStart canSpawn " + g.timeTriggerStart)

    if (!g.timeTriggerStart) {
      val gameSessionId = allocateGameSession()
      g.gameSessionIdOpt = Some(gameSessionId)
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
      this.findByGameSessionId(gameSessionId).flatMap( g =>
        startPersistedGameServer(g)
      )
    }
    r.getOrElse(throw new IllegalArgumentException("Couldnt find or create game " + gameSessionId))
  }

  def playOpenServer(serverId:Long, playerId:AbstractUser) = {
    throw new NotImplementedException()
  }

  def playNonScheduledClosed(reservationId:GameServerReservationId, userId:AbstractUser) = {
    val gameSessionId = this.findInMemoryReservation(reservationId).map(_._1).getOrElse(throw new IllegalArgumentException("reservationId doenst exist " + reservationId ))
    val rg = GameServerPool.pool.findRunningGame(gameSessionId).getOrElse(throw new IllegalStateException("No server found for " + gameSessionId))
    val gp = this.createGameServerPass(rg, userId, Some(reservationId), None).getOrElse(throw new IllegalArgumentException("reservationId doenst exist " + reservationId ))
    (rg,gp)
  }

  @Transactional
  def playScheduledClosed(gameOccassionId:GameOccassionId, player:RegedUser) =  {
    // TODO, check eligable

    val game = gameDao.find(new lang.Long(gameOccassionId)).getOrElse(throw new IllegalArgumentException("GameOccassion " + gameOccassionId + " not found"))

    val competitor = competingFor(game,player)
    if (competitor.isEmpty) {
      throw new IllegalArgumentException("Player " + player + "not eligable for " + game )
    }



    val rgOpt = game.gameSessionIdOpt match {
      case Some(gameSessionId) =>
        log.info("path a")
        GameServerPool.pool.findRunningGame(gameSessionId)
      case None =>
        log.info("path b")
        startPersistedGameServer(game)
    }
    val teamOpt = competitor.collect { case t:Team => t }

    log.info("rgOpt " + rgOpt)
    //val rg = findOrCreateServer(gameOccassionId)
    val rg = rgOpt.getOrElse(throw new IllegalStateException("No server could be started for " + game))
    val gp = this.createGameServerPass(rg, player, None, teamOpt ).getOrElse(throw new IllegalArgumentException("Unable to create game pass for " + player + " " + gameOccassionId))
    (rg,gp)
  }

  @Autowired
  var competitorDao:CompetitorDao = _


  protected def competingFor(game:GameOccassion, playerId:RegedUser) : Option[Competitor] = {
    import scala.collection.JavaConversions.asScalaBuffer
    log.info("Who is player competing for")

    game.competitorType match {
      case "individual" => game.participants.map(_.id.competitor).find { case u:User => u.id == playerId.id }
      case "team" =>
          competitorDao.findPlayerTeams(playerId.id).find(
            pt => game.participants.exists {
              gp => gp.id.competitor match {
                  case t:Team => t.members.exists( m => m.id.user.id == playerId.id &&  pt.id == t.id )
                  case _ => throw new IllegalStateException("bajja")
                }
            }
          )

      case _ =>
        None
    }
  }

  def getServerAndCredentials(userIdOpt:Option[UserId], reservationIdOpt: Option[Long], serverIdOpt: Option[Long], gameOccasionIdOpt:Option[GameServerReservationId], playerNameOpt:Option[String]) = {



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
      case Some(p) => Some(new RegisteredPlayerIdentifierWithInfo(p.username,p.id))
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
                gameReservationService.findByGameSessionId(gameSessionId).flatMap( g =>
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

