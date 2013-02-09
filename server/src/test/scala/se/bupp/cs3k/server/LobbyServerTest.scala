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
import scala.concurrent.{promise}

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
      SVEN -> 13,
      ROLF -> 9,
      INGE -> 1,
      PER -> 8,
      NILS -> 10
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
          //val runningGame = launchServerInstance(gameServerSettings, partyAndCon, pt)
          val p = promise[ProcessToken]
          p.future
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
                (rr.:+ (u,ranking), lr)
              else
                (rr, lr.:+ (u,ranking))
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
        List((AnonUser(PETER),5)),
        List((AnonUser(SVEN),13)),
        List((AnonUser(ROLF),9), (AnonUser(PER),8)),
        List((AnonUser(NILS),10))
      ))
    }


    "asdf" in {
      val list = List((new LobbyJoinRequest(-1, LEIF), getCon(1)),
        (new LobbyJoinRequest(-1, PETER), getCon(2)),
        (new LobbyJoinRequest(-1, SVEN), getCon(3)),
        (new LobbyJoinRequest(-1, ROLF), getCon(4)),
        (new LobbyJoinRequest(-1, INGE), getCon(5)),
        (new LobbyJoinRequest(-1, PER), getCon(6)),
        (new LobbyJoinRequest(-1, NILS), getCon(7)))

      def con(s:String) = {
        list.find(_._1.getName == s).map(_._2).get
      }
      /*
      val lobby = new LalLobbyHandler(2,null)
      lobby.playerJoined(new LobbyJoinRequest(1,"tja"))
      lobby.playerJoined(new LobbyJoinRequest(1,"tja"))
         */

      val handler = new TestLobbyQueueHandler(2,1,('A,'B))

      val nullLong= null.asInstanceOf[java.lang.Long]


      list.foreach { a => (handler.playerJoined _).tupled(a) }

      // test disconnect
      var theQueue = Queue.empty[(Connection,handler.Info )].enqueue(handler.queue.toList)
      var (completeParties1, assigned1) = handler.buildLobbies(Nil, theQueue)
        //[[AnonUser(leffe)], [AnonUser(nils)], [AnonUser(peter)], [AnonUser(per)], [AnonUser(sven)], [AnonUser(inge)], [AnonUser(rolf)]
      completeParties1 must haveTheSameElementsAs(List(List(AnonUser(LEIF), AnonUser(INGE)), List(AnonUser(ROLF), AnonUser(PER))))
      assigned1 must haveTheSameElementsAs(List((AnonUser(LEIF),0), (AnonUser(INGE),0), (AnonUser(ROLF),1), (AnonUser(PER),1)))

      handler.removeConnection(con(NILS))
      1 === 1

      theQueue = Queue.empty[(Connection,handler.Info )].enqueue(handler.queue.toList)
      var (completeParties2, assigned2) = handler.buildLobbies(assigned1, theQueue)

      completeParties2 must haveTheSameElementsAs(List(List(AnonUser(LEIF), AnonUser(INGE)), List(AnonUser(NILS), AnonUser(PER))))
      assigned2 must haveTheSameElementsAs(List((AnonUser(LEIF),0), (AnonUser(INGE),0), (AnonUser(NILS),1), (AnonUser(PER),1)))

    }

  }
}
