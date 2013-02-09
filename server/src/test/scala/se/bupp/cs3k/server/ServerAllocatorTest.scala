package se.bupp.cs3k.server

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import se.bupp.cs3k.LobbyJoinRequest
import service.resourceallocation.ServerAllocator
import ServerAllocator.{AllocateAccept, Allocate}
import scala.util.Success
import concurrent.{Future, future }

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-04
 * Time: 23:47
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class ServerAllocatorTest extends Specification with Mockito {


  def lock(s:String) { s.synchronized { s.wait() } }
  def unlock(s:String) { s.synchronized { s.notify() } }
  "server allocator " should {
    "allocata" in {

      val allocator = new ServerAllocator(2)
      import scala.concurrent.ExecutionContext.Implicits.global
      //val apa = (x:Int) => future { x }
      val t1 = ""

      val al1 = allocator.allocate((x:Int) => future { lock(t1);x } )
      val t2 = ""
      val al2 = allocator.allocate((x:Int) => future {  lock(t2);x } )
      val t3 = ""

      val al3 = allocator.allocate((x:Int) => future {  lock(t3);x } )

      val t4 = ""
      val al4 = allocator.allocate((x:Int) => future {  lock(t4);x } )

      /*al1.value === Some(Success(0))
      al2.value === Some(Success(1))
      al3.value === None*/

      al1.isCompleted === true
      al2.isCompleted === true
      al3.isCompleted === false
      al4.isCompleted === false

      unlock(t1)

      al1.isCompleted === true
      al2.isCompleted === true
      al3.isCompleted === true
      al4.isCompleted === false

    }
  }
}
