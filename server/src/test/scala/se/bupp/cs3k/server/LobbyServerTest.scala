package se.bupp.cs3k.server

import model.GameOccassion
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import se.bupp.cs3k.LobbyJoinRequest

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-04
 * Time: 23:47
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class LobbyServerTest extends Specification with Mockito {


  "lobby server" should {
    "" in {
      /*
      val lobby = new LalLobbyHandler(2,null)
      lobby.playerJoined(new LobbyJoinRequest(1,"tja"))
      lobby.playerJoined(new LobbyJoinRequest(1,"tja"))
         */
      1 === 1

    }

  }
}
