package se.bupp.cs3k

import org.specs2.mutable.Specification
import server.model.{RunningGame, NonPersisentGameOccassion}
import server.service.gameserver.{GameServerPool, GameServerRepository, GameProcessTemplate}
import server.{Init}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-18
 * Time: 23:53
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class ServerPoolTest  extends Specification {

  "server pool" should {
    "should handle instances beeing killed" in {

      new Init()

      var setup: GameProcessTemplate = GameServerRepository.gameServerSetups(('TankGame, 'TG2Player))

      var pool: GameServerPool = new GameServerPool()
      var server1: RunningGame = pool.spawnServer(setup, new NonPersisentGameOccassion(123L))
      val executor1 = pool.servers(server1)

      executor1.getWatchdog.killedProcess() === false
      try {
        Thread.sleep(2000)
      } catch {
        case e:InterruptedException =>
      }

      var server2: RunningGame = pool.spawnServer(setup, new NonPersisentGameOccassion(234L))
      val executor2 = pool.servers(server2)
      pool.servers.size === 2

      pool.destroyServer(server1)
      try {
        Thread.sleep(2000)
      } catch {
        case e:InterruptedException =>
      }

      pool.servers.size === 1
      executor1.getWatchdog.killedProcess() === true
      executor2.getWatchdog.killedProcess() === false
      pool.destroyServer(server2)
      try {
        Thread.sleep(2000)
      } catch {
        case e:InterruptedException =>
      }
      pool.servers.size === 0
      executor2.getWatchdog.killedProcess() === true
     }
  }
}