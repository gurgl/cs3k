package se.bupp.cs3k.server.web.component

import org.apache.wicket.markup.html.panel.Panel
import java.io.PrintWriter
import org.apache.wicket.markup.MarkupStream
import org.apache.wicket.model.Model
import se.bupp.cs3k.server.web.component.TournamentQualifier.Alles
import org.apache.wicket.spring.injection.annot.SpringBean
import se.bupp.cs3k.server.service.{Blabb, LadderService}
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
class TournamentView(id:String) extends Panel(id) {

  @SpringBean
  var ladderService:LadderService = _





  /*def build(q:Qualifier, stepsToBottom:Int) : List[Alles] = {
    q.childrenOpt match {
      case Some(children) =>
      case None =>
    }
    */


  /*def createTournament() : List = {
    var tournament = ladderService.buildATournament(10)


    tournament
  }*/

  var buffa = {
    val yMod = 50
    var yo = new Blabb.Yo[Alles](
      (offsetY, subTreeMaxHeight, stepsToBottom) => {
        new Alles("Nisse","Lars", 1234, stepsToBottom*100 , yMod * (offsetY.toFloat - 0.5f + subTreeMaxHeight.toFloat / 2f), 100, yMod*subTreeMaxHeight / 2.0f)
      }
    )

    val qa = ladderService.buildATournament(10)
    var listn = yo.build(qa, 0.0f, 3)._1
    import scala.collection.JavaConversions.seqAsJavaList

    new ListModel(listn)
  }


  var view = new ListView[Alles]("it", buffa) {
    def populateItem(p1: ListItem[Alles]) {
      println("Rend " + p1.getIndex)
      p1.add(new TournamentQualifier("item", p1.getModel))

    }
  }
  add(view)
}

