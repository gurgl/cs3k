package se.bupp.cs3k.server

import java.net.URL
import io.Source
import org.apache.log4j.Logger
import se.bupp.cs3k.server.model.Model._

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-20
 * Time: 19:33
 * To change this template use File | Settings | File Templates.
 */

object Cs3kConstants {
  val GAME_SERVER_CMDLINE_GAME_SESSION_ID_PARAM_NAME = "--game-session-id"
  var LAUNCHER_PATH = "deploy-launcher/"
}
object Cs3kConfig {



  val log = Logger.getLogger(this.getClass)

  val TCP_RANGE_START = 54555
  val TCP_RANGE_END = 54565

  val UDP_RANGE_START = 54777
  val UDP_RANGE_END = 54787

  val CS3K_HOST = "localhost"
  val CS3K_PORT = 1199

  val LOBBY_SERVER_PORT_RANGE = Range(51545,51554)

  var TEMP_FIX_FOR_STORING_GAME_TYPE: GameAndRulesId = ('TankGame, 'TG2Player)
  var LOBBY_GAME_LAUNCH_ANNOUNCEMENT_DELAY = 2


  val MAX_NUM_OF_GAME_SERVER_PROCESSES = 10

  //lazy val REMOTE_IP = "localhost"
  lazy val REMOTE_IP = {
    val stackOverflowURL = "http://www.biranchi.com/ip.php"
    val requestProperties = Map(
      "User-Agent" -> "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:12.0) Gecko/20100101 Firefox/12.0"
    )

    val connection = new URL(stackOverflowURL).openConnection
    requestProperties.foreach({
      case (name, value) => connection.setRequestProperty(name, value)
    })

    var response = Source.fromInputStream(connection.getInputStream).getLines.mkString("\n").substring(3)
    log.info("*" + response + "*")

    response
  }
}
