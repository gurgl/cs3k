package se.bupp.cs3k.server

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import se.bupp.cs3k.LobbyJoinRequest
import service.resourceallocation.ServerAllocator
import ServerAllocator.{AllocateAccept, Allocate}
import scala.util.Success

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-04
 * Time: 23:47
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class ServerAllocatorTest extends Specification with Mockito {

  "server allocator " should {
    "allocata" in {

      val allocator = new ServerAllocator(2)

      val al1 = allocator.allocate()
      val al2 = allocator.allocate()
      val al3 = allocator.allocate()

      /*al1.value === Some(Success(0))
      al2.value === Some(Success(1))
      al3.value === None*/

      al1.isCompleted === true
      al2.isCompleted === true
      al3.isCompleted === false


    }
  }
}
