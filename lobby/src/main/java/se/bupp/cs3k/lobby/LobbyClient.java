package se.bupp.cs3k.lobby;


import com.esotericsoftware.kryonet.*;
import se.bupp.cs3k.*;
import se.bupp.cs3k.lobby.ui.BaseUI;

import java.awt.*;
import javax.swing.*;
import javax.jnlp.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;


public class LobbyClient extends JFrame {
    static BasicService basicService = null;



    URL gameJnlpUrl = null;



    public LobbyClient(String s) {
        super("Lobby Client");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {

            init();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "exception happens" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void init() {
        Container content = getContentPane();


        String lobbyPortStr = System.getProperty("javaws.lobbyPort");
        String playerName = System.getProperty("javaws.playerName");
        String userIdStr = System.getProperty("javaws.userId");
        String givenMode = System.getProperty("javaws.mode");




        setTitle("Tank Showdown lobby" + playerName);

        int lobbyPort = 12345;
        if(lobbyPortStr != null) {
            lobbyPort = Integer.parseInt(lobbyPortStr);
        }
        String lobbyHost = "";
        try {
            basicService = (BasicService)
                    ServiceManager.lookup("javax.jnlp.BasicService");
            URL codeBase = basicService.getCodeBase();
            lobbyHost = codeBase.getHost();

        } catch (UnavailableServiceException e) {
            System.err.println("Lookup failed: " + e);
        }

        LobbyServerHandler ls =  null;



        if(givenMode != null && givenMode.equals("team")) {
            ls = new NonTeamLobbyHandler(this);
        }   else {
            ls = new NonTeamLobbyHandler(this);
        }
        JComponent handlerUi = ls.getUIPanel();
        final Communication com = new Communication(lobbyPort, lobbyHost,ls);
        com.init();

        String message = "Not connected";



        final BaseUI formDelegate = new BaseUI(handlerUi);
        //formDelegate.getContainer()


        final JComponent form = formDelegate.$$$getRootComponent$$$();
        content.add(form);
        //teamLobbyPanel.getProgressBar().setStringPainted(true);

        //teamLobbyPanel.getProgressBar().setString(message);
        System.err.println("bef listener");

        com.client.addListener(new Listener() {

            public void disconnected (Connection connection) {

            }

            public void received(Connection connection, Object object) {
                if (object instanceof LobbyJoinResponse) {
                    System.err.println("LobbyJoinResponse");
                    final LobbyJoinResponse connectMessage = (LobbyJoinResponse) object;

                } else if(object instanceof ProgressUpdated) {
                    System.err.println("ProgressUpdated received");
                    final ProgressUpdated upd  =(ProgressUpdated)object;

                } else if(object instanceof StartGame) {
                    final StartGame upd  =(StartGame)object;


                    System.err.println("Start Game Received");

                } else {
                    System.err.println("REC + " + object);
                }
            }
        });



        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {

                final Window window = JFrame.getWindows()[0];

                getToolkit().getSystemEventQueue().postEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
            }
        };



        formDelegate.getLeaveButton().addActionListener(listener);
        pack();

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();

        setVisible(true);
        Dimension frameSize = this.getSize();

        setLocation((screenSize.width - frameSize.width) / 2  , (screenSize.height - frameSize.height ) / 2);

         Long userId =  -1L;
        try { userId = Long.valueOf(userIdStr); } catch(Exception e) { }

        com.join(userId, playerName);

        System.err.println("slut");

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                com.client.close();
                System.exit(0);
            }
        });
    }

    public void startGame(StartGame sg) {

        try {
            basicService = (BasicService)
                    ServiceManager.lookup("javax.jnlp.BasicService");

            if(!basicService.showDocument(gameJnlpUrl)) {
                JOptionPane.showMessageDialog(this, "Unable to open " + gameJnlpUrl);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        System.exit(0);
    }

    public static void main(String args[]) {
        String s = "";
        for (int i = 0; i < args.length; i++) {
            s = s + args[i];

        }
        new LobbyClient(s);
    }


}