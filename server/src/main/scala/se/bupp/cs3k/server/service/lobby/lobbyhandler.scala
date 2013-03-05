package se.bupp.cs3k.server.service.lobby

import org.slf4j.{LoggerFactory, Logger}
import com.esotericsoftware.kryonet.{Listener, Server, KryoSerialization, Connection}
import se.bupp.cs3k.server.model._
import java.util.concurrent.{TimeUnit, Executors}
import java.net.URL
import se.bupp.cs3k._
import se.bupp.cs3k.server.Cs3kConfig
import se.bupp.cs3k.server.model.Model._
import collection.parallel.mutable
import se.bupp.cs3k.server.service.gameserver.{GameServerRepository, GameServerPool, GameProcessTemplate}
import scala.Some
import se.bupp.cs3k.server.model.RegedUser
import se.bupp.cs3k.server.model.RunningGame
import se.bupp.cs3k.server.model.AnonUser
import se.bupp.cs3k.server.service.resourceallocation.ServerAllocator


import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.BeanSerializer
import server.model.AnonUser
import server.model.NonPersisentGameOccassion
import scala.Some
import server.model.RegedUser
import server.model.RunningGame
import server.service.{GameReservationService, RankingService}
import util.{Failure, Success}
import collection.immutable.Queue
import concurrent.Future
import se.bupp.cs3k.server.model.Model._
/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-06
 * Time: 03:29
 * To change this template use File | Settings | File Templates.
 */
object AbstractLobbyQueueHandler {
  val log = LoggerFactory.getLogger(this.getClass)
  var gameReservationService:GameReservationService = _

  var rankingService:RankingService = _


  def sendStartGameInstructionsToParty(party:List[(Connection,AbstractUser,GameServerReservationId)], gameSessionId : GameSessionId, runningGame: RunningGame) {
    val scheduler = Executors.newScheduledThreadPool(1);
    log.info("creating delayed game launch announcer task " + party.size)
    val beeper = new Runnable() {
      def  run() {
        log.info("In delayed game launch announcer " + party.size)
        party.foreach { case (c,pi, reservationId) =>
          try {
            // TODO Associate reservation with either ID or name (depending on pi subclass)
            var jnlpUrl: URL = runningGame.processSettings.jnlpUrl(reservationId, None)
            c.sendTCP(new StartGame(jnlpUrl.toExternalForm))
          } catch {
            case e:Exception => e.printStackTrace()
          }
          c.close()
        }
      }
    }
    val beeperHandle = scheduler.schedule(beeper, Cs3kConfig.LOBBY_GAME_LAUNCH_ANNOUNCEMENT_DELAY,  TimeUnit.SECONDS);
  }
}

import AbstractLobbyQueueHandler._

// TODO: Anon Lobby dont need ranking functionallity but keep it like for testing
class NonTeamLobbyQueueHandler(val numOfPlayers:Int, gameAndRulesId: GameAndRulesId) extends AbstractRankedLobbyQueueHandler[None.type](gameAndRulesId) {

  def customize(u: AbstractUser) = None

  def reserveSeats(gameSessionId:GameSessionId, party: List[(Connection, AbstractUser)]) : List[(Connection, AbstractUser, GameServerReservationId)] = {
    log.info("Reserving seats")
    party.map { case (c,u) =>
      val reservationId = gameReservationService.reserveSeat(gameSessionId, u, None)
      (c, u , reservationId)
    }
  }

  def isMatchable(firstRank: NonTeamLobbyQueueHandler#Info, ranking: NonTeamLobbyQueueHandler#Info) = true
}


class RankedTeamLobbyQueueHandler(numOfTeams:Int, numOfPlayersPerTeam:Int, gameAndRulesId: GameAndRulesId) extends AnonTeamLobbyQueueHandler(numOfTeams,numOfPlayersPerTeam,gameAndRulesId) with HasTeamSupport {
  override def customize(u: AbstractUser) = u match {
    case RegedUser(id) => rankingService.getRanking(id).getOrElse(0)
    case _ => throw new IllegalArgumentException("Ranked lobby cant handle anon players")
  }
}

trait HasTeamSupport {

  def numOfTeams : Int
  def numOfPlayersPerTeam : Int
  def numOfPlayers = numOfTeams * numOfPlayersPerTeam
}


class AnonTeamLobbyQueueHandler(val numOfTeams:Int, val numOfPlayersPerTeam:Int, gameAndRulesId: GameAndRulesId) extends AbstractRankedLobbyQueueHandler[Int](gameAndRulesId) with HasTeamSupport {


  def customize(u: AbstractUser) = u match {
    case _ => 3

  }

  def isMatchable(firstRank: Info, ranking: Info): Boolean = {
    math.abs(firstRank - ranking) <= 2
  }

  def reserveSeats(gameSessionId:GameSessionId, party: List[(Connection, AbstractUser)]) : List[(Connection, AbstractUser, GameServerReservationId)] = {
    log.info("Reserving seats")

    val teams = (1 to numOfTeams).map { tid => gameReservationService.createVirtualTeam(Some("Team " + tid.toString))}
    teams.flatMap( t => (1 to numOfPlayersPerTeam).map( tt => t) ).zip(party).toList.map { case (t,(c,u)) => (c,u,gameReservationService.reserveSeat(gameSessionId, u, Some(t))) }
  }
}

abstract class AbstractRankedLobbyQueueHandler[T](gameAndRulesId: GameAndRulesId) extends AbstractLobbyQueueHandler[T](gameAndRulesId)  {

  def buildLobbies(oldQueueMembersWithLobbyAssignments:List[(AbstractUser,Int)], theQueue:Queue[(Connection,(AbstractUser, Info))]) : (List[List[AbstractUser]],List[List[AbstractUser]], List[(AbstractUser,Int)]) = {
    val disolvedLobbyIds = oldQueueMembersWithLobbyAssignments.filterNot {
      case (u, t) => theQueue.exists {
        case (c, (uu, _)) => u == uu
      }
    }.map( _._2)
    var oldQueueMembersWithLobbyAssignmentsRev = oldQueueMembersWithLobbyAssignments.filterNot { case (u,t) => disolvedLobbyIds.exists ( _ ==  t) }
    val evaluatableQueue = theQueue.filterNot { case (c,(u,r)) => oldQueueMembersWithLobbyAssignmentsRev.exists(_._1 == u) }.toList

    val matches = matchRanking(evaluatableQueue.map(_._2))

    var (completeParties, nonFullParties) = matches.map( l => l.map( _._1)).partition(p => p.size == numOfPlayers)
    //val completeParties = bupps

    val completePartiesIndexed = (oldQueueMembersWithLobbyAssignmentsRev.groupBy(_._2).values.map(_.map(_._1)).toList ::: completeParties).zipWithIndex

    val queueMembersWithLobbyAssignmentsNew = theQueue.map { case (c,(u,i)) =>
      completePartiesIndexed.find { case (party,_) => party.exists { case (cpu) => u == cpu } } match {
        case Some((_,idx)) => (u, Some(idx))
        case None => (u,None)
      }
    }.collect { case (u, Some(t)) => (u,t) }.toList

    (completeParties, nonFullParties, queueMembersWithLobbyAssignmentsNew)
  }

  def matchRanking(queue:List[UserInfo]) : List[(List[UserInfo])] = {
    queue match {
      case (first, firstRank) :: tail =>
        println(first.asInstanceOf[AnonUser].name)
        val (party,left) = tail.foldLeft((List[UserInfo](),List[UserInfo]())) {
          case ((rr,lr), (u, ranking) ) => {
            val numNeededForGame = ((numOfPlayers) - 1)
            if(isMatchable(firstRank, ranking) && rr.size < numNeededForGame)
              (rr.:+ (u,ranking), lr)
            else
              (rr, lr.:+ (u,ranking))
          }
        }
        val p:List[UserInfo] = ((first,firstRank) :: party )
        p :: matchRanking(left)
      case Nil => Nil
    }
  }

  def createParties()  = {
    val theQueue = Queue.empty[(Connection,UserInfo)].enqueue(queue.synchronized { queue.toList })
    var (completeParties, nonCompleteParties, lobbyAssignedPlayerNew) = buildLobbies(queueMembersWithLobbyAssignments, theQueue)
    queueMembersWithLobbyAssignments = lobbyAssignedPlayerNew
    (completeParties, nonCompleteParties)
  }

  def isMatchable(firstRank: Info, ranking: Info): Boolean

}



trait LobbyHandler {
  def removeConnection(p1: Connection) : Boolean
  def playerJoined(request: LobbyJoinRequest, connection: Connection)

  def numOfPlayers:Int
  def clientMode:String
}


abstract class AbstractLobbyQueueHandler[T](gameAndRulesId: GameAndRulesId) extends LobbyHandler {

  type Info = T
  type UserInfo = (AbstractUser,Info)

  val gameServerSettings =  GameServerRepository.findBy(gameAndRulesId).getOrElse(throw new RuntimeException("Not found " + gameAndRulesId))
  var queue = scala.collection.mutable.Queue.empty[(Connection,(AbstractUser, Info))]
  var queueMembersWithLobbyAssignments = List.empty[(AbstractUser,Int)]

  //def removePartyFromQueue(party:List[(AbstractUser)]) : List[(Connection,AbstractUser)]

  def numOfPlayers : Int
  // TODO : Fixme
  def clientMode = "normal"

  //def evaluateQueue() : Unit

  def customize(u:AbstractUser) : T

  def createParties() : (List[List[AbstractUser]],List[List[AbstractUser]])

  def allocateServer(p:ProcessToken => Future[ProcessToken]) = {
    ServerAllocator.serverAllocator.allocate(p)
  }

  def removePartyFromQueue(party:List[AbstractUser]) = {
    val p = queue.synchronized {
      val (p, queueNew ) =  queue.partition { case (c,(p,i)) => party.exists(_ == p) }
      queue = queueNew
      p
    }
    p.map { case (p, (u, i)) => (p,u) } toList
  }

  def playerJoined(request: LobbyJoinRequest, connection: Connection) {
    addPlayer(request, connection)

    evaluateQueue()
  }

  def addPlayer(request: LobbyJoinRequest, connection: Connection) {
    val api = request.userIdOpt.map(new RegedUser(_)).getOrElse {
      if (request.name == "") throw new RuntimeException("YO")
      new AnonUser(request.name)
    }
    queue.synchronized {
      queue += Pair(connection, (api, customize(api)))
    }
  }

  def evaluateQueue() {

    val (completeParties,nonCompleteParties) = createParties

    import scala.concurrent.ExecutionContext.Implicits.global
    completeParties.foreach { party =>

      scala.concurrent.future {
        val completeInfo = party.flatMap( p => queue.find {case (c,(u,i)) => u == p} )
        completeInfo.foreach { case (c,(u,i)) => c.sendTCP(new ProgressUpdated(party.size)) }
      }

      val launcher = (pt:ProcessToken) => {
        val partyAndCon = removePartyFromQueue(party)
        val f = launchServerInstance(gameServerSettings, partyAndCon, pt)
        f
      }
      val allocation = allocateServer(launcher)

      allocation onComplete {
        case Success(i) =>
          // TODO : Send lobby created
          log.error("Allocation success for process token : " + i)
        case Failure(t) =>
          // TODO : Send lobby destroyed
          log.error("Allocation went bad - reinserting party " + t)
      }
    }
    nonCompleteParties.foreach { party =>
    // TODO: Not scalable
      val completeInfo = party.flatMap( p => queue.find {case (c,(u,i)) => u == p} )
      completeInfo.foreach { case (c,(u,i)) => c.sendTCP(new ProgressUpdated(party.size)) }
    }
  }


  def reserveSeats(gameSessionId:GameSessionId, party: List[(Connection, AbstractUser)]) : List[(Connection, AbstractUser, GameServerReservationId)]

  def launchServerInstance(settings:GameProcessTemplate, party: List[(Connection, AbstractUser)], processToken:ProcessToken) = {

    log.info("New queue size " + queue.size)

    val gameSessionId = gameReservationService.allocateGameSession()
    var runningGame: RunningGame = GameServerPool.pool.spawnServer(settings, new NonPersisentGameOccassion(gameSessionId),processToken)
    val partyWithSeatsReserved = reserveSeats(gameSessionId, party)

    sendStartGameInstructionsToParty(partyWithSeatsReserved, gameSessionId, runningGame)
    runningGame.done
  }

  def removeConnection(p1: Connection): Boolean = {

    queue.synchronized { queue.dequeueFirst(_._1.getID == p1.getID) } match {
      case None => log.warn("UNKNOWN disconnect")
      false
      case Some(c) => log.info("Removing disconnect")
      true
    }
  }
}
