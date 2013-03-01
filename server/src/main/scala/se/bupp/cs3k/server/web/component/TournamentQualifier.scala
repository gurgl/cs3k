package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import java.io.PrintWriter
import org.apache.wicket.model.IModel
import org.apache.wicket.Component
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import org.apache.wicket.markup.html.WebComponent

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-12
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */

object TournamentQualifier {
  case class Alles(var p1:String, var p2:String, var id:Int, var left:Float,var top:Float,var width:Float,var height:Float) extends Serializable {

  }
  val topTextAreaHeight = 10
  val lineToTextMargin = 5
  val textLeftMargin = 5
}
class TournamentQualifier(id:String, model:IModel[TournamentQualifier.Alles]) extends WebComponent(id) {
  import TournamentQualifier._

  var m = model.getObject
  val textId1 = "text1_" + id
  val textId2 = "text2_" + id
  val pathId= "path_" + id

  val topBox = m.top //+ topTextAreaHeight + lineToTextMargin
  val heightBox = m.height // - topTextAreaHeight - lineToTextMargin
  val text1y = m.top + topTextAreaHeight
  val text2y = m.top + (m.height - lineToTextMargin)
  override def onComponentTagBody(markupStream: MarkupStream, openTag: ComponentTag) {
    //super.onComponentTagBody(markupStream, openTag)
    var stream = getResponse()//.getOutputStream()

    val path = s"m ${m.left},$topBox ${m.width},0 0,$heightBox ${-m.width},0"
    stream.write(
      s"""
        <text x="${m.left + textLeftMargin}" y="${text1y}" id="$textId1" xml:space="preserve" style="font-size:16px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:start;line-height:100%;writing-mode:lr-tb;text-anchor:start;fill:#000000;fill-opacity:1;stroke:none;font-family:Sans;-inkscape-font-specification:Sans">
          <tspan  x="${m.left + textLeftMargin}" y="${text1y}" id="tspan2826" style="font-size:16px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:start;line-height:100%;writing-mode:lr-tb;text-anchor:start;font-family:Sans;-inkscape-font-specification:Sans">
            ${m.p1}
          </tspan>
        </text>
        <text x="${m.left + textLeftMargin}" y="${text2y}" id="$textId2" xml:space="preserve" style="font-size:16px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:start;line-height:100%;writing-mode:lr-tb;text-anchor:start;fill:#000000;fill-opacity:1;stroke:none;font-family:Sans;-inkscape-font-specification:Sans">
          <tspan  x="${m.left + textLeftMargin}" y="${text2y}" id="tspan2830" style="font-size:16px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:start;line-height:100%;writing-mode:lr-tb;text-anchor:start;font-family:Sans;-inkscape-font-specification:Sans">
            ${m.p2}
          </tspan>
        </text>
        <path d="$path" id="path2842" style="fill:none;stroke:#000000;stroke-width:1;stroke-linecap:round;stroke-linejoin:round;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none"/>
      """//.stripMargin
    )
    //writer.close()
  }


    //super.onRender()



  /*
    override def getMarkupType() {
      "xml/svg";
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

