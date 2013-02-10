package se.bupp.cs3k.server

import facade.lobby.{RankedTeamLobbyQueueHandler, AbstractLobbyQueueHandler}
import model._
import model.AnonUser
import model.Model._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import se.bupp.cs3k.LobbyJoinRequest
import service.gameserver.{GameProcessTemplate, GameServerRepository}
import com.esotericsoftware.kryonet.Connection
import collection.immutable.{Queue, ListMap}
import scala.Some
import service.resourceallocation.ServerAllocator
import scala.util.{Failure, Success}
import scala.concurrent.{promise, Promise}


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

  GameServerRepository.addProcessTemplate(('A,'B),null)

  class TestRankedTeamLobbyQueueHandler(t:Int,p:Int) extends RankedTeamLobbyQueueHandler(t,p,('A,'B)) {
    var launchRequests = List[Promise[ProcessToken]]()
    override def launchServerInstance(settings: GameProcessTemplate, party: List[(Connection, AbstractUser)], processToken: ProcessToken) = {
      var p = promise[ProcessToken]
      launchRequests = launchRequests :+ p
      p.future
    }

    override def customize(u: AbstractUser) = u match {
      case AnonUser(name) => ranking.apply(name)
      case _ => throw new IllegalArgumentException("na")
    }
  }

  def getCon(id:Int) = {
    val connection1 = mock[Connection]
    connection1.getID() returns id
    connection1
  }

  "lobby server" should {
    "bupps" in {
      val handler = new TestRankedTeamLobbyQueueHandler(2,1) {}
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


    "custom evaluate" in {
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


      val handler = new TestRankedTeamLobbyQueueHandler(2,1) {}


      list.foreach { a => (handler.addPlayer _).tupled(a) }

      // test disconnect
      var theQueue = Queue.empty[(Connection,handler.UserInfo)].enqueue(handler.queue.toList)
      var (completeParties1, assigned1) = handler.buildLobbies(Nil, theQueue)
      handler.queueMembersWithLobbyAssignments = assigned1

      completeParties1 must haveTheSameElementsAs(List(List(AnonUser(LEIF), AnonUser(INGE)), List(AnonUser(ROLF), AnonUser(PER))))
      assigned1 must haveTheSameElementsAs(List((AnonUser(LEIF),0), (AnonUser(INGE),0), (AnonUser(ROLF),1), (AnonUser(PER),1)))


      handler.removeConnection(con(PER))

      theQueue = Queue.empty[(Connection,handler.UserInfo )].enqueue(handler.queue.toList)
      var (completeParties2, assigned2) = handler.buildLobbies(assigned1, theQueue)
      handler.queueMembersWithLobbyAssignments = assigned2

      completeParties2 must haveTheSameElementsAs(List(List(AnonUser(ROLF), AnonUser(NILS))))
      assigned2 must haveTheSameElementsAs(List((AnonUser(LEIF),0), (AnonUser(INGE),0), (AnonUser(NILS),1), (AnonUser(ROLF),1)))

      handler.queue.size === 6
      handler.removePartyFromQueue(List((AnonUser(LEIF)), (AnonUser(INGE))))
      handler.queue.size === 4

      theQueue = Queue.empty[(Connection,handler.UserInfo )].enqueue(handler.queue.toList)
      var (completeParties3, assigned3) = handler.buildLobbies(assigned2, theQueue)
      handler.queueMembersWithLobbyAssignments = assigned3

      completeParties3 must haveTheSameElementsAs(Nil)
      assigned3 must haveTheSameElementsAs(List((AnonUser(NILS),0), (AnonUser(ROLF),0)))
    }

    "test 2" in {
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


      val handler = new TestRankedTeamLobbyQueueHandler(2,1) {}


      list.foreach { a => (handler.playerJoined _).tupled(a) }

      // test disconnect
      var theQueue = Queue.empty[(Connection,handler.UserInfo )].enqueue(handler.queue.toList)
      var (completeParties1, assigned1) = handler.buildLobbies(Nil, theQueue)

      completeParties1 must haveTheSameElementsAs(List(List(AnonUser(LEIF), AnonUser(INGE)), List(AnonUser(ROLF), AnonUser(PER))))
      assigned1 must haveTheSameElementsAs(List((AnonUser(LEIF),0), (AnonUser(INGE),0), (AnonUser(ROLF),1), (AnonUser(PER),1)))

      handler.removeConnection(con(PER))

      theQueue = Queue.empty[(Connection,handler.UserInfo )].enqueue(handler.queue.toList)
      var (completeParties2, assigned2) = handler.buildLobbies(assigned1, theQueue)

      completeParties2 must haveTheSameElementsAs(List(List(AnonUser(ROLF), AnonUser(NILS))))
      assigned2 must haveTheSameElementsAs(List((AnonUser(LEIF),0), (AnonUser(INGE),0), (AnonUser(NILS),1), (AnonUser(ROLF),1)))
    }
  }
}
