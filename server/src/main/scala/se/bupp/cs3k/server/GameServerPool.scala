package se.bupp.cs3k.server

import org.apache.commons.exec._
import se.bupp.cs3k.server.GameServerPool.GameProcessSettings

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-15
 * Time: 17:17
 * To change this template use File | Settings | File Templates.
 */

object GameServerPool {

  val cmdStr = "java " +
    "-classpath " +
    "C:/dev/workspace/opengl-tanks/target/scala-2.9.1/classes;C:/dev/workspace/opengl-tanks/lib/jME3-bullet-natives.jar;C:/dev/workspace/opengl-tanks/lib/jME3-bullet.jar;C:/dev/workspace/opengl-tanks/lib/kryonet-2.12-all.jar;C:/Users/karlw/.sbt/boot/scala-2.9.1/lib/scala-library.jar;C:/Users/karlw/.ivy2/cache/org.lwjgl.lwjgl/lwjgl/jars/lwjgl-2.8.3.jar;C:/Users/karlw/.ivy2/cache/org.lwjgl.lwjgl/lwjgl-platform/jars/lwjgl-platform-2.8.3-natives-osx.jar;C:/Users/karlw/.ivy2/cache/net.java.jinput/jinput/jars/jinput-2.0.5.jar;C:/Users/karlw/.ivy2/cache/net.java.jutils/jutils/jars/jutils-1.0.0.jar;C:/Users/karlw/.ivy2/cache/net.java.jinput/jinput-platform/jars/jinput-platform-2.0.5-natives-osx.jar;C:/Users/karlw/.ivy2/cache/org.lwjgl.lwjgl/lwjgl_util/jars/lwjgl_util-2.8.3.jar;C:/Users/karlw/.ivy2/cache/org.scalaz/scalaz-core_2.9.1/jars/scalaz-core_2.9.1-6.0.4.jar;C:/Users/karlw/.ivy2/cache/org.objenesis/objenesis/jars/objenesis-1.2.jar;C:/Users/karlw/.ivy2/cache/com.jme3/eventbus/jars/eventbus-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jinput/jars/jinput-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-blender/jars/jME3-blender-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-core/jars/jME3-core-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-desktop/jars/jME3-desktop-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-effects/jars/jME3-effects-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-jogg/jars/jME3-jogg-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-lwjgl/jars/jME3-lwjgl-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-lwjgl-natives/jars/jME3-lwjgl-natives-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-networking/jars/jME3-networking-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-niftygui/jars/jME3-niftygui-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-plugins/jars/jME3-plugins-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-terrain/jars/jME3-terrain-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-testdata/jars/jME3-testdata-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/j-ogg-oggd/jars/j-ogg-oggd-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/j-ogg-vorbisd/jars/j-ogg-vorbisd-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/lwjgl/jars/lwjgl-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty/jars/nifty-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-default-controls/jars/nifty-default-controls-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-examples/jars/nifty-examples-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-style-black/jars/nifty-style-black-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/stack-alloc/jars/stack-alloc-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/vecmath/jars/vecmath-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/xmlpull-xpp3/jars/xmlpull-xpp3-3.0.0.20120512-SNAPSHOT.jar;C:/dev/workspace/opengl-tanks/src/main/blender/ " +
    "se.bupp.lek.server.Server"

  val tankGameSettings2  = new GameProcessSettings(cmdStr + " 54555 54777", "http://localhost:8080/game_deploy_dir_tmp/tanks/Game.jnlp" )
  val tankGameSettings4  = new GameProcessSettings(cmdStr + " 53556 53778", "http://localhost:8080/game_deploy_dir_tmp/tanks/Game.jnlp" )

  class GameProcessSettings(var commandLine:String, var clientJNLPUrl:String) {
    val cmdLine = CommandLine.parse(commandLine);
  }

  val pool = new GameServerPool


  def main(args:Array[String]) {
    val pool: GameServerPool = new GameServerPool
    pool.spawnServer(tankGameSettings2)
    pool.spawnServer(tankGameSettings4)
  }
}



class GameServerPool {


  var servers = collection.mutable.Map.empty[GameProcessSettings, DefaultExecutor]

  def spawnServer(gps:GameProcessSettings) = {

    val resultHandler = new DefaultExecuteResultHandler {
      override def onProcessComplete(exitValue: Int) {
        super.onProcessComplete(exitValue)
        removeServerFromPool(gps)
      }

      override def onProcessFailed(e: ExecuteException) {
        super.onProcessFailed(e)
        removeServerFromPool(gps)
      }
    }

    val watchdog  = new ExecuteWatchdog(60 * 1000)

    val executor  = new DefaultExecutor

    executor.setExitValue(1)

    executor.setWatchdog(watchdog)

    executor.execute(gps.cmdLine, resultHandler)

    servers = servers + (gps -> executor)

    // some time later the result handler callback was invoked so we
    // can safely request the exit value

  }


  private def removeServerFromPool(gps: GameServerPool.GameProcessSettings) {
    servers.remove(gps)
  }

  def destroyServer = {

  }

}
