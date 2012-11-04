package se.bupp.cs3k.lobby;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.BeanSerializer;
import com.esotericsoftware.kryonet.*;
import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.shaded.org.objenesis.instantiator.ObjectInstantiator;
import scala.Tuple2;
import se.bupp.cs3k.*;

import java.awt.*;
import javax.swing.*;
import javax.jnlp.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;


public class LobbyClient extends JFrame {
    static BasicService basicService = null;

    Client client = null;

    Integer gameSize = null;
    private int lobbyPort;
    private String lobbyHost;

    URL gameJnlpUrl = null;

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


        String message = "Not connected";

        final Lal formDelegate = new Lal();
        final JComponent form = formDelegate.$$$getRootComponent$$$();
        content.add(form);
        formDelegate.getProgressBar().setStringPainted(true);

        formDelegate.getProgressBar().setString(message);
        Kryo kryo = new Kryo();
        kryo.setDefaultSerializer(BeanSerializer.class);

        for(Tuple2<Class<?>,ObjectInstantiator> clz : LobbyProtocol.getTypesAndSerializer()) {
            System.err.println("Registering " + clz._1().getName());
            kryo.register(clz._1()).setInstantiator(clz._2());
        }
        KryoSerialization kryoSerialization = new KryoSerialization(kryo);
        client = new Client(8192, 2048,kryoSerialization);

        System.err.println("bef listener");


        client.addListener(new Listener() {

            public void disconnected (Connection connection) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        formDelegate.getProgressBar().setString("Disconnected");

                    }
                });
            }

            public void received(Connection connection, Object object) {
                if (object instanceof LobbyJoinResponse) {
                    System.err.println("LobbyJoinResponse");
                    final LobbyJoinResponse connectMessage = (LobbyJoinResponse) object;
                    gameSize = connectMessage.getParticipantsRequired();

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            formDelegate .getProgressBar().setMaximum(gameSize);
                        }
                    });
                } else if(object instanceof ProgressUpdated) {
                    System.err.println("ProgressUpdated received");
                    final ProgressUpdated upd  =(ProgressUpdated)object;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            formDelegate.getProgressBar().setString("" + upd.getProgress() + " of " + gameSize + " connected.");


                            formDelegate .getProgressBar().setValue(upd.getProgress());
                        }
                    });

                } else if(object instanceof StartGame) {
                    final StartGame upd  =(StartGame)object;


                    System.err.println("Start Game Received");
                    try {
                        gameJnlpUrl = new URL(upd.getJnlpURL());
                    } catch (MalformedURLException e) {
                        JOptionPane.showMessageDialog(LobbyClient.this, "Bad url" + upd.getJnlpURL());
                    }
                    System.err.println(upd.getJnlpURL());


                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            formDelegate.getProgressBar().setString("Game Started");
                            formDelegate.getProgressBar().setValue(0);
                            startGame(upd);
                        }
                    });
                } else {
                    System.err.println("REC + " + object);
                }
            }
        });

        String lobbyPortStr = System.getProperty("javaws.lobbyPort");
        String playerName = System.getProperty("javaws.playerName");
        String userIdStr = System.getProperty("javaws.userId");
        setTitle("Tank Showdown lobby" + playerName);
        if(lobbyPortStr != null) {
            lobbyPort = Integer.parseInt(lobbyPortStr);
        } else {
            lobbyPort = 12345;
        }

        try {
            basicService = (BasicService)
                    ServiceManager.lookup("javax.jnlp.BasicService");
            URL codeBase = basicService.getCodeBase();
            lobbyHost = codeBase.getHost();

        } catch (UnavailableServiceException e) {
            System.err.println("Lookup failed: " + e);
        }


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

        client.start();

        try {
            client.connect(5000, lobbyHost, lobbyPort);
        } catch (Exception e) {
            System.err.println("fel");
            e.printStackTrace();
        }

        Long userId =  -1L;
        try { userId = Long.valueOf(userIdStr); } catch(Exception e) { }
        LobbyJoinRequest r = new LobbyJoinRequest(userId,playerName);
        //r.b_$eq(1);
        client.sendTCP(r);
        System.err.println("slut");

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                client.close();
                System.exit(0);
            }
        });
    }

    public static void main(String args[]) {
        String s = "";
        for (int i = 0; i < args.length; i++) {
            s = s + args[i];

        }
        new LobbyClient(s);
    }
}