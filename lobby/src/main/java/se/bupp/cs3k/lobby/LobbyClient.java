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
import java.util.*;

public class LobbyClient extends JFrame {
    static BasicService basicService = null;

    Client client = null;

    Integer gameSize = null;
    private int lobbyPort;
    private String lobbyHost;

    URL gameJnlpUrl = null;

    public void startGame2(StartGame sg) {
        String[] pbargs = new String[]{
                "java",
                "-classpath",
                "C:/dev/workspace/opengl-tanks/target/scala-2.9.1/classes;C:/dev/workspace/opengl-tanks/lib/jME3-bullet-natives.jar;C:/dev/workspace/opengl-tanks/lib/jME3-bullet.jar;C:/dev/workspace/opengl-tanks/lib/kryonet-2.12-all.jar;C:/Users/karlw/.sbt/boot/scala-2.9.1/lib/scala-library.jar;C:/Users/karlw/.ivy2/cache/org.lwjgl.lwjgl/lwjgl/jars/lwjgl-2.8.3.jar;C:/Users/karlw/.ivy2/cache/org.lwjgl.lwjgl/lwjgl-platform/jars/lwjgl-platform-2.8.3-natives-osx.jar;C:/Users/karlw/.ivy2/cache/net.java.jinput/jinput/jars/jinput-2.0.5.jar;C:/Users/karlw/.ivy2/cache/net.java.jutils/jutils/jars/jutils-1.0.0.jar;C:/Users/karlw/.ivy2/cache/net.java.jinput/jinput-platform/jars/jinput-platform-2.0.5-natives-osx.jar;C:/Users/karlw/.ivy2/cache/org.lwjgl.lwjgl/lwjgl_util/jars/lwjgl_util-2.8.3.jar;C:/Users/karlw/.ivy2/cache/org.scalaz/scalaz-core_2.9.1/jars/scalaz-core_2.9.1-6.0.4.jar;C:/Users/karlw/.ivy2/cache/org.objenesis/objenesis/jars/objenesis-1.2.jar;C:/Users/karlw/.ivy2/cache/com.jme3/eventbus/jars/eventbus-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jinput/jars/jinput-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-blender/jars/jME3-blender-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-core/jars/jME3-core-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-desktop/jars/jME3-desktop-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-effects/jars/jME3-effects-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-jogg/jars/jME3-jogg-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-lwjgl/jars/jME3-lwjgl-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-lwjgl-natives/jars/jME3-lwjgl-natives-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-networking/jars/jME3-networking-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-niftygui/jars/jME3-niftygui-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-plugins/jars/jME3-plugins-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-terrain/jars/jME3-terrain-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-testdata/jars/jME3-testdata-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/j-ogg-oggd/jars/j-ogg-oggd-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/j-ogg-vorbisd/jars/j-ogg-vorbisd-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/lwjgl/jars/lwjgl-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty/jars/nifty-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-default-controls/jars/nifty-default-controls-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-examples/jars/nifty-examples-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-style-black/jars/nifty-style-black-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/stack-alloc/jars/stack-alloc-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/vecmath/jars/vecmath-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/xmlpull-xpp3/jars/xmlpull-xpp3-3.0.0.20120512-SNAPSHOT.jar;C:/dev/workspace/opengl-tanks/src/main/blender/",
                "se.bupp.lek.client.Client"
        };

        final ProcessBuilder pb = new ProcessBuilder(Arrays.asList(pbargs));
        try {
            System.err.print("Tja");
            Process p = pb.start();
            String out = new java.util.Scanner(p.getErrorStream()).useDelimiter("\\A").next();
            System.err.print(out);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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



    public LobbyClient(String s) {
        super("Mkyong Jnlp UnOfficial Guide");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {

            init();
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(this, "exception happens" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void init() {
        Container content = getContentPane();

        //content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));

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



        //ArrayList<Class<? extends ScalaObject>[]> types = LobbyProtocol.getTypes();



        System.err.println("bef listener");


        /*final JProgressBar prog = new JProgressBar(0);

        content.add(prog);*/
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




        setTitle("Tank Showdown lobby");
        String lobbyPortStr = System.getProperty("javaws.lobbyPort");
        String playerName = System.getProperty("javaws.playerName");
        String userIdStr = System.getProperty("javaws.userId");
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


        /*
        JButton button = new JButton("http://www.bupp.com");
        */


        //pb.directory(new File("C:\\dev\\workspace\\opengl-tanks"));
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //URL url = new URL(actionEvent.getActionCommand());
                //startGame(null);

                final Window window = JFrame.getWindows()[0];

                getToolkit().getSystemEventQueue().postEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
                //basicService.showDocument(url);
            }
        };



        formDelegate.getLeaveButton().addActionListener(listener);
          /*
        content.add(button);
        */
        pack();

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();

// Setup the frame accordingly
// This is assuming you are extending the JFrame //class
        //this.setSize(WIDTH \ 2, HEIGHT \2);
        setVisible(true);
        Dimension frameSize = this.getSize();
        //setLocation((WIDTH - frameSize.width) / 2  , (frameSize.height - WIDTH ) / 2);
        setLocation((screenSize.width - frameSize.width) / 2  , (screenSize.height - frameSize.height ) / 2);

        client.start();

        try {
            client.connect(5000, lobbyHost, lobbyPort);
        } catch (Exception e) {
            System.err.println("fel");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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