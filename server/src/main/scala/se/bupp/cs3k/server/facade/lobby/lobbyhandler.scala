package se.bupp.cs3k.server.facade.lobby

import org.slf4j.{LoggerFactory, Logger}
import com.esotericsoftware.kryonet.{Listener, Server, KryoSerialization, Connection}
import se.bupp.cs3k.server.model._
import java.util.concurrent.{TimeUnit, Executors}
import java.net.URL
import se.bupp.cs3k._
import se.bupp.cs3k.server.{Cs3kConfig}
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


  def sendStartGameInstructionsToParty(party:List[(Connection,AbstractUser)], gameSessionId : GameSessionId, runningGame: RunningGame) {
    val scheduler = Executors.newScheduledThreadPool(1);
    log.info("creating delayed game launch announcer task " + party.size)
    val beeper = new Runnable() {
      def  run() {
        log.info("In delayed game launch announcer " + party.size)
        party.foreach { case (c,pi) =>

          try {
            val reservationId = gameReservationService.reserveSeat(gameSessionId, pi, None)

            log.info("Reserving seat and sending sending start game instructions")
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


abstract class AbstractLobbyQueueHandler[T](gameAndRulesId: GameServerRepository.GameAndRulesId) {
  import AbstractLobbyQueueHandler._

  type Info = T
  type UserInfo = (AbstractUser,Info)

  val gameServerSettings =  GameServerRepository.findBy(gameAndRulesId).getOrElse(throw new RuntimeException("Not found " + gameAndRulesId))

  var queue = scala.collection.mutable.Queue.empty[(Connection,(AbstractUser, Info))]
  var queueMembersWithLobbyAssignments = List.empty[(AbstractUser,Int)]

  //def removePartyFromQueue(party:List[(AbstractUser)]) : List[(Connection,AbstractUser)]

  def evaluateQueue() : Unit

  def customize(u:AbstractUser) : T

  def allocateServer(p:ProcessToken => Future[ProcessToken]) = {
    ServerAllocator.serverAllocator.allocate(p)
  }

  def removePartyFromQueue(party:List[AbstractUser]) = {
    val (p, queueNew ) =  queue.partition( p => party.exists(_ == p))
    queue = queueNew
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
    queue += Pair(connection, (api, customize(api)))
  }

  def launchServerInstance(settings:GameProcessTemplate, party: List[(Connection, AbstractUser)], processToken:ProcessToken) = {

    log.info("New queue size " + queue.size)

    val gameSessionId = gameReservationService.allocateGameSession()
    var runningGame: RunningGame = GameServerPool.pool.spawnServer(settings, new NonPersisentGameOccassion(gameSessionId),processToken)

    sendStartGameInstructionsToParty(party, gameSessionId, runningGame)
    runningGame.done
  }

  def removeConnection(p1: Connection): Boolean = {
    queue.dequeueFirst(_._1.getID == p1.getID) match {
      case None => log.info("Removing UNKNOWN disconnect ")
      false
      case Some(c) => log.info("Removing disconnect")
      true
      //queue = mutable.Queue.empty[Connection].dequeue++ queue.drop(i)
    }
  }
}


class RankedTeamLobbyQueueHandler(val numOfTeams:Int, val numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) extends AbstractLobbyQueueHandler[Int](gameAndRulesId) {

  import AbstractLobbyQueueHandler._


  def customize(u: AbstractUser) = u match {
    case RegedUser(id) => rankingService.getRanking(id).getOrElse(0)
    case _ => throw new IllegalArgumentException("Ranked lobby cant handle anon players")
  }

  def buildLobbies(oldQueueMembersWithLobbyAssignments:List[(AbstractUser,Int)], theQueue:Queue[(Connection,(AbstractUser, Info))]) : (List[List[AbstractUser]],List[(AbstractUser,Int)]) = {
    val disolvedLobbyIds = oldQueueMembersWithLobbyAssignments.filterNot {
      case (u, t) => theQueue.exists {
        case (c, (uu, _)) => u == uu
      }
    }.map( _._2)
    var oldQueueMembersWithLobbyAssignmentsRev = oldQueueMembersWithLobbyAssignments.filterNot { case (u,t) => disolvedLobbyIds.exists ( _ ==  t) }
    val evaluatableQueue = theQueue.filterNot { case (c,(u,r)) => oldQueueMembersWithLobbyAssignmentsRev.exists(_._1 == u) }.toList

    val matches = matchRanking(evaluatableQueue.map(_._2))

    val completeParties = matches.filter( p => p.size == numOfPlayers * numOfTeams).map( l => l.map( _._1))

    val completePartiesIndexed = (oldQueueMembersWithLobbyAssignmentsRev.groupBy(_._2).values.map(_.map(_._1)).toList ::: completeParties).zipWithIndex

    val queueMembersWithLobbyAssignmentsNew = theQueue.map { case (c,(u,i)) =>
      completePartiesIndexed.find { case (party,_) => party.exists { case (cpu) => u == cpu } } match {
        case Some((_,idx)) => (u, Some(idx))
        case None => (u,None)
      }
    }.collect { case (u, Some(t)) => (u,t) }.toList

    (completeParties, queueMembersWithLobbyAssignmentsNew)
  }



  def evaluateQueue() {
    val theQueue = Queue.empty[(Connection,UserInfo)].enqueue(queue.toList)
    var (completeParties, lobbyAssignedPlayerNew) = buildLobbies(queueMembersWithLobbyAssignments, theQueue)
    queueMembersWithLobbyAssignments = lobbyAssignedPlayerNew


    import scala.concurrent.ExecutionContext.Implicits.global
    completeParties.foreach { party =>

      val launcher = (pt:ProcessToken) => {
        val partyAndCon = removePartyFromQueue(party)
        val f = launchServerInstance(gameServerSettings, partyAndCon, pt)
        f
      }
      val allocation = allocateServer(launcher)

      allocation onComplete {
        case Success(i) =>
        // TODO : Send lobby created

        case Failure(t) =>
          // TODO : Send lobby destroyed
          log.error("Allocation went bad - reinserting party " + t)
      }
    }
  }

  def matchRanking(queue:List[UserInfo]) : List[(List[UserInfo])] = {
    queue match {
      case (first, firstRank) :: tail =>
        println(first.asInstanceOf[AnonUser].name)
        val (party,left) = tail.foldLeft((List[UserInfo](),List[UserInfo]())) {
          case ((rr,lr), (u, ranking) ) => {
            val numNeededForGame = ((numOfTeams * numOfPlayers) - 1)
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

  def isMatchable(firstRank: Info, ranking: Info): Boolean = {
    math.abs(firstRank - ranking) <= 2
  }
}



/*
class TeamLobbyQueueHandler(val numOfTeams:Int, val numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) extends AbstractLobbyQueueHandler[AbstractUser](gameAndRulesId) {

  import AbstractLobbyQueueHandler._

  def customize(u: AbstractUser) = u

  def evaluateQueue() {

  }

}
*/


class NonTeamLobbyQueueHandler(val numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) extends AbstractLobbyQueueHandler[None.type](gameAndRulesId) {
  import AbstractLobbyQueueHandler._


  def customize(u: AbstractUser) = None

  def createParty = {
    var party = List[(Connection,AbstractUser)]()

    log.info("Removing " + numOfPlayers + " players from a queue of " + queue.size)

    (0 until numOfPlayers).foreach { s =>
      val c = queue.dequeue()
      party = (c._1,c._2._1) :: party
    }
    party
  }

  /*def removePartyFromQueue(party:List[(Connection,AbstractUser)])  {
    queue =  queue.filter( p => party.exists(_ == p))
  }*/

  def evaluateQueue() {
    //if (queue.size >= numOfPlayers) {

      /*GameServerRepository.findBy(gameAndRulesId) match {
        case Some(gameServerSettings) =>
      */
    val party = createParty
    import scala.concurrent.ExecutionContext.Implicits.global

    val launcher = (pt:ProcessToken) => {
      val partyWithCon = removePartyFromQueue(party.map(_._2))
      val f = launchServerInstance(gameServerSettings, partyWithCon, pt)
      f
    }
    val allocation = allocateServer(launcher)

    allocation onComplete {
      case Success(i) =>
        // TODO : Send lobby created

        //launchServerInstance(gameServerSettings,party)
      case Failure(t) =>
        // TODO : Send lobby destroyed
        log.error("Allocation went bad - reinserting party " + t)
    }
        //case None => log.error("Unknown game server setting : " + gameAndRulesId)
  }
}
