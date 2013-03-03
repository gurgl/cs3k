package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import java.io.PrintWriter
import org.apache.wicket.markup.MarkupStream
import org.apache.wicket.model.{IModel, Model}
import se.bupp.cs3k.server.web.component.TournamentQualifier.Alles
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.{Blabb, CompetitionService}
import se.bupp.cs3k.server.model.Qualifier
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.apache.wicket.model.util.ListModel

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-12
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */
class TournamentView(id:String, mod:IModel[Integer]) extends Panel(id) {

  @SpringBean
  var ladderService:CompetitionService = _

  var buffa:ListModel[Alles] = new ListModel[Alles]()

  override def onBeforeRender() {
    var numOfPlayers = mod.getObject
    val yMod = 70
    val screenOffsetY  = 20
    var i = 0

    val yo = new Blabb.Yo2[Alles](
      (offsetY, subTreesHeights,subTreeHeight,  stepsToBottom) => {
        def bupp(height:Float) = {
          yMod * (offsetY.toFloat  + (subTreeHeight.toFloat /2f + height /2f) )
        }

        val (top, bot) = subTreesHeights match {
          case x :: y :: Nil  =>
            println(x + " " + y)
            (bupp(-x),bupp(y))
          case x :: Nil => (bupp(-x),bupp(0.5f))
          case Nil => (bupp(-0.5f),bupp(0.5f))
        }
        //i = i +1
        //println(s"$top $bot ${subTreesHeights.size} $subTreesHeights")
        new Alles(subTreesHeights.lift(0).toString,subTreesHeights.lift(1).toString, 1234, stepsToBottom * 100 ,screenOffsetY +  top, 100, math.abs(top-bot))
      }
    )

    val numOfCompleteLevels:Int = ladderService.log2(numOfPlayers)
    val numOfPlayersMoreThanCompleteLevels:Int = numOfPlayers % (1 << numOfCompleteLevels)
    val numOfLevels = numOfCompleteLevels + (if(numOfPlayersMoreThanCompleteLevels > 0) 1 else 0) -1

    val qa = ladderService.buildATournament(numOfPlayers)
    var listn = yo.build(qa, 0.0f, numOfLevels)._1
    import scala.collection.JavaConversions.seqAsJavaList


    buffa.setObject(listn)
    super.onBeforeRender()
  }

  var view = new ListView[Alles]("it", buffa) {
    def populateItem(p1: ListItem[Alles]) {
      println("Rend " + p1.getIndex)
      p1.add(new TournamentQualifier("item", p1.getModel))

    }
  }
  add(view)
}

