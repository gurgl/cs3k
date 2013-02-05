package se.bupp.cs3k.lobby;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import se.bupp.cs3k.LobbyJoinResponse;
import se.bupp.cs3k.ProgressUpdated;
import se.bupp.cs3k.StartGame;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-03
 * Time: 20:51
 * To change this template use File | Settings | File Templates.
 */
public interface LobbyServerHandler {

    void setCommunication(Communication communication);

    void onStartGame(StartGame upd);

    void onServerNotification(ProgressUpdated upd);

    void onLobbyJoinResponse(LobbyJoinResponse connectMessage);

    void onDisconnected(Connection connection);

    public JComponent getUIPanel();
}
