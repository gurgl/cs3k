package se.bupp.cs3k.server.web

import org.apache.wicket.markup.html.panel.Panel
import java.io.PrintWriter
import org.apache.wicket.markup.MarkupStream

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-12
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */
class TourmentView {// extends Panel {

/*
  override def getMarkupType() {
    "xml/svg";
  }
  override def onRender() = {
    val writer = new PrintWriter(getResponse().getOutputStream());
    writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n" +
      "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n" +
      "<svg width=\"100%\" height=\"100%\" version=\"1.1\"\n" +
      "xmlns=\"http://www.w3.org/2000/svg\">\n")

    super.onRender()
    writer.write(
      "</svg> ")
  }


  class TreeViewNode() extends Panel {


    override def getMarkupType() {
      "xml/svg";
    }


    override def onRender(markupStream:MarkupStream ) {
      val writer = new PrintWriter(getResponse().getOutputStream());
      writer.write(makeRectangleSVG());
      writer.flush();
      writer.close();
    }

    private def makeRectangleSVG() = {

        "\n" +
        "<rect width=\"300\" height=\"100\"\n" +
        "style=\"fill:rgb(0,0,255);stroke-width:1;\n" +
        "stroke:rgb(0,0,0)\"/>\n" +
        "\n"
        ;
    }
  }*/
}

