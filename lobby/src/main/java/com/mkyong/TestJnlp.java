package com.mkyong;
 


import sun.misc.IOUtils;

import java.awt.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.*;
import javax.jnlp.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class TestJnlp {
  static BasicService basicService = null;
  public static void main(String args[]) {
    JFrame frame = new JFrame("Mkyong Jnlp UnOfficial Guide");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JLabel label = new JLabel();
    Container content = frame.getContentPane();
    content.add(label, BorderLayout.CENTER);
    String message = "Jnln Hello Word";
 
    label.setText(message);
 
    try {
      basicService = (BasicService)
        ServiceManager.lookup("javax.jnlp.BasicService");
    } catch (UnavailableServiceException e) {
      System.err.println("Lookup failed: " + e);
    }
 
    JButton button = new JButton("http://www.mkyong.com");


      String [] pbargs  = new String[] {
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
 
    button.addActionListener(listener);
 
    content.add(button, BorderLayout.SOUTH);
    frame.pack();
    frame.show();
  }
}