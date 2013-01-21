package se.bupp.cs3k.server

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import service.resourceallocation.{AllocatablePortRange, AllocatedResourceSet, ResourceAllocator, ResourceNeeds}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-20
 * Time: 20:00
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class ResourceAllocatorTest extends Specification {

  "Port allocator" should {
    "asdf" in {
      var range: AllocatablePortRange = new AllocatablePortRange(Range(1, 3), "TCP Range")
      range.reservePort() === Some(1)
      range.reservePort() === Some(2)
    }
  }
  "ResourceAllocator" should {
    "allocate resources if available" in {
      var allocator: ResourceAllocator = new ResourceAllocator
      allocator.tcpRange = new AllocatablePortRange(Range(1,3), "TCP Range")
      allocator.udpRange = new AllocatablePortRange(Range(4,6), "UDP Range")
      var allocation: Option[AllocatedResourceSet] = allocator.allocate(new ResourceNeeds(1, 2))
      allocation match {
        case Some(x) =>
          x.tcps === Set(1)
          x.udps === Set(4,5)
        case None => failure("asdf")
      }
      1 === 1
    }
  }




}
