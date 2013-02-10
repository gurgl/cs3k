package se.bupp.cs3k.server.facade.lobby

import com.esotericsoftware.kryonet.{KryoSerialization, Listener, Connection, Server}
import se.bupp.cs3k._
import api.user.{RegisteredPlayerIdentifierWithInfo, RegisteredPlayerIdentifier, AnonymousPlayerIdentifier, AbstractPlayerIdentifier}

import collection.mutable
import java.util.{Timer, HashMap}
import org.apache.commons.exec.{DefaultExecutor, ExecuteWatchdog, DefaultExecuteResultHandler, CommandLine}
import java.util.concurrent.{TimeUnit, Executors}
import java.net.URL
import io.Source
import server.Cs3kConfig
import server.service.gameserver.{GameServerPool, GameServerRepository}


import com.esotericsoftware.minlog.Log
import com.esotericsoftware.kryo.serializers.{BeanSerializer, TaggedFieldSerializer, JavaSerializer}
import com.esotericsoftware.kryo.Kryo
import se.bupp.cs3k.server.model.Model._
import scala.Some
import concurrent.{Promise, Future, future, promise}
import collection.immutable.Queue
import scala.util.{Failure, Success}
import org.slf4j.{LoggerFactory, Logger}


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-07-13
 * Time: 22:44
 * To change this template use File | Settings | File Templates.
 */

object LobbyServer {



  var seqId = Cs3kConfig.LOBBY_SERVER_PORT_RANGE.head

  def createContinousForNonPersistedGameOcassionsInstance(numOfPlayers:Int, gameAndRulesId: GameServerRepository.GameAndRulesId) = {
    if (seqId + 1 >= Cs3kConfig.LOBBY_SERVER_PORT_RANGE.last) throw new RuntimeException("Range ended")
    val lobbyServer = new LobbyServer(seqId, new NonTeamLobbyQueueHandler(numOfPlayers, gameAndRulesId))
    seqId = seqId + 1
    lobbyServer
  }

}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-06
 * Time: 03:38
 * To change this template use File | Settings | File Templates.
 */
class LobbyServer(val portId:Int, var lobbyHandler:NonTeamLobbyQueueHandler) {



  val log = LoggerFactory.getLogger(classOf[LobbyServer])



  val kryo: Kryo = new Kryo
  kryo.setDefaultSerializer(classOf[BeanSerializer[_]])
  import scala.collection.JavaConversions.asScalaBuffer
  LobbyProtocol.getTypes.toList.foreach( c => kryo.register(c) )


  val kryoSerialization: KryoSerialization = new KryoSerialization(kryo)

  val server = new Server(16384, 2048, kryoSerialization)

  def stop() {
    server.stop();
    server.close();

    GameServerPool.pool.servers.foreach( s => s._2.getWatchdog.destroyProcess())
  }

  def start = {

    server.start()

    server.bind(portId);

    server.addListener(new Listener() {

      override def disconnected(p1: Connection) {
        super.disconnected(p1)
        if (lobbyHandler.removeConnection(p1)) {
          server.sendToAllTCP(new ProgressUpdated(lobbyHandler.queue.size))
        }
      }

      override def received (connection:Connection , ob:Object) {
        ob match {
          case request:LobbyJoinRequest =>
            log.info("LobbyJoinRequest received : " + request)
            connection.sendTCP(new LobbyJoinResponse(lobbyHandler.numOfPlayers))

            lobbyHandler.playerJoined(request, connection)

            //server.sendToAllTCP(new ProgressUpdated(lobbyHandler.queue.size))

          case e => log.info("uknown rec" + e)
        }
      }
    })
  }
}














