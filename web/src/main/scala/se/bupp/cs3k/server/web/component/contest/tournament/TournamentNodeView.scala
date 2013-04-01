package se.bupp.cs3k.server.web.component.contest.tournament

import org.apache.wicket.model.IModel
import org.apache.wicket.model.Model
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import org.apache.wicket.markup.html.WebComponent
import se.bupp.cs3k.server.service.TournamentHelper.TwoGameQualifierPositionAndSize
import se.bupp.cs3k.server.service.TournamentHelper
import org.apache.wicket.markup.html.panel.Panel
import se.bupp.cs3k.server.web.component.contest.tournament.SvgLabel

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-12
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */

class SvgPath(id:String,pm:IModel[String],cssClassM:IModel[String]) extends WebComponent(id) {


  override def onComponentTag(tag: ComponentTag) {
    super.onComponentTag(tag)
    tag.append("d",pm.getObject,"")
    tag.put("class",tag.getAttribute("class") + " " + cssClassM.getObject)
  }
        /*
  override def onComponentTag(markupStream: MarkupStream, openTag: ComponentTag) {

    var stream = getResponse()//.getOutputStream()

    //throw new RuntimeException("AWER")

    stream.write(
      s"""
        <path d="${pm.getObject}" id="path2842" class="svg-tour-path-css ${cssClassM.getObject}"/>
      """//.stripMargin
    )
    super.onComponentTagBody(markupStream, openTag)
    //writer.close()
  }
  */
}


class TournamentNodeView(id:String, model:IModel[_ <: TwoGameQualifierPositionAndSize]) extends Panel(id) {

  import TournamentHelper._
  var m = model.getObject

  val textId1 = "text1_" + id
  val textId2 = "text2_" + id


  val pathId= "path_" + id
  val cssClass = m.state match {
    case QualifierState.Determined => "determined"
    case QualifierState.Played => "played"
    case QualifierState.Undetermined => "undetermined"
  }
  val topBox = m.top //+ topTextAreaHeight + lineToTextMargin
  val heightBox = m.height // - topTextAreaHeight - lineToTextMargin
  val text1y = m.top - lineToTextMargin
  val text2y = m.top + (m.height - lineToTextMargin)

  val path = s"m ${m.left},$topBox ${m.width},0 0,$heightBox ${-m.width},0"
  add(new SvgLabel("lbl1",new Model(new java.lang.Float(m.left + textLeftMargin)),new Model(text1y),new Model(m.p1.map(x => "").getOrElse("Undecided")), new Model(m.p1)));
  add(new SvgLabel("lbl2",new Model(new java.lang.Float(m.left + textLeftMargin)),new Model(text2y),new Model(m.p2.map(x => "").getOrElse("Undecided")), new Model(m.p2)));

  add(new SvgPath("path", new Model(path),new Model(cssClass)))



  /*
        <text x="${m.left + textLeftMargin}" y="${text1y}" id="$textId1" xml:space="preserve" class="svg-tour-text-css">
            <tspan  x="${m.left + textLeftMargin}" y="${text1y}" id="tspan2826" class="svg-tour-tspan-css ${m.p1.map(x => "").getOrElse("Undecided")}">
              ${m.p1.getOrElse("Undecided")}
            </tspan>
          </text>
          <text x="${m.left + textLeftMargin}" y="${text2y}" id="$textId2" xml:space="preserve" class="svg-tour-text-css">
            <tspan  x="${m.left + textLeftMargin}" y="${text2y}" id="tspan2830" class="svg-tour-tspan-css ${m.p2.map(x => "").getOrElse("Undecided")}">
              ${m.p2.getOrElse("Undecided")}
            </tspan>
          </text>

   */
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

