package se.bupp.cs3k.server.web

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-03-05
 * Time: 00:33
 * To change this template use File | Settings | File Templates.
 */

import component.contest.LadderFormPanel
import component.contest.tournament.{TournamentNodeView, TournamentViewNotStarted}
import se.bupp.cs3k.server.model
import org.specs2.mutable.Specification
import se.bupp.cs3k.server.model.GameType
import se.bupp.cs3k.server.model.GameSetupType
import se.bupp.cs3k.server.service.dao.GameSetupTypeDao
import org.apache.wicket.util.tester.WicketTester
import se.bupp.cs3k.server.service.dao.GameSetupTypeDao
import se.bupp.cs3k.server.model.GameSetupType
import org.springframework.transaction.support.{TransactionCallbackWithoutResult, TransactionTemplate}

import se.bupp.cs3k.server.service.CompetitionService
import java.io.PrintWriter
import org.springframework.transaction.TransactionStatus
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.beans.factory.BeanFactory
import javax.persistence.EntityManagerFactory
import org.springframework.context.support.FileSystemXmlApplicationContext
import org.specs2.specification.{After, Scope}
import org.specs2.mock.Mockito
import org.apache.wicket.model.Model
import se.bupp.cs3k.server.service.TournamentHelper.{QualifierState, TwoGameQualifierPositionAndSize}


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */

class WebTest extends Specification with Mockito {
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





  "asdfasdfasdf" should {
    "asdfasdf" in {
      val wt = new WicketTester()
      wt.getApplication.getMarkupSettings.setStripWicketTags(true)
      //(1 until 50 by 1).foreach { case i =>
      val i = 31
      val view = new TournamentViewNotStarted("bupp", new org.apache.wicket.model.Model(i))
      view.ladderService = new CompetitionService
      wt.startComponentInPage(view)
      Some(new PrintWriter(s"/tmp/example$i.html")).foreach{p => p.write(wt.getLastResponseAsString); p.close}
      //import sys.process._
      //() #> new java.io.File("/tmp/example.html") !
      //}
      1 === 1
    }
    "immutable maps" in {

      val wt = new WicketTester()
      wt.getApplication.getMarkupSettings.setStripWicketTags(true)

      val gameSetupDao = mock[GameSetupTypeDao]
      //import scala.collection.JavaConversions.seqAsJavaList
      val gt = new GameType('Bla, "ubpp")
      gameSetupDao.findAll returns List(
      { val a = new GameSetupType('bla,"tjo","","") ; a.gameType = gt ; a.id = 1; a},
      { val a = new GameSetupType('bla2,"tjo2","",""); a.gameType = gt; a.id = 2 ; a})

      var panel = new LadderFormPanel("asdf", "Blha", null)

      panel.gameSetupDao = gameSetupDao
      wt.startComponentInPage(panel)
      wt.getLastResponseAsString
      1.shouldEqual(1)
      wt.destroy()
    }

    "tournament maps view" in {

      val wt = new WicketTester()
      wt.getApplication.getMarkupSettings.setStripWicketTags(true)


      var lal: TwoGameQualifierPositionAndSize = new TwoGameQualifierPositionAndSize(Some("asdf"),None,3,30f,10f,100f,85f,QualifierState.Determined)
      wt.startComponentInPage(new TournamentNodeView("id",new Model(lal)))
      wt.getLastResponseAsString === ""
      1.shouldEqual(1)
      wt.destroy()
    }
  }
}

