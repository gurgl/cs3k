package se.bupp.cs3k.server.service

import dao.{CompetitorDao, GameParticipationDao, UserDao, GameDao}
import gameserver.{GameServerPool, GameServerRepository}
import org.springframework.stereotype.{Service, Component}
import resourceallocation.ServerAllocator
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

  private var occassionSeqId:Long = 100L
  private var virtualTeamSeqId:Long = 1000L

  var seatSeqId:Long= 1L
  var openGameSessions =
    Map[GameSessionId,
      (Players, TeamsDetailsOpt)
      ]()

  //var openGameSessionsTeams = Map[GameSessionId,List[AbstractTeamRef]]()

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
      case RunningGame(null,_,_) =>
        val apiIdentifier = pi match {
          case RegedUser(i) => new RegisteredPlayerIdentifier(i)
          case AnonUser(s) => new AnonymousPlayerIdentifier(s)
        }

        Some(new IdentifyOnlyPass(apiIdentifier))
      case RunningGame(NonPersisentGameOccassion(occasionId),_,_) =>
        reservationIdOpt.flatMap { res =>
          findGameSessionByReservationId(res).map { case (occ,part) =>
            new GateGamePass(res)
          }
        }

      case RunningGame(go:GameOccassion,_,_) => pi match {
        case p:RegedUser =>
          // Might be wrong?
          Some(new GateGamePass(reserveSeat(go.gameSessionIdOpt.get, pi, teamOpt.map( t => TeamRef(t.id)))))
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

  def addTeamToSession(gameSessionId:GameSessionId, at:AbstractTeamRef)  {

    findGameSession(gameSessionId) match {
      case Some((players, teamsOpt)) =>

        val teams = teamsOpt.toList.flatten :+ at

        val newEntry = (gameSessionId -> (players, Some(teams)))
        log.info("Modifying gs : add team " + newEntry)
        openGameSessions = openGameSessions +  newEntry
      case None =>
        throw new RuntimeException("Session Not found")
    }
  }

  def createVirtualTeam(name:Option[String]) : VirtualTeamRef = {

    val res  = new VirtualTeamRef(virtualTeamSeqId, name)
    log.info("Allocating new virtual team id " + res)
    virtualTeamSeqId = virtualTeamSeqId + 1

    res
  }


  def allocateGameSession() : GameSessionId = {
    val res:GameSessionId = occassionSeqId
    log.info("Allocating new game sessiond id " + res)
    occassionSeqId = occassionSeqId + 1
    openGameSessions = openGameSessions + (res -> (Map.empty, None))
    res
  }

  def reserveSeat(gameSessionId:GameSessionId, pi:AbstractUser, pTeamOpt:Option[AbstractTeamRef]) : GameServerReservationId = {
    findGameSession(gameSessionId) match {
      case Some((players, teamsOpt)) =>
        var res:GameServerReservationId = seatSeqId
        seatSeqId = seatSeqId + 1
        val modifiedSessionEntry = gameSessionId -> (players + (res ->(pi, pTeamOpt)), teamsOpt)
        openGameSessions = openGameSessions + modifiedSessionEntry
        //openGameSessions(gameSessionId) +  (res -> (pi,teamOpt))
        res
      case None =>
        throw new RuntimeException("Session Not found")
    }
  }



  def findGameSessionIdAndPlayerByReservationId(id:GameServerReservationId) : Option[(GameSessionId,ReservationDetails)] = {
    findGameSessionByReservationId(id:GameServerReservationId).map { case (gsId, (plyrs,teams)) => (gsId,plyrs(id)) }
  }
  def findGameSessionByReservationId(id:GameServerReservationId) : Option[(GameSessionId,Session)] = {
    openGameSessions.find {
      case (gameSessionId, (seatMap, teamsOpt)) => seatMap.exists( s => s._1 == id)
    }
  }

  def findReservation(reservationId:GameServerReservationId, reservationGameSessionId: GameSessionId) : Option[ReservationDetails] = {
    openGameSessions.get(reservationGameSessionId).flatMap { case (plyrs,teamsOpt)=> plyrs.get(reservationId) }
  }

  def findGameSession(gameSessionId: GameSessionId) : Option[Session] = {
    openGameSessions.get(gameSessionId)
  }

  def findReservationPlayerIdentifer(id:GameServerReservationId, verifyOccId:GameSessionId) : Option[ReservationDetails] = {
    findGameSessionByReservationId(id).flatMap { case (gameSessionId,(plyers,teamsOpt)) =>
      if (gameSessionId == verifyOccId) {
        plyers.find { case (seat, details) => seat == id }.map( p => p._2 )
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

      import scala.collection.JavaConversions.asScalaBuffer
      val teams = g.participants.map( _.id.competitor ) .collect { case t: Team  => t }
      if(teams.size > 0 ) {
        if(teams.size == g.participants.size) {
          teams.foreach { t => addTeamToSession(gameSessionId, new TeamRef(t.id)) }
        } else {
          throw new IllegalStateException("Mixed competitors in gameocassion")
        }
      }

      g.gameSessionIdOpt = Some(gameSessionId)
      var processSettings = GameServerRepository.findBy(g.gameAndRulesId).getOrElse(throw new IllegalArgumentException("Unknown gs setting"))
      log.info("GAMES IN startPersistedGameServer " + gameDao.findAll.mkString(","))


      /*val launcher = (pt:ProcessToken) => {
        removePartyFromQueue(party)
        val runningGame = launchServerInstance(gameServerSettings, party, pt)
        runningGame.done
      }
      val allocation = ServerAllocator.serverAllocator.allocate(launcher)

      */


      //TODO Fixeme
      val processToken = 11
      var server: RunningGame = GameServerPool.pool.spawnServer(processSettings, g, processToken)


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

  def playNonScheduledClosed(reservationId:GameServerReservationId) = {
    val (gameSessionId, (user, teamOpt) ) = findGameSessionIdAndPlayerByReservationId(reservationId).getOrElse(throw new IllegalArgumentException("reservationId doenst exist " + reservationId ))
    val rg = GameServerPool.pool.findRunningGame(gameSessionId).getOrElse(throw new IllegalStateException("No server found for " + gameSessionId))
    val gp = createGameServerPass(rg, user, Some(reservationId), None).getOrElse(throw new IllegalArgumentException("reservationId doenst exist " + reservationId ))
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
}

