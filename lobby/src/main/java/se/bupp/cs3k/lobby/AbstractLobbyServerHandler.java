package se.bupp.cs3k.lobby;

import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-03
 * Time: 21:07
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractLobbyServerHandler implements LobbyServerHandler {

    URL gameJnlpUrl = null;
    LobbyClient lobbyClient;
    public Communication clt;


    protected AbstractLobbyServerHandler(LobbyClient lobbyClient) {
        this.lobbyClient = lobbyClient;
    }
}
