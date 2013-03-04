package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import java.io.PrintWriter
import org.apache.wicket.model.IModel
import org.apache.wicket.Component
import org.apache.wicket.markup.{ComponentTag, MarkupStream}
import org.apache.wicket.markup.html.WebComponent
import org.apache.wicket.markup.head.IHeaderResponse
import se.bupp.cs3k.server.service.TournamentHelper
import se.bupp.cs3k.server.model.HasQualifierDetails

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-12
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */

object TournamentNodeView {
  case class TwoGameQualifierPositionAndSize(var p1:String, var p2:String, var id:Int, var left:Float,var top:Float,var width:Float,var height:Float,state:QualifierState.QualifierState = QualifierState.Undetermined) extends Serializable {

  }


  object QualifierState extends Enumeration {
    type QualifierState = Value
    val Played,Determined,Undetermined = Value
  }
  val topTextAreaHeight = 10
  val lineToTextMargin = 5
  val textLeftMargin = 5


  def createLayout(numOfPlayers:Int ): List[TournamentNodeView.TwoGameQualifierPositionAndSize] = {
    val yMod = 70
    val screenOffsetY = 20
    var i = 0

    val yo = new TournamentHelper.ArmHeightVisualizer[TwoGameQualifierPositionAndSize](
      (offsetY, subTreesHeights, subTreeHeight, stepsToBottom, nodeId) => {
        def bupp(height: Float) = {
          yMod * (offsetY.toFloat + (subTreeHeight.toFloat / 2f + height / 2f))
        }

        val (top, bot) = subTreesHeights match {
          case x :: y :: Nil =>
            println(x + " " + y)
            (bupp(-x), bupp(y))
          case x :: Nil => (bupp(-x), bupp(0.5f))
          case Nil => (bupp(-0.5f), bupp(0.5f))
        }
        //i = i +1
        //println(s"$top $bot ${subTreesHeights.size} $subTreesHeights")
        new TwoGameQualifierPositionAndSize(subTreesHeights.lift(0).toString, subTreesHeights.lift(1).toString, 1234, stepsToBottom * 100, screenOffsetY + top, 100, math.abs(top - bot))
      }
    )

    val numOfCompleteLevels: Int = TournamentHelper.log2(numOfPlayers)
    val numOfPlayersMoreThanCompleteLevels: Int = numOfPlayers % (1 << numOfCompleteLevels)
    val numOfLevels = numOfCompleteLevels + (if (numOfPlayersMoreThanCompleteLevels > 0) 1 else 0) - 1

    val qa = TournamentHelper.indexedToSimple(TournamentHelper.index(TournamentHelper.createTournamentStructure(numOfPlayers)))
    var listn = yo.build(qa, 0.0f, numOfLevels)._1
    listn
  }


}
class TournamentNodeView(id:String, model:IModel[_ <: TournamentNodeView.TwoGameQualifierPositionAndSize]) extends WebComponent(id) {
  import TournamentNodeView._

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

