package se.bupp.cs3k.server.service.resourceallocation

import collection.immutable.Queue
import concurrent.{Future, Promise, future, promise}
import se.bupp.cs3k.server.Cs3kConfig

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-06
 * Time: 03:34
 * To change this template use File | Settings | File Templates.
 */

object ServerAllocator {
  case class Allocate()
  case class Free()
  case class Init(val numOf:Int)


  case class AllocateDenyQueued()
  case class DropInterrest()
  case class AllocateAccept()

  var serverAllocator = new ServerAllocator(Cs3kConfig.MAX_NUM_OF_GAME_SERVER_PROCESSES)

}

class ServerAllocator(val numOfTotalSlots:Int) {
  var queue = Queue.empty[(Promise[Int], Int => Future[Int])]

  import scala.concurrent.ExecutionContext.Implicits.global

  var numOfSlotsAllocated = 0

  private def allocation() = {
    val r = numOfSlotsAllocated
    numOfSlotsAllocated = numOfSlotsAllocated + 1
    r
  }
  def allocate(done:Int => Future[Int]) : Future[Int] = {
    if (numOfSlotsAllocated < numOfTotalSlots) {
      val token = allocation()
      future {
        handleCompletion(done, token)
      }
    } else {
      val p = promise[Int]
      queue = queue.enqueue((p,done))
      p.future
    }
  }
  private def handleCompletion(done:Int => Future[Int],token:Int) : Int = {
    done(token) onComplete {
       case _ => deallocate()
    }
    token
  }

  private def deallocate() {
    if (queue.size > 0) {
      queue = queue.dequeue match {
        case ((p, done)  , queueNew) =>
          val token = allocation()
          p success handleCompletion(done,token)
          queueNew
      }
    } else {
      numOfSlotsAllocated = numOfSlotsAllocated - 1
    }
  }
}
