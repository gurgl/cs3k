package se.bupp.cs3k.server

import service.gameserver.{GameServerRepository, GameServerSpecification}
import service.resourceallocation.{ServerAllocator, ResourceNeeds}
import ServerAllocator.Init

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
    " ", new ResourceNeeds(1,1))

  val tankGameSettings2  = tankGameServer.create(
    " --tcp-port ${tcp[0]} --udp-port ${udp[0]} --master-host ${cs3k_host} --master-port ${cs3k_port} ", "start_game.jnlp",
    Map("gamePortUDP" -> "${udp[0]}", "gamePortTCP" -> "${tcp[0]}", "gameHost" -> Cs3kConfig.REMOTE_IP)
  )

  val tankGameSettings4  = tankGameServer.create(
    " --tcp-port ${tcp[0]} --udp-port ${udp[0]} --master-host ${cs3k_host} --master-port ${cs3k_port} ", "start_game.jnlp",
    Map("gamePortUDP" -> "${udp[0]}", "gamePortTCP" -> "${tcp[0]}", "gameHost" -> Cs3kConfig.REMOTE_IP)
  )


  GameServerRepository.add('TankGame, tankGameServer)
  GameServerRepository.addProcessTemplate(('TankGame, 'TG2Player), tankGameSettings2)
  GameServerRepository.addProcessTemplate(('TankGame, 'TG4Player), tankGameSettings4)


}
