package se.bupp.cs3k.server.service.gameserver

import org.apache.commons.exec._

import java.net.URL
import se.bupp.cs3k.server.service.GameReservationService._
import org.apache.log4j.Logger

import scala.collection.immutable.HashMap
import collection.{mutable, SortedSet}
import se.bupp.cs3k.server.model.{AbstractGameOccassion, RunningGame}
import se.bupp.cs3k.server.service.resourceallocation.ResourceAllocator

import se.bupp.cs3k.server.model.Model._
import concurrent.{Future,Promise, future, promise }
import se.bupp.cs3k.server.Cs3kConstants

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-15
 * Time: 17:17
 * To change this template use File | Settings | File Templates.
 */


object GameServerPool {

  def clock = System.currentTimeMillis()
  /**
   * Use sbt:
   * > console-project server
   * > evalTask( fullClasspath in Compile, currentState ).files foreach println
   *
   */
  var pool = new GameServerPool

  def main(args:Array[String]) {
    val pool: GameServerPool = new GameServerPool
    //pool.spawnServer(tankGameSettings2)
    //pool.spawnServer(tankGameSettings4)
  }
}



class GameServerPool {

  import GameServerPool._

  val resourceAllocator = new ResourceAllocator()

  val log = Logger.getLogger(classOf[GameServerPool])

  var servers = collection.mutable.Map.empty[RunningGame, DefaultExecutor]


  def findRunningGame(gameSessionId:GameSessionId) : Option[RunningGame] = {
    log.debug("findRunningGame " + gameSessionId + " " + servers.size)
    servers.foreach { case (rg,e) => log.debug("rg.game.gameSessionId " + rg.game.gameSessionId) }
    servers.find { case (rg,e) => rg.game.gameSessionId == gameSessionId}.map(_._1)
  }

  def spawnServer(gpsTemplate:GameProcessTemplate, game:AbstractGameOccassion, processToken:ProcessToken) : RunningGame = {
    log.info("Starting game " + gpsTemplate + " " + game.gameSessionId)


    var time: Long = (GameServerPool.clock / 1000) % (10*365*24*60*60)
    var logName: String = "logs/srv_" + time + "_" + game.gameSessionId + ".log"


    val done = promise[ProcessToken]

    val resourceAllocations = resourceAllocator.allocate(gpsTemplate.gameSpecification.resourceNeeds).getOrElse(
      throw new RuntimeException("Not enought resources available")
    )
    val gameProcessSettings = gpsTemplate.specifyInstance(resourceAllocations, " " + Cs3kConstants.GAME_SERVER_CMDLINE_GAME_SESSION_ID_PARAM_NAME + " " + game.gameSessionId + " --log " + logName)

    val resultHandler = new DefaultExecuteResultHandler {


      override def onProcessComplete(exitValue: Int) {
        super.onProcessComplete(exitValue)
        resourceAllocator.unallocate(gameProcessSettings.resourceSet)
        removeServerFromPoolIfExist(game)
        done success processToken
      }

      override def onProcessFailed(e: ExecuteException) {
        super.onProcessFailed(e)
        resourceAllocator.unallocate(gameProcessSettings.resourceSet)
        removeServerFromPoolIfExist(game)
        done success processToken
      }

    }

    val watchdog  = new ExecuteWatchdog(10 * 60 * 1000)

    val executor  = new DefaultExecutor

    val processDestroyer = new ShutdownHookProcessDestroyer()

    executor.setExitValue(1)

    executor.setWatchdog(watchdog)

    executor.setProcessDestroyer(processDestroyer)


    log.info("Begin server start, cmd line : " + gameProcessSettings.commandLine)
    executor.execute(gameProcessSettings.commandLine, resultHandler)
    log.info("End server start")


    var running: RunningGame = new RunningGame(game, gameProcessSettings, done.future)
    servers = servers + (running -> executor)

    // some time later the result handler callback was invoked so we
    // can safely request the exit value

    running
  }


  private def removeServerFromPoolIfExist(gps: AbstractGameOccassion) {
    //servers.remove(gps)

    findRunningGame(gps.gameSessionId) foreach { rg =>
      removeServerFromPool(gps, rg)
    }

  }


  def removeServerFromPool(gps: AbstractGameOccassion, rg: RunningGame): Option[DefaultExecutor] = {
    log.info("REMOVING SERVER FROM POOL: " + gps.gameSessionId)
    servers.remove(rg)
  }

  def destroyServer(rg:RunningGame) = {
    servers.get(rg).foreach { case executor =>
      executor.getWatchdog.destroyProcess()
    }
  }

}
