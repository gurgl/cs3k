package se.bupp.cs3k.server.web.component

import org.apache.wicket.model.IModel
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import org.apache.wicket.markup.html.WebComponent
import se.bupp.cs3k.server.service.TournamentHelper.TwoGameQualifierPositionAndSize
import se.bupp.cs3k.server.service.TournamentHelper

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-12
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */


class TournamentNodeView(id:String, model:IModel[_ <: TwoGameQualifierPositionAndSize]) extends WebComponent(id) {

  import TournamentHelper._
  var m = model.getObject

  val textId1 = "text1_" + id
  val textId2 = "text2_" + id


  val pathId= "path_" + id

  val topBox = m.top //+ topTextAreaHeight + lineToTextMargin
  val heightBox = m.height // - topTextAreaHeight - lineToTextMargin
  val text1y = m.top - lineToTextMargin
  val text2y = m.top + (m.height - lineToTextMargin)
  override def onComponentTagBody(markupStream: MarkupStream, openTag: ComponentTag) {
    //super.onComponentTagBody(markupStream, openTag)
    var stream = getResponse()//.getOutputStream()

    val path = s"m ${m.left},$topBox ${m.width},0 0,$heightBox ${-m.width},0"
    stream.write(
      s"""
        <text x="${m.left + textLeftMargin}" y="${text1y}" id="$textId1" xml:space="preserve" class="svg-tour-text-css">
          <tspan  x="${m.left + textLeftMargin}" y="${text1y}" id="tspan2826" style="svg-tour-tspan-css">
            ${m.p1}
          </tspan>
        </text>
        <text x="${m.left + textLeftMargin}" y="${text2y}" id="$textId2" xml:space="preserve" class="svg-tour-text-css">
          <tspan  x="${m.left + textLeftMargin}" y="${text2y}" id="tspan2830" style="svg-tour-tspan-css">
            ${m.p2}
          </tspan>
        </text>
        <path d="$path" id="path2842" class="svg-tour-path-css"/>
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
