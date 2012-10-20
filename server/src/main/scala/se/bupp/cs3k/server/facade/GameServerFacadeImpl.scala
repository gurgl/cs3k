package se.bupp.cs3k.server.facade

import se.bupp.cs3k.api._
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import java.rmi.server.UnicastRemoteObject

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 21:30
 * To change this template use File | Settings | File Templates.
 */
@Service("gameServerFacade")
class GameServerFacadeImpl() extends GameServerFacadeRemote with GameServerFacade {

  val om = new ObjectMapper()

  @throws(classOf[java.rmi.RemoteException])
  def evaluateGamePass(pass: String) = {
    var absPI = om.readValue(pass, classOf[AbstractGamePass])
    absPI match {
      case t:Ticket => new RegisteredPlayerInfo()
      case t:AnonymousPass => new AnonymousPlayerInfo(t.getName)
    }

  }
}
