package se.bupp.cs3k.server

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import se.bupp.cs3k.LobbyJoinRequest
import se.bupp.cs3k.server.ServerAllocator.{AllocateAccept, Allocate}

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


      val allocator = ServerAllocator.serverAllocator
      allocator.start()

      val res = allocator ! Allocate()

      res must beAnInstanceOf[AllocateAccept]


    }

  }
}
