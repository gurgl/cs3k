package se.bupp.cs3k.lobby;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import se.bupp.cs3k.Greeting;
import se.bupp.cs3k.Tjena;

import java.awt.*;
import javax.swing.*;
import javax.jnlp.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;

public class LobbyClient extends JFrame {
    static BasicService basicService = null;

    Client client = null;

    public LobbyClient() {
        super("Mkyong Jnlp UnOfficial Guide");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JLabel label = new JLabel();
        Container content = getContentPane();
        content.add(label, BorderLayout.CENTER);
        String message = "Jnln Hello Word";

        client = new Client();

        Kryo kryo = client.getKryo();
        kryo.register(Tjena.class);

        System.err.println("bef listener");

        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof Tjena) {
                    System.err.println("hehj");
                    final Tjena response = (Tjena) object;
                    System.err.println(response.a());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            label.setText(response.a());
                        }
                    });
                }
            }
        });


        label.setText(message);

        try {
            basicService = (BasicService)
                    ServiceManager.lookup("javax.jnlp.BasicService");
        } catch (UnavailableServiceException e) {
            System.err.println("Lookup failed: " + e);
        }

        Greeting g = new Greeting("asdf");
        JButton button = new JButton("http://www.bupp.com");


        String[] pbargs = new String[]{
                "java",
                "-classpath",
                "C:/dev/workspace/opengl-tanks/target/scala-2.9.1/classes;C:/dev/workspace/opengl-tanks/lib/jME3-bullet-natives.jar;C:/dev/workspace/opengl-tanks/lib/jME3-bullet.jar;C:/dev/workspace/opengl-tanks/lib/kryonet-2.12-all.jar;C:/Users/karlw/.sbt/boot/scala-2.9.1/lib/scala-library.jar;C:/Users/karlw/.ivy2/cache/org.lwjgl.lwjgl/lwjgl/jars/lwjgl-2.8.3.jar;C:/Users/karlw/.ivy2/cache/org.lwjgl.lwjgl/lwjgl-platform/jars/lwjgl-platform-2.8.3-natives-osx.jar;C:/Users/karlw/.ivy2/cache/net.java.jinput/jinput/jars/jinput-2.0.5.jar;C:/Users/karlw/.ivy2/cache/net.java.jutils/jutils/jars/jutils-1.0.0.jar;C:/Users/karlw/.ivy2/cache/net.java.jinput/jinput-platform/jars/jinput-platform-2.0.5-natives-osx.jar;C:/Users/karlw/.ivy2/cache/org.lwjgl.lwjgl/lwjgl_util/jars/lwjgl_util-2.8.3.jar;C:/Users/karlw/.ivy2/cache/org.scalaz/scalaz-core_2.9.1/jars/scalaz-core_2.9.1-6.0.4.jar;C:/Users/karlw/.ivy2/cache/org.objenesis/objenesis/jars/objenesis-1.2.jar;C:/Users/karlw/.ivy2/cache/com.jme3/eventbus/jars/eventbus-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jinput/jars/jinput-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-blender/jars/jME3-blender-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-core/jars/jME3-core-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-desktop/jars/jME3-desktop-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-effects/jars/jME3-effects-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-jogg/jars/jME3-jogg-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-lwjgl/jars/jME3-lwjgl-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-lwjgl-natives/jars/jME3-lwjgl-natives-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-networking/jars/jME3-networking-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-niftygui/jars/jME3-niftygui-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-plugins/jars/jME3-plugins-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-terrain/jars/jME3-terrain-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/jME3-testdata/jars/jME3-testdata-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/j-ogg-oggd/jars/j-ogg-oggd-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/j-ogg-vorbisd/jars/j-ogg-vorbisd-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/lwjgl/jars/lwjgl-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty/jars/nifty-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-default-controls/jars/nifty-default-controls-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-examples/jars/nifty-examples-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/nifty-style-black/jars/nifty-style-black-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/stack-alloc/jars/stack-alloc-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/vecmath/jars/vecmath-3.0.0.20120512-SNAPSHOT.jar;C:/Users/karlw/.ivy2/cache/com.jme3/xmlpull-xpp3/jars/xmlpull-xpp3-3.0.0.20120512-SNAPSHOT.jar;C:/dev/workspace/opengl-tanks/src/main/blender/",
                "se.bupp.lek.client.Client"
        };

        final ProcessBuilder pb = new ProcessBuilder(Arrays.asList(pbargs));
        //pb.directory(new File("C:\\dev\\workspace\\opengl-tanks"));
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //URL url = new URL(actionEvent.getActionCommand());
                try {
                    System.err.print("Tja");
                    Process p = pb.start();
                    String out = new java.util.Scanner(p.getErrorStream()).useDelimiter("\\A").next();
                    System.err.print(out);
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                //basicService.showDocument(url);

            }
        };


        JProgressBar prog = new JProgressBar(0,2);

        content.add(prog);
        prog.setValue(1);
        button.addActionListener(listener);

        content.add(button, BorderLayout.SOUTH);
        pack();
        show();

        client.start();
        try {
            client.connect(5000, "localhost", 12345);
        } catch (IOException e) {
            System.err.println("fel");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Tjena r = new Tjena("asdf", 123);
        r.b_$eq(1);
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
         new LobbyClient();
    }
}