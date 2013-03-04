package se.bupp.cs3k.server

import model._
import org.specs2.mutable.Specification
import io.Source
import java.net.URL
import service.TournamentHelper.XY
import service.TournamentHelper.XY
import service.dao.{CompetitionDao, GameSetupTypeDao, TeamDao}
import service.{CompetitorService, TournamentHelper, CompetitionService}
import web.component.TournamentNodeView.TwoGameQualifierPositionAndSize
import org.apache.wicket.util.tester.WicketTester

import java.io.PrintWriter
import org.apache.wicket.model.Model
import web.component.TournamentNodeView.TwoGameQualifierPositionAndSize
import web.component.TournamentView
import org.springframework.context.support.FileSystemXmlApplicationContext
import org.springframework.beans.factory.BeanFactory
import org.springframework.transaction.{TransactionStatus, PlatformTransactionManager}
import javax.persistence.EntityManagerFactory
import org.springframework.orm.jpa.JpaTransactionManager
import org.specs2.specification.{After, Scope}
import org.springframework.transaction.support.{TransactionCallbackWithoutResult, TransactionTemplate}
import scala.Some
import se.bupp.cs3k.model.{CompetitionState, CompetitorType}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */

class LadderServiceTest extends Specification {
  sequential


  trait TestDataGameSetup {
    self:InApplicationContext =>

    val gs = new GameType('super_mario2, "Super Mario")
    val gst = new GameSetupType(Symbol("2vs2"), "Versus Mode",
      "se.bupp.cs3k.example.ExampleScoreScheme.ExContestScore",
      "se.bupp.cs3k.example.ExampleScoreScheme.ExScoreScheme")
    gst.gameType = gs

    var gstDao = factory.getBean(classOf[GameSetupTypeDao])

    new TransactionTemplate(txMgr).execute(new TransactionCallbackWithoutResult {
      def doInTransactionWithoutResult(p1: TransactionStatus) {
        gstDao.em.persist(gs)
        gstDao.insert(gst)
      }
    })
    gst.ensuring( _.id != null)

  }

  trait InApplicationContext extends Scope with After {
    val appContext = new FileSystemXmlApplicationContext("server/src/test/resources/applicationContext.xml");
    val factory =  appContext.asInstanceOf[BeanFactory];

    val emf = factory.getBean(classOf[EntityManagerFactory])
    //var em = emf.createEntityManager()
    var txMgr = factory.getBean("transactionManager", classOf[JpaTransactionManager])
    //val tdef = new DefaultTransactionDefinition();
    var tx = null//txMgr.getTransaction(tdef)



    def doInTx[G]( body : => G) {
      new TransactionTemplate(txMgr).execute(new TransactionCallbackWithoutResult {
        def doInTransactionWithoutResult(p1: TransactionStatus) {
          body
        }
      })
    }

    def after = {

      //em.close()
      emf.close()

      appContext.close()
      appContext.destroy()

    }
  }




  "competition" should {

    "create a correct tournament from num of player" in {

      var ls = TournamentHelper

      val qa = ls.createTournamentStructure(10)

      ls.count(qa) === 9


      val qa2 = ls.createTournamentStructure(2)
      ls.count(qa2) === 1


      val qa3 = ls.createTournamentStructure(8)
      ls.count(qa3) === 7


      val qa4 = ls.createTournamentStructure(4)
      ls.count(qa4) === 3
    }

    "bupps" in {
      var ls = TournamentHelper

      val qa = ls.createTournamentStructure(10)

      var res = TournamentHelper.dummyVisualizer.build(qa, 0, 3)
      res._1 must haveTheSameElementsAs(List(XY(0.0f,0), XY(1.0f,0), XY(0.5f,1), XY(2.0f,1), XY(1.5f,2), XY(4.0f,1), XY(5.0f,1), XY(4.5f,2), XY(3.5f,3)))



    }

    "bupps1" in {
      var ls = TournamentHelper

      val qa = ls.createTournamentStructure(8)

      var res = TournamentHelper.dummyVisualizer.build(qa, 0, 3)
      res._1 must haveTheSameElementsAs(List(XY(0.0f,1), XY(1.0f,1), XY(0.5f,2), XY(2.0f,1), XY(3.0f,1), XY(2.5f,2), XY(1.5f,3)))



    }

    /*"bupps2" in {
      var ls = TournamentHelper

      val qa = ls.createTournamentStructure(10)



      var yo = new TournamentHelper.PositionOnlyVisualizer[TwoGameQualifierPositionAndSize](
        (offsetY, subTreeMaxHeight, stepsToBottom) => {
          new TwoGameQualifierPositionAndSize("Nisse","Lars", 1234, stepsToBottom*100 ,50 * (offsetY.toFloat - 0.5f + subTreeMaxHeight.toFloat / 2f),  100, 40)
        }
      )
      yo.build(qa,10f,3)
      1 === 2
    }*/
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

  "build qualfiier structure" should {
    "serialize and deserialize tournament structure in a readable format into database" in new InApplicationContext with TestDataGameSetup {

      var competitionDao = factory.getBean(classOf[CompetitionDao])
      var competitionService = factory.getBean(classOf[CompetitionService])

      var competitorService = factory.getBean(classOf[CompetitorService])

      (0 until 10).foreach ( i => competitorService.createTeam(new Team(i.toString)))

      var teamDao = factory.getBean(classOf[TeamDao])
      teamDao.findAll.size == 10


      var tournament = new Tournament("lal", CompetitorType.INDIVIDUAL, gst, CompetitionState.SIGNUP)

      competitionService.storeCompetition(tournament)


      teamDao.findAll.foreach { case t:Team =>
        competitionService.addCompetitorToCompetition(t,tournament)
      }

      var numOfPlayers:Int = -1
      var tournamentPrim:Tournament = null
      doInTx {
        tournamentPrim = competitionDao.find(tournament.id).get.asInstanceOf[Tournament]

        numOfPlayers = tournamentPrim.participants.size
      }
      numOfPlayers === 10
      var structure = TournamentHelper.createTournamentStructure(numOfPlayers)
      var indexed = TournamentHelper.index(structure)
      indexed === IndexedQualifier(None,Some(List(IndexedQualifier(None,Some(List(IndexedQualifier(None,Some(List(IndexedQualifier(None,None,1), IndexedQualifier(None,None,2))),3), IndexedQualifier(None,None,4))),5), IndexedQualifier(None,Some(List(IndexedQualifier(None,None,6), IndexedQualifier(None,None,7))),8))),9)

      competitionService.generateTournamentStructure(tournamentPrim,indexed)

      var tournamentBis:Tournament = null
      doInTx {
        tournamentBis = competitionDao.find(tournament.id).get.asInstanceOf[Tournament]
        tournamentBis.structure.size === 9

      }
      //var persistedStructure = tournamentBis.structure

      import scala.collection.JavaConversions.asScalaBuffer


      var tree = competitionService.getTournamentQualifierStructure(tournamentBis)
      /*doInTx {
        tree = TournamentHelper.fromPersistedToQualifierTree(persistedStructure.toList)
      }*/
      tree === Qualifier(9,List(
        Qualifier(5,List(
          Qualifier(3,List(
            Qualifier(1,Nil,Some(3)),
            Qualifier(2,Nil,Some(3))
          ),Some(5)),
          Qualifier(4,Nil,Some(5))
        ),Some(9)),
        Qualifier(8,List(
          Qualifier(6,Nil,Some(8)),
          Qualifier(7,Nil,Some(8))
        ),Some(9))
      ),None)
      TournamentHelper.indexedToSimple(indexed) === tree


      competitionService.distrubtutePlayersInTournament(tournamentBis,lawl)

      1 === 1
    }

    "distribute " in {


      lal((0L until 10L).toList).toList must haveTheSameElementsAs(List((0L,5), (5L,0), (1L,2), (6L,7), (9L,8), (2L,9), (7L,4), (3L,6), (8L,1), (4L,3)))

    }


  }

  val lawl = Lal.lol[Competitor]

  val lal = Lal.lol[Long]
}

object Lal {
  def lol[G]:TournamentHelper.SlotDistributor[G] = (competitorIds:List[G]) => {
    var slots = (0 until competitorIds.size).map( i => (i,Option.empty[G])).toMap

    var ptr = 5
    competitorIds.foreach { cid =>

      while(slots(ptr).isDefined) {
        ptr = ptr + 3
        ptr = ptr % 10
      }
      slots = slots + (ptr -> Some(cid))
    }
    slots.map { case (k,v) => k -> v.get }
  }

}