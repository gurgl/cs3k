package se.bupp.cs3k.server

import model.{AbstractGameOccassion, RunningGame}
import org.apache.commons.exec._
import se.bupp.cs3k.server.GameServerPool.GameProcessSettings
import java.net.URL
import se.bupp.cs3k.server.service.GameReservationService._
import org.apache.log4j.Logger
import se.bupp.cs3k.server.model.Model.UserId

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
  val cmdStr = "java " +
    "-jar " +
    "C:/dev/workspace/opengl-tanks/server/target/scala-2.9.2/server_2.9.2-0.1-one-jar.jar" +
    //"C:/dev/workspace/opengl-tanks/target/scala-2.9.2/classes;C:/dev/workspace/opengl-tanks/lib/kryonet-2.18-all.jar;C:/Users/karlw/.sbt/boot/scala-2.9.1/lib/scala-library.jar;C:/Users/karlw/.ivy2/cache/org.scalaz/scalaz-core_2.9.1/jars/scalaz-core_2.9.1-6.0.4.jar;C:/Users/karlw/.ivy2/cache/org.objenesis/objenesis/jars/objenesis-1.2.jar;C:/Users/karlw/.ivy2/cache/log4j/log4j/bundles/log4j-1.2.17.jar;C:/Users/karlw/.ivy2/cache/com.jme3/eventbus/jars/eventbus-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jbullet/jars/jbullet-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jinput/jars/jinput-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-blender/jars/jME3-blender-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-core/jars/jME3-core-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-desktop/jars/jME3-desktop-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-effects/jars/jME3-effects-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-jbullet/jars/jME3-jbullet-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-jogg/jars/jME3-jogg-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-lwjgl/jars/jME3-lwjgl-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-lwjgl-natives/jars/jME3-lwjgl-natives-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-networking/jars/jME3-networking-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-niftygui/jars/jME3-niftygui-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-plugins/jars/jME3-plugins-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-terrain/jars/jME3-terrain-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-testdata/jars/jME3-testdata-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/j-ogg-oggd/jars/j-ogg-oggd-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/j-ogg-vorbisd/jars/j-ogg-vorbisd-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/lwjgl/jars/lwjgl-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty/jars/nifty-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-default-controls/jars/nifty-default-controls-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-examples/jars/nifty-examples-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-style-black/jars/nifty-style-black-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/stack-alloc/jars/stack-alloc-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/vecmath/jars/vecmath-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/xmlpull-xpp3/jars/xmlpull-xpp3-3.0.0.20120512-SNAPSHOT.jar " +
    //"se.bupp.lek.server.Server"
    " "


  val tankGameSettings2  = new GameProcessSettings(
    cmdStr + " --tcp-port 54555 --udp-port 54777 --master-host localhost --master-port 1199 ", "http://" + LobbyServer.remoteIp + ":8080/start_game.jnlp",
    Map("gamePortUDP" -> "54777", "gamePortTCP" -> "54555", "gameHost" -> LobbyServer.remoteIp))
  val tankGameSettings4  = new GameProcessSettings(cmdStr + " 53556 53778", "http://" + LobbyServer.remoteIp + ":8080/start_game.jnlp", Map() )

  class GameProcessSettings(var commandLine:String, var clientJNLPUrl:String, val props:Map[String,String]) {
    def cmdLine(extra:String) = CommandLine.parse(commandLine + extra);


    def jnlpUrl(reservationId:SeatId,name:String) : URL = new URL(clientJNLPUrl + "?reservation_id=" + reservationId+"&player_name=" + name)
    def jnlpUrl(reservationId:SeatId,name:UserId) : URL = new URL(clientJNLPUrl + "?reservation_id=" + reservationId+"&user_id=" + name)
  }

  val pool = new GameServerPool




  def main(args:Array[String]) {
    val pool: GameServerPool = new GameServerPool
    //pool.spawnServer(tankGameSettings2)
    //pool.spawnServer(tankGameSettings4)
  }
}



class GameServerPool {

  val log = Logger.getLogger(classOf[GameServerPool])

  var servers = collection.mutable.Map.empty[RunningGame, DefaultExecutor]


  def findRunningGame(occassionId:OccassionId) : Option[RunningGame] = {
    log.debug("findRunningGame " + occassionId + " " + servers.size)
    servers.foreach { case (rg,e) => log.debug("rg.game.occassionId " + rg.game.occassionId) }
    servers.find { case (rg,e) => rg.game.occassionId == occassionId}.map(_._1)
  }

  def spawnServer(gps:GameProcessSettings, game:AbstractGameOccassion) = {
    log.info("Starting game " + gps + " " + game.occassionId)

    val resultHandler = new DefaultExecuteResultHandler {
      override def onProcessComplete(exitValue: Int) {
        super.onProcessComplete(exitValue)
        removeServerFromPool(game)
      }

      override def onProcessFailed(e: ExecuteException) {
        super.onProcessFailed(e)
        removeServerFromPool(game)
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


  private def removeServerFromPool(gps: AbstractGameOccassion) {
    //servers.remove(gps)
    log.info("SHUTTING DOWN SERVER : " + gps.occassionId)
    findRunningGame(gps.occassionId) foreach( rg => servers.remove(rg))
  }

  def destroyServer = {

  }

}
