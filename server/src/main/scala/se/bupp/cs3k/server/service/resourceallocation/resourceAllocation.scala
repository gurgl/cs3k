package se.bupp.cs3k.server.service.resourceallocation

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-21
 * Time: 00:38
 * To change this template use File | Settings | File Templates.
 */

import org.apache.log4j.Logger
import se.bupp.cs3k.server.Cs3kConfig
import collection.SortedSet



/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-21
 * Time: 00:37
 * To change this template use File | Settings | File Templates.
 */
class ResourceNeeds(val numOfTcpPorts:Int, val numOfUdpPorts:Int)

class AllocatedResourceSet(val tcps:Set[Int], val udps:Set[Int]) {}

class ResourceAllocator {
  var tcpRange = new AllocatablePortRange(Range(Cs3kConfig.TCP_RANGE_START,Cs3kConfig.TCP_RANGE_END), "TCP Range")
  var udpRange = new AllocatablePortRange(Range(Cs3kConfig.UDP_RANGE_START,Cs3kConfig.UDP_RANGE_END), "UDP Range")

  def allocate(rn:ResourceNeeds) = {
    val reservedTcp = (0 until rn.numOfTcpPorts).map( b => tcpRange.reservePort() )
    val reservedUdp = (0 until rn.numOfUdpPorts).map( b => udpRange.reservePort() )


    (reservedTcp.flatten,reservedUdp.flatten) match {
      case (tcps, udps) if (tcps.size, udps.size) == (rn.numOfTcpPorts, rn.numOfUdpPorts) => Some(new AllocatedResourceSet(tcps.toSet, udps.toSet))
      case (tcps, udps) =>

        println(tcps.size + " " +udps.size)
        unallocate(tcps, tcpRange)
        unallocate(udps, udpRange)
        None
    }
  }

  def unallocate(rs:AllocatedResourceSet) {
    unallocateTcp(IndexedSeq.empty ++ rs.tcps.toSeq)
    unallocateTcp(IndexedSeq.empty ++ rs.udps.toSeq)
  }

  def unallocate(ports:IndexedSeq[Int], range:AllocatablePortRange) {
    ports.foreach { p =>
      range.unAllocate(p)
    }
  }

  def unallocateTcp(tcps:IndexedSeq[Int]) {
    unallocate(tcps,tcpRange)
  }
  def unallocateUdp(udps:IndexedSeq[Int]) {
    unallocate(udps,udpRange)
  }
}

class AllocatablePortRange(val range:Range, val id:String) {
  val log = Logger.getLogger(classOf[AllocatablePortRange])

  var allocated = SortedSet[Int]()

  def reservePort() : Option[Int] = {
    range.find(!allocated.contains(_))
      .map {
      p =>
        allocated = allocated + p
        p
    }
  }
  def unAllocate(p:Int) {
    if (allocated.contains(p)) {
      allocated = allocated - p
      log.info("Unnallocated" + p + " from " + id)
    } else
      log.warn("Bad unallocation req :  " + p + " from " + id)
  }
}

