package se.bupp.cs3k.server

import facade.lobby.AbstractLobbyQueueHandler
import model.AnonUser
import model.Model._
import model.{AnonUser, AbstractUser, GameOccassion}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import se.bupp.cs3k.LobbyJoinRequest
import service.gameserver.GameServerRepository
import com.esotericsoftware.kryonet.Connection
import collection.immutable.{Queue, ListMap}
import scala.Some
import service.resourceallocation.ServerAllocator
import scala.util.{Failure, Success}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-04
 * Time: 23:47
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class LobbyServerTest extends Specification with Mockito {

  val LEIF = "leffe"
  val PETER = "peter"
  val SVEN = "sven"
  val ROLF = "rolf"
  val INGE = "inge"
  val PER = "per"
  val NILS = "nils"

  val ranking = ListMap(
      LEIF -> 2,
      PETER -> 5,
      SVEN -> 11,
      ROLF -> 8,
      INGE -> 1,
      PER -> 7,
      NILS -> 9
    )


  class TestLobbyQueueHandler(val numOfTeams:Int, val numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) extends AbstractLobbyQueueHandler[(AbstractUser,Int)](gameAndRulesId) {

    import AbstractLobbyQueueHandler._

    //val numOfMatchingAParty = 2


    def customize(u: AbstractUser) = u match {
      case AnonUser(name) => (u,ranking.apply(name))
    }


    def buildLobbies(oldQueueMembersWithLobbyAssignments:List[(AbstractUser,Int)], theQueue:Queue[(Connection,Info)]) : (List[List[AbstractUser]],List[(AbstractUser,Int)]) = {
      val disolvedLobbyIds = oldQueueMembersWithLobbyAssignments.filterNot {
        case (u, t) => theQueue.exists {
          case (c, (uu, _)) => u == uu
        }
      }.map( _._2)
      var oldQueueMembersWithLobbyAssignmentsRev = oldQueueMembersWithLobbyAssignments.filterNot { case (u,t) => disolvedLobbyIds.exists ( _ ==  t) }
      val evaluatableQueue = theQueue.filterNot { case (c,(u,r)) => oldQueueMembersWithLobbyAssignmentsRev.exists(_._1 == u) }.toList

      val matches = matchRanking(evaluatableQueue.map(_._2))

      val completeParties = matches.filter( p => p.size == numOfPlayers * numOfTeams).map( l => l.map( _._1))

      val completePartiesIndexed = completeParties.zipWithIndex

      queueMembersWithLobbyAssignments = theQueue.map { case (c,(u,i)) =>
        completePartiesIndexed.find { case (party,_) => party.exists { case (cpu) => u == cpu } } match {
          case Some((_,idx)) => (u, Some(idx))
          case None => (u,None)
        }
      }.collect { case (u, Some(t)) => (u,t) }.toList

      (completeParties, queueMembersWithLobbyAssignments)
    }

    def removePartyFromQueue(party:List[AbstractUser]) = {
      val (p, queueNew ) =  queue.partition( p => party.exists(_ == p))
      queue = queueNew
      p.map { case (p, (u, i)) => (p,u) } toList
    }

    def evaluateQueue() {
      val theQueue = Queue.empty[(Connection,Info)].enqueue(queue.toList)
      var (completeParties, asdf) = buildLobbies(queueMembersWithLobbyAssignments, theQueue)

      import scala.concurrent.ExecutionContext.Implicits.global
      completeParties.foreach { party =>

        val launcher = (pt:ProcessToken) => {
          val partyAndCon = removePartyFromQueue(party)
          val runningGame = launchServerInstance(gameServerSettings, partyAndCon, pt)
          runningGame.done
        }
        val allocation = ServerAllocator.serverAllocator.allocate(launcher)

        allocation onComplete {
          case Success(i) =>
          // TODO : Send lobby created

          //launchServerInstance(gameServerSettings,party)
          case Failure(t) =>
            // TODO : Send lobby destroyed
            log.error("Allocation went bad - reinserting party " + t)
        }
      }
    }

    def doIt() {

    }

    def matchRanking(queue:List[(AbstractUser,Int)]) : List[(List[(AbstractUser,Int)])] = {
      queue match {
        case (first, firstRank) :: tail =>
          println(first.asInstanceOf[AnonUser].name)
          val (party,left) = tail.foldLeft((List[Info](),List[Info]())) {
            case ((rr,lr), (u, ranking) ) => {
              val numNeededForGame = ((numOfTeams * numOfPlayers) - 1)
              if(math.abs(firstRank-ranking) <= 2 && rr.size < numNeededForGame)
                (rr.+: (u,ranking), lr)
              else
                (rr, lr.+: (u,ranking))
            }
          }
          val p:List[(AbstractUser, Int)] = ((first,firstRank) :: party )
          p :: matchRanking(left)
        case Nil => Nil
      }
    }


  }

  GameServerRepository.addProcessTemplate(('A,'B),null)

  def getCon(id:Int) = {
    val connection1 = mock[Connection]
    connection1.getID() returns id
    connection1
  }

  "lobby server" should {
    "bupps" in {
      val handler = new TestLobbyQueueHandler(2,1,('A,'B))
      val queue = ranking.map {
        case (k, v) => (AnonUser(k), v)
      }.toList

      val matching = handler.matchRanking(queue)

      matching must haveTheSameElementsAs(List(
        List((AnonUser(LEIF),2), (AnonUser(INGE),1)),
        List((AnonUser(NILS),9), (AnonUser(PER),7)),
        List((AnonUser(PETER),5)),
        List((AnonUser(ROLF),8)),
        List((AnonUser(SVEN),11))))
    }


    "asdf" in {
      /*
      val lobby = new LalLobbyHandler(2,null)
      lobby.playerJoined(new LobbyJoinRequest(1,"tja"))
      lobby.playerJoined(new LobbyJoinRequest(1,"tja"))
         */

      val handler = new TestLobbyQueueHandler(1,1,('A,'B))

      val nullLong= null.asInstanceOf[java.lang.Long]
      handler.playerJoined(new LobbyJoinRequest(-1, LEIF), getCon(1))
      handler.playerJoined(new LobbyJoinRequest(-1, PETER), getCon(2))
      handler.playerJoined(new LobbyJoinRequest(-1, SVEN), getCon(3))
      handler.playerJoined(new LobbyJoinRequest(-1, ROLF), getCon(4))
      handler.playerJoined(new LobbyJoinRequest(-1, INGE), getCon(5))
      handler.playerJoined(new LobbyJoinRequest(-1, PER), getCon(6))
      handler.playerJoined(new LobbyJoinRequest(-1, NILS), getCon(7))


      // test disconnect


      handler.evaluateQueue()

      1 === 1

    }

  }
}
