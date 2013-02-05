package se.bupp.cs3k.server

import service.gameserver.{GameServerRepository, GameServerSpecification}
import service.resourceallocation.ResourceNeeds
import se.bupp.cs3k.server.ServerAllocator.Init

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-19
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
class Init {



  val tankGameServer = new GameServerSpecification("java " +
    "-jar " +
    "C:/dev/workspace/opengl-tanks/server/target/scala-2.9.2/server_2.9.2-0.1-one-jar.jar" +
    //"C:/dev/workspace/opengl-tanks/target/scala-2.9.2/classes;C:/dev/workspace/opengl-tanks/lib/kryonet-2.18-all.jar;C:/Users/karlw/.sbt/boot/scala-2.9.1/lib/scala-library.jar;C:/Users/karlw/.ivy2/cache/org.scalaz/scalaz-core_2.9.1/jars/scalaz-core_2.9.1-6.0.4.jar;C:/Users/karlw/.ivy2/cache/org.objenesis/objenesis/jars/objenesis-1.2.jar;C:/Users/karlw/.ivy2/cache/log4j/log4j/bundles/log4j-1.2.17.jar;C:/Users/karlw/.ivy2/cache/com.jme3/eventbus/jars/eventbus-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jbullet/jars/jbullet-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jinput/jars/jinput-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-blender/jars/jME3-blender-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-core/jars/jME3-core-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-desktop/jars/jME3-desktop-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-effects/jars/jME3-effects-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-jbullet/jars/jME3-jbullet-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-jogg/jars/jME3-jogg-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-lwjgl/jars/jME3-lwjgl-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-lwjgl-natives/jars/jME3-lwjgl-natives-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-networking/jars/jME3-networking-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-niftygui/jars/jME3-niftygui-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-plugins/jars/jME3-plugins-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-terrain/jars/jME3-terrain-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-testdata/jars/jME3-testdata-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/j-ogg-oggd/jars/j-ogg-oggd-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/j-ogg-vorbisd/jars/j-ogg-vorbisd-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/lwjgl/jars/lwjgl-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty/jars/nifty-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-default-controls/jars/nifty-default-controls-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-examples/jars/nifty-examples-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-style-black/jars/nifty-style-black-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/stack-alloc/jars/stack-alloc-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/vecmath/jars/vecmath-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/xmlpull-xpp3/jars/xmlpull-xpp3-3.0.0.20120512-SNAPSHOT.jar " +
    //"se.bupp.lek.server.Server"
    " ", new ResourceNeeds(1,1))

  //Map("gamePortUDP" -> "54777", "gamePortTCP" -> "54555", "gameHost" -> LobbyServer.remoteIp)
  val tankGameSettings2  = tankGameServer.create(
    " --tcp-port ${tcp[0]} --udp-port ${udp[0]} --master-host ${cs3k_host} --master-port ${cs3k_port} ", "start_game.jnlp",
    Map("gamePortUDP" -> "${udp[0]}", "gamePortTCP" -> "${tcp[0]}", "gameHost" -> Cs3kConfig.REMOTE_IP)
  )

  val tankGameSettings4  = tankGameServer.create(" 53556 53778", "http://" + Cs3kConfig.REMOTE_IP + ":8080/start_game.jnlp", Map() )



  GameServerRepository.add('TankGame, tankGameServer)
  GameServerRepository.addProcessTemplate(('TankGame, 'TG2Player), tankGameSettings2)
  GameServerRepository.addProcessTemplate(('TankGame, 'TG4Player), tankGameSettings4)


}
