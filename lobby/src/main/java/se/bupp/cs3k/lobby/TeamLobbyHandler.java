package se.bupp.cs3k.lobby;

import com.esotericsoftware.kryonet.Connection;
import se.bupp.cs3k.LobbyJoinResponse;
import se.bupp.cs3k.ProgressUpdated;
import se.bupp.cs3k.StartGame;
import se.bupp.cs3k.lobby.ui.NonTeamLobbyPanel;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-03
 * Time: 21:04
 * To change this template use File | Settings | File Templates.
 */
public class TeamLobbyHandler extends AbstractLobbyServerHandler {

    Integer gameSize = null;

    public TeamLobbyHandler(LobbyClient lobbyClient) {
        super(lobbyClient);
    }


    @Override
    public JComponent getUIPanel() {
        return  teamLobbyPanel.getPanel1();
    }

    @Override
    public void setCommunication(Communication communication) {
        clt = communication;
    }

    final public NonTeamLobbyPanel teamLobbyPanel = new NonTeamLobbyPanel();

    @Override
    public void onStartGame(final StartGame upd) {

        try {
            gameJnlpUrl = new URL(upd.getJnlpURL());
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(lobbyClient, "Bad url" + upd.getJnlpURL());
        }
        System.err.println(upd.getJnlpURL());


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                teamLobbyPanel.getProgressBar1().setString("Game Started");
                teamLobbyPanel.getProgressBar1().setValue(0);
                lobbyClient.startGame(gameJnlpUrl);
            }
        });
    }

    @Override
    public void onServerNotification(final ProgressUpdated upd) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                teamLobbyPanel.getProgressBar1().setString("" + upd.getProgress() + " of " + gameSize + " connected.");


                teamLobbyPanel.getProgressBar1().setValue(upd.getProgress());
            }
        });

    }

    @Override
    public void onLobbyJoinResponse(LobbyJoinResponse connectMessage) {
        gameSize = connectMessage.getParticipantsRequired();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                teamLobbyPanel.getProgressBar1().setMaximum(gameSize);
            }
        });
    }

    @Override
    public void onDisconnected(Connection connection) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                teamLobbyPanel.getProgressBar1().setString("Disconnected");

            }
        });
    }
}

