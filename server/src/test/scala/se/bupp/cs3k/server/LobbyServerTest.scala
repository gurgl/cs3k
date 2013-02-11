package se.bupp.cs3k.server

import facade.lobby.{AnonTeamLobbyQueueHandler, AbstractLobbyQueueHandler}
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
import scala.util.{Random, Failure, Success}
import concurrent.{Future, promise, Promise}


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

  val serverAllocator = mock[ServerAllocator]
  class TestRankedTeamLobbyQueueHandler(t:Int,p:Int) extends AnonTeamLobbyQueueHandler(t,p,('A,'B)) {
    var launchRequests = List[Promise[ProcessToken]]()
    override def launchServerInstance(settings: GameProcessTemplate, party: List[(Connection, AbstractUser)], processToken: ProcessToken) = {
      var serverDone = promise[ProcessToken]
      launchRequests = launchRequests :+ serverDone
      serverDone.future
    }

    override def customize(u: AbstractUser) = u match {
      case AnonUser(name) => ranking.apply(name)
      case _ => throw new IllegalArgumentException("na")
    }

    override def allocateServer(p: (Model.ProcessToken) => Future[Model.ProcessToken]) = {
      serverAllocator.allocate(p)
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
      var (completeParties1, nonCompleteParties1, assigned1) = handler.buildLobbies(Nil, theQueue)
      handler.queueMembersWithLobbyAssignments = assigned1

      completeParties1 must haveTheSameElementsAs(List(List(AnonUser(LEIF), AnonUser(INGE)), List(AnonUser(ROLF), AnonUser(PER))))
      assigned1 must haveTheSameElementsAs(List((AnonUser(LEIF),0), (AnonUser(INGE),0), (AnonUser(ROLF),1), (AnonUser(PER),1)))


      handler.removeConnection(con(PER))

      theQueue = Queue.empty[(Connection,handler.UserInfo )].enqueue(handler.queue.toList)
      var (completeParties2, nonCompleteParties2, assigned2) = handler.buildLobbies(assigned1, theQueue)
      handler.queueMembersWithLobbyAssignments = assigned2

      completeParties2 must haveTheSameElementsAs(List(List(AnonUser(ROLF), AnonUser(NILS))))
      assigned2 must haveTheSameElementsAs(List((AnonUser(LEIF),0), (AnonUser(INGE),0), (AnonUser(NILS),1), (AnonUser(ROLF),1)))

      handler.queue.size === 6
      handler.removePartyFromQueue(List((AnonUser(LEIF)), (AnonUser(INGE))))
      handler.queue.size === 4

      theQueue = Queue.empty[(Connection,handler.UserInfo )].enqueue(handler.queue.toList)
      var (completeParties3, nonCompleteParties3, assigned3) = handler.buildLobbies(assigned2, theQueue)
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

      (0 to 3).foreach { i =>
        handler.launchRequests.size === 0
        there was no(serverAllocator).allocate(any)
        (handler.playerJoined _).tupled(list(i))
      }

      import scala.concurrent.ExecutionContext.Implicits.global

      var p = promise[ProcessToken]
      serverAllocator.allocate(any) answers {
        (para,b) => {
            val onDone = para.asInstanceOf[Array[_]](0).asInstanceOf[ProcessToken => Future[ProcessToken]]
            val fu = p.future
            fu onComplete {
              case Success(i) => onDone(i)
            }
            fu
        }
      }
      (handler.playerJoined _).tupled(list(4))
      handler.launchRequests.size === 0

      var doneRes = -1
      p.future onComplete {
        case _ =>
          Thread.sleep(100)
          doneRes = handler.launchRequests.size
      }
      p success 1


      doneRes must be_==(1).eventually
      // test disconnect
      /*var theQueue = Queue.empty[(Connection,handler.UserInfo )].enqueue(handler.queue.toList)
      var (completeParties1, assigned1) = handler.buildLobbies(Nil, theQueue)*/

      //completeParties1 must haveTheSameElementsAs(List(List(AnonUser(LEIF), AnonUser(INGE)), List(AnonUser(ROLF), AnonUser(PER))))
      //assigned1 must haveTheSameElementsAs(List((AnonUser(LEIF),0), (AnonUser(INGE),0), (AnonUser(ROLF),1), (AnonUser(PER),1)))

      handler.removeConnection(con(PER))
      1 === 1
      //theQueue = Queue.empty[(Connection,handler.UserInfo )].enqueue(handler.queue.toList)
      //var (completeParties2, assigned2) = handler.buildLobbies(assigned1, theQueue)

      //completeParties2 must haveTheSameElementsAs(List(List(AnonUser(ROLF), AnonUser(NILS))))
      //assigned2 must haveTheSameElementsAs(List((AnonUser(LEIF),0), (AnonUser(INGE),0), (AnonUser(NILS),1), (AnonUser(ROLF),1)))
    }

    "test alot" in {

      import scala.collection.immutable.Queue
      class FiniteQueue[A](q: Queue[A]) {
        def enqueueFinite[B >: A](elem: B, maxSize: Int): Queue[B] = {
          var ret = q.enqueue(elem)
          while (ret.size > maxSize) { ret = ret.dequeue._2 }
          ret
        }
      }
      implicit def queue2finitequeue[A](q: Queue[A]) = new FiniteQueue[A](q)

      var players = List.empty[(LobbyJoinRequest,Connection)]

      var left = List[Int]()//List.empty[(LobbyJoinRequest,Connection)]


      val serverAllocator = new ServerAllocator(2)
      val handler = new TestRankedTeamLobbyQueueHandler(2,2) {
        override def customize(u: AbstractUser) = u match {
          case AnonUser(name) => if(Random.nextInt(20) > 10) 10 else 5
          case _ => throw new IllegalArgumentException("na")
        }

        override def allocateServer(p: (Model.ProcessToken) => Future[Model.ProcessToken]) = {
          println("allocateServer")
          serverAllocator.allocate(p)
        }
      }


      var joinLoop = new Runnable() {
        def run() {
          (0 to 200).foreach {
            case i =>
              val (p,c) = (new LobbyJoinRequest(-1,"Player " + i),getCon(i))
              players = players :+ (p,c)

              Thread.sleep(Random.nextInt(100).toLong)
              handler.playerJoined(p,c)
          }
        }
      }

      var allocationsReleased = 0
      var allocationReleaser = new Runnable() {
        def run() {

          try {
            var req = Queue.empty[Int]
            var WINDOW_SIZE = 10

            var queueIsChanging= true
            while(true && queueIsChanging) {
              Thread.sleep(Random.nextInt(1000).toLong)
              queueIsChanging = if(req.size >= WINDOW_SIZE) !req.forall(_ == req.head) else true
              println("*** Alloc RELEASE" + handler.queue.size + " " + queueIsChanging)
              req = req.enqueueFinite(handler.queue.size,WINDOW_SIZE)
              while(allocationsReleased < handler.launchRequests.size ) {
                handler.launchRequests(allocationsReleased) success allocationsReleased
                allocationsReleased = allocationsReleased + 1
              }
            }
          } catch {
            case e:Throwable => e.printStackTrace
          }
        }
      }

      var leaveLoopKeepalive = true
      var leaveLoop = new Runnable() {
        def run() {
          while(leaveLoopKeepalive) {
            Thread.sleep(Random.nextInt(1000).toLong)
            var toChooseFrom = 0
            try {
              toChooseFrom = handler.queue.size
              if(toChooseFrom > 1) {
                var pick = Random.nextInt(toChooseFrom - 1)

                val rmOpt = try {
                  val c = handler.queue.synchronized { handler.queue.apply(pick)._1 }
                  Some(c)
                } catch {
                  case _ => None
                }
                rmOpt.foreach { case c =>
                  println("P Leaving " + pick)
                  handler.removeConnection(c)
                }
              } else {
                println("no players to disconnect")
              }
            } catch {
              case e:Throwable =>
                println(toChooseFrom)
                e.printStackTrace
            }
          }
        }
      }

      var joinThread = new Thread(joinLoop)
      joinThread.start()
      var leaveThread = new Thread(leaveLoop)
      leaveThread.start
      var allocRelThread = new Thread(allocationReleaser)
      allocRelThread.start()
      joinThread.isAlive must be_==(false).eventually(200,new org.specs2.time.Duration(200))
      allocRelThread.isAlive must be_==(false).eventually(2000,new org.specs2.time.Duration(2000))
      leaveLoopKeepalive = false
      leaveThread.isAlive must be_==(false).eventually(200,new org.specs2.time.Duration(2000))
      handler.queue.size must be lessThan(7)
    }
  }
}
