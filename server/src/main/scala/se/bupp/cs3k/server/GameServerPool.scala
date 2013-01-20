package se.bupp.cs3k.server

import model.RunningGame
import model.{AbstractGameOccassion, RunningGame}
import org.apache.commons.exec._

import java.net.URL
import se.bupp.cs3k.server.service.GameReservationService._
import org.apache.log4j.Logger
import se.bupp.cs3k.server.model.Model.UserId
import scala.collection.immutable.HashMap
import collection.{mutable, SortedSet}



/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-15
 * Time: 17:17
 * To change this template use File | Settings | File Templates.
 */


class GameServerSpecification(val cmdStr:String,val resourceNeeds:ResourceNeeds) {

  def create(args:String, jnlpPath:String, props:Map[String,String]) = {
    new GameProcessTemplate(cmdStr + args, "http://" + LobbyServer.remoteIp + ":8080/" + jnlpPath, props, this)
  }
}

class ResourceNeeds(val numOfTcpPorts:Int, val numOfUdpPorts:Int)

class GameProcessSettings(private var commandLine:String, var clientJNLPUrl:String, val props:Map[String,String], val gpt:GameProcessTemplate) {
  def cmdLine(extra:String) = CommandLine.parse(commandLine + extra);

  def jnlpUrl(reservationId:SeatId,name:String) : URL = new URL(clientJNLPUrl + "?reservation_id=" + reservationId+"&player_name=" + name)
  def jnlpUrl(reservationId:SeatId,name:UserId) : URL = new URL(clientJNLPUrl + "?reservation_id=" + reservationId+"&user_id=" + name)

}



class ResourceSet(val tcps:Set[Int], val udps:Set[Int]) {

}

object GameProcessTemplate {
  val TcpPortExpression = """\$\{tcp\[(\d*)\]\}""".r
  val UdpPortExpression = """\$\{udp\[(\d*)\]\}""".r
  val Cs3kHostExpression = """\$\{cs3k_port\}""".r
  val Cs3kPortExpression = """\$\{cs3k_host\}""".r
  def applyInstanceSpecificResources(s:String, resource:ResourceSet) = {

    val cmdLineWithTcpAndUdp = Map(TcpPortExpression -> resource.tcps,UdpPortExpression -> resource.udps).foldLeft(s) {
      case (cmdLien, (pattern, resources)) =>
        var resourcesLeft:Seq[Int] = Seq(resources.toSeq:_*)
        resourcesLeft.lift(3)
        val repl = pattern.replaceAllIn(cmdLien,
          matcher => {
            val index: Int = matcher.group(1).toInt
            val removedOpt = resourcesLeft.lift.apply(index)
            removedOpt.map { reducedValue =>
              val res = reducedValue
              val (s,e) = resourcesLeft splitAt index
              resourcesLeft = s ++ (e drop 1)

              "" + res + ""
            }.getOrElse(throw new IllegalArgumentException("Matcher " + matcher.toString() + " aint defined at " +  index))
          }
        )
        if(resourcesLeft.size > 0) {
          throw new IllegalArgumentException("Not all requested resources used")
        }
        repl
    }

    val cmdLineWithMasterHost = Cs3kHostExpression.replaceAllIn(cmdLineWithTcpAndUdp,
      matcher => {
        val res = Cs3kConfig.CS3K_HOST
        "" + res + ""
      })

    val cmdLineWithMasterPort = Cs3kPortExpression.replaceAllIn(cmdLineWithMasterHost,
      matcher => {
        val res = Cs3kConfig.CS3K_PORT
        "" + res + ""
      })

    cmdLineWithMasterPort
  }





}
class GameProcessTemplate(private var commandLineTemplate:String, var clientJNLPUrl:String, val props:Map[String,String],val gameSpecification:GameServerSpecification) {

  import GameProcessTemplate._
  //def cmdLine(extra:String) = CommandLine.parse(commandLine + extra);

  //def jnlpUrl(reservationId:SeatId,name:String) : URL = new URL(clientJNLPUrl + "?reservation_id=" + reservationId+"&player_name=" + name)
  //def jnlpUrl(reservationId:SeatId,name:UserId) : URL = new URL(clientJNLPUrl + "?reservation_id=" + reservationId+"&user_id=" + name)

  def specifyInstance(resourceSet:ResourceSet) : GameProcessSettings = {
    val commandLine = applyInstanceSpecificResources(commandLineTemplate, resourceSet)
    new GameProcessSettings(commandLine,clientJNLPUrl,props,this)

  }

}


object GameServerRepository  {

  type GameServerTypeId = Symbol
  type GameProcessSettingsId = Symbol
  type GameAndRulesId = (GameServerTypeId, GameProcessSettingsId)

  var gameServerTypes = new HashMap[GameServerTypeId,GameServerSpecification]()
  var gameServerSetups = new HashMap[(GameServerTypeId,GameProcessSettingsId),GameProcessTemplate]()


  def findBy(ss:GameProcessSettingsId) : Option[GameProcessTemplate] = {
    gameServerSetups.find( _._1._2 == ss).map(_._2)
  }
  def findBy(ss:GameAndRulesId) : Option[GameProcessTemplate] = {
    gameServerSetups.get(ss)
  }

  def add(id:GameServerTypeId,spec: GameServerSpecification) = {
    gameServerTypes = gameServerTypes + (id -> spec)
  }

  def addProcessSettings(id:(GameServerTypeId, GameProcessSettingsId),spec: GameProcessTemplate) = {
    gameServerSetups = gameServerSetups + (id -> spec)
  }



    //Map('TANK_GAME -> Map[GameServerSettingsId, GameProcessTemplate]( 'TankGame2P-> tankGameSettings2, 'TankGame4P -> tankGameSettings4))




  /*" --tcp-port 54555 --udp-port 54777 --master-host localhost --master-port 1199 ", "http://" + LobbyServer.remoteIp + ":8080/start_game.jnlp",
  Map("gamePortUDP" -> "54777", "gamePortTCP" -> "54555", "gameHost" -> LobbyServer.remoteIp)
  */



}


object GameServerPool {



  def clock = System.currentTimeMillis()
  /**
   * Use sbt:
   * > console-project server
   * > evalTask( fullClasspath in Compile, currentState ).files foreach println
   *
   */


  val pool = new GameServerPool




  def main(args:Array[String]) {
    val pool: GameServerPool = new GameServerPool
    //pool.spawnServer(tankGameSettings2)
    //pool.spawnServer(tankGameSettings4)
  }
}

class ResourceAllocator {
  val tcpRange = new AllocatablePortRange(Range(Cs3kConfig.TCP_RANGE_START,Cs3kConfig.TCP_RANGE_END), "TCP Range")
  val udpRange = new AllocatablePortRange(Range(Cs3kConfig.UDP_RANGE_START,Cs3kConfig.UDP_RANGE_END), "UDP Range")

  def allocate(rn:ResourceNeeds) = {
    val reservedUdp = (1 until rn.numOfUdpPorts).map( b => udpRange.reservePort() )
    val reservedTcp= (1 until rn.numOfTcpPorts).map( b => tcpRange.reservePort() )


    (reservedTcp.flatten,reservedUdp.flatten) match {
      case (tcps, udps) if (tcps.size, udps.size) == (rn.numOfTcpPorts, rn.numOfUdpPorts) => Some(new ResourceSet(tcps.toSet, udps.toSet))
      case (tcps, udps) =>
        unallocate(tcps, tcpRange)
        unallocate(udps, udpRange)
        None
    }
  }

  def unallocate(ports:IndexedSeq[Int], range:AllocatablePortRange) {
    ports.foreach(range.unAllocate(_))
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
    if (allocated.contains(p))
      allocated = allocated - p
    else
      log.warn("Unallocating " + p + " from " + id)
  }
}


class GameServerPool {

  val resourceAllocator = new ResourceAllocator()

  val log = Logger.getLogger(classOf[GameServerPool])

  var servers = collection.mutable.Map.empty[RunningGame, DefaultExecutor]


  def findRunningGame(occassionId:OccassionId) : Option[RunningGame] = {
    log.debug("findRunningGame " + occassionId + " " + servers.size)
    servers.foreach { case (rg,e) => log.debug("rg.game.occassionId " + rg.game.occassionId) }
    servers.find { case (rg,e) => rg.game.occassionId == occassionId}.map(_._1)
  }

  def spawnServer(gpsTemplate:GameProcessTemplate, game:AbstractGameOccassion) = {
    log.info("Starting game " + gpsTemplate + " " + game.occassionId)


    val resourceAllocations = resourceAllocator.allocate(gpsTemplate.gameSpecification.resourceNeeds).getOrElse(
      throw new RuntimeException("Not enought resources available")
    )
    val gps = gpsTemplate.specifyInstance(resourceAllocations)
    val resultHandler = new DefaultExecuteResultHandler {
      override def onProcessComplete(exitValue: Int) {
        super.onProcessComplete(exitValue)
        removeServerFromPoolIfExist(game)
      }

      override def onProcessFailed(e: ExecuteException) {
        super.onProcessFailed(e)
        removeServerFromPoolIfExist(game)
      }

    }

    val watchdog  = new ExecuteWatchdog(10 * 60 * 1000)

    val executor  = new DefaultExecutor

    val processDestroyer = new ShutdownHookProcessDestroyer()

    executor.setExitValue(1)

    executor.setWatchdog(watchdog)

    executor.setProcessDestroyer(processDestroyer)

    var time: Long = (GameServerPool.clock / 1000) % (10*365*24*60*60)
    var logName: String = "logs/srv_" + time + "_" + game.occassionId + ".log"
    var cmdLine: CommandLine = gps.cmdLine(" --occassion-id " + game.occassionId + " --log " + logName)

    log.info("Begin server start, cmd line : " + cmdLine)
    executor.execute(cmdLine, resultHandler)
    log.info("End server start")


    var running: RunningGame = new RunningGame(game, gps)
    servers = servers + (running -> executor)

    // some time later the result handler callback was invoked so we
    // can safely request the exit value

    running
  }


  private def removeServerFromPoolIfExist(gps: AbstractGameOccassion) {
    //servers.remove(gps)

    findRunningGame(gps.occassionId) foreach { rg =>
      removeServerFromPool(gps, rg)
    }

  }


  def removeServerFromPool(gps: AbstractGameOccassion, rg: RunningGame): Option[DefaultExecutor] = {
    log.info("REMOVING SERVER FROM POOL: " + gps.occassionId)
    servers.remove(rg)
  }

  def destroyServer(rg:RunningGame) = {
    servers.get(rg).foreach { case executor =>
      executor.getWatchdog.destroyProcess()
    }
  }

}
