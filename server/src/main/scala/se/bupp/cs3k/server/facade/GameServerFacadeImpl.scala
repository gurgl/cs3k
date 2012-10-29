package se.bupp.cs3k.server.facade

import se.bupp.cs3k.api._
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import java.rmi.server.UnicastRemoteObject
import java.lang.Integer
import java.lang.{Integer => JInt}
import org.apache.log4j.Logger
import java.rmi.RemoteException


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 21:30
 * To change this template use File | Settings | File Templates.
 */
@Service("gameServerFacade")
class GameServerFacadeImpl() extends GameServerFacadeRemote with GameServerFacade {

  val log = Logger.getLogger(classOf[GameServerFacadeImpl])

  val om = new ObjectMapper()

  @throws(classOf[java.rmi.RemoteException])
  def evaluateGamePass(pass: String) : AbstractPlayerInfo = {
    try {
      log.info("evaluateGamePass invoked")
      var absPI = om.readValue(pass, classOf[AbstractGamePass])
      log.info("evaluateGamePass invoked" + absPI)
      absPI match {
        case t:Ticket => new RegisteredPlayerInfo()
        case t:AnonymousPass => new AnonymousPlayerInfo(new String(t.getName.getBytes()))
      }
    } catch {
      case e:Exception => e.printStackTrace()
      throw new RemoteException("Tjenare")
    }

  }

  def startGame(occassionId: JInt, teamsByBlayers: java.util.Map[JInt, JInt]) {
    log.info("StartGame invoked")
  }

  def startGame(occassionId: JInt, players: java.util.List[JInt]) {

  }

  def endGame(occassionId: JInt, serializedScore: String) {
    log.info("EndGame invoked")

  }
}
