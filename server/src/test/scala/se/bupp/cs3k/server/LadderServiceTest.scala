package se.bupp.cs3k.server

import model.Qualifier
import org.specs2.mutable.Specification
import io.Source
import java.net.URL
import service.Blabb.XY
import service.{Blabb, CompetitionService}
import web.component.TournamentQualifier.Alles
import org.apache.wicket.util.tester.WicketTester

import java.io.PrintWriter
import org.apache.wicket.model.Model
import web.component.TournamentView

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */

class LadderServiceTest extends Specification {
  sequential
  "competition" should {

    "create a correct tournament from num of player" in {

      var ls = new CompetitionService()

      val qa = ls.buildATournament(10)

      ls.count(qa) === 9


      val qa2 = ls.buildATournament(2)
      ls.count(qa2) === 1


      val qa3 = ls.buildATournament(8)
      ls.count(qa3) === 7


      val qa4 = ls.buildATournament(4)
      ls.count(qa4) === 3
    }

    "bupps" in {
      var ls = new CompetitionService()

      val qa = ls.buildATournament(10)

      var res = Blabb.yeah.build(qa, 0, 3)
      res._1 must haveTheSameElementsAs(List(XY(0.0f,0), XY(1.0f,0), XY(0.5f,1), XY(2.0f,1), XY(1.5f,2), XY(4.0f,1), XY(5.0f,1), XY(4.5f,2), XY(3.5f,3)))



    }

    "bupps1" in {
      var ls = new CompetitionService()

      val qa = ls.buildATournament(8)

      var res = Blabb.yeah.build(qa, 0, 3)
      res._1 must haveTheSameElementsAs(List(XY(0.0f,0), XY(1.0f,0), XY(0.5f,1), XY(2.0f,1), XY(1.5f,2), XY(4.0f,1), XY(5.0f,1), XY(4.5f,2), XY(3.5f,3)))



    }

    "bupps2" in {
      var ls = new CompetitionService()

      val qa = ls.buildATournament(10)



      var yo = new Blabb.Yo[Alles](
        (offsetY, subTreeMaxHeight, stepsToBottom) => {
          new Alles("Nisse","Lars", 1234, stepsToBottom*100 ,50 * (offsetY.toFloat - 0.5f + subTreeMaxHeight.toFloat / 2f),  100, 40)
        }
      )
      yo.build(qa,10f,3)
      1 === 2
    }
  }

  "asdfasdfasdf" should {
    "asdfasdf" in {
      val wt = new WicketTester()
      wt.getApplication.getMarkupSettings.setStripWicketTags(true)
      //(1 until 50 by 1).foreach { case i =>
      val i = 31
      var view = new TournamentView("bupp", new Model(i))
        view.ladderService = new CompetitionService
        wt.startComponentInPage(view)
        Some(new PrintWriter(s"/tmp/example$i.html")).foreach{p => p.write(wt.getLastResponseAsString); p.close}
        //import sys.process._
        //() #> new java.io.File("/tmp/example.html") !
      //}
      1 === 1
    }
  }

}

/*
       x
      /
     x
    / \
   /   x
  x
   \   x
    \ /
     x


*/