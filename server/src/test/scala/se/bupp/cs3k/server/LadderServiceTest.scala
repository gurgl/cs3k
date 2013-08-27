package se.bupp.cs3k.server

import model._
import model.IndexedQualifier
import model.Qualifier
import org.specs2.mutable.Specification
import io.Source
import java.net.URL
import service.TournamentHelper.TwoGameQualifierPositionAndSize
import service.TournamentHelper.XY
import service.TournamentHelper.{QualifierState, TwoGameQualifierPositionAndSize, XY}
import service.dao.{CompetitionDao, GameSetupTypeDao, TeamDao}
import service._

import gameserver.{GameServerRepositoryService, GameProcessTemplate, GameServerRepository}
import org.apache.wicket.util.tester.WicketTester

import java.io.PrintWriter
import org.apache.wicket.model.Model

import org.springframework.context.support.FileSystemXmlApplicationContext
import org.springframework.beans.factory.BeanFactory
import org.springframework.transaction.{TransactionStatus, PlatformTransactionManager}
import javax.persistence.EntityManagerFactory
import org.springframework.orm.jpa.JpaTransactionManager
import org.specs2.specification.{After, Scope}
import org.springframework.transaction.support.{TransactionCallback, TransactionCallbackWithoutResult, TransactionTemplate}
import scala.Some
import se.bupp.cs3k.model.{CompetitionState, CompetitorType}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import scala.Some
import org.specs2.mock.Mockito
import java.util


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
class LadderServiceTest extends Specification with Mockito {
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

    Init.gameReservationServiceStore = new GameReservationServiceStore


    def doInTx[G]( body : => G) {
      new TransactionTemplate(txMgr).execute(new TransactionCallbackWithoutResult {
        def doInTransactionWithoutResult(p1: TransactionStatus) {
          body
        }
      })
    }

    def doInTxWithRes[G]( body : => G) : G = {
      new TransactionTemplate(txMgr).execute(new TransactionCallback[G] {
        def doInTransaction(p1: TransactionStatus) = body
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

      competitionService.storeTournamentStructure(tournamentPrim,indexed)

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


      competitionService.distributePlayersInTournament(tournamentBis,2,lawl)

      1 === 1
    }


    "verify state change" in new InApplicationContext with TestDataGameSetup {
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

      competitionService.startTournament(tournamentPrim)

      var tournamentBis:Tournament = null
      doInTx {
        tournamentBis = competitionDao.find(tournament.id).get.asInstanceOf[Tournament]
      }

      tournamentBis.state === CompetitionState.RUNNING
    }


    "asdf" in new InApplicationContext with TestDataGameSetup {
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

      competitionService.startTournament(tournamentPrim)

      var tournamentBis:Tournament = null
      doInTx {
        tournamentBis = competitionDao.find(tournament.id).get.asInstanceOf[Tournament]
      }

      tournamentBis.state === CompetitionState.RUNNING

      var layout = competitionService.createLayout2(tournamentPrim)
      layout must haveTheSameElementsAs(
        List(
          TwoGameQualifierPositionAndSize(Some("0"),Some("7"),1,0.0f,37.5f,100.0f,35.0f,QualifierState.Determined),
           TwoGameQualifierPositionAndSize(Some("4"),Some("1"),2,0.0f,107.5f,100.0f,35.0f,QualifierState.Determined),
           TwoGameQualifierPositionAndSize(None,None,3,100.0f,55.0f,100.0f,70.0f,QualifierState.Undetermined),
           TwoGameQualifierPositionAndSize(Some("8"),Some("5"),4,100.0f,177.5f,100.0f,35.0f,QualifierState.Determined),
           TwoGameQualifierPositionAndSize(None,None,5,200.0f,90.0f,100.0f,105.0f,QualifierState.Undetermined),
           TwoGameQualifierPositionAndSize(Some("2"),Some("9"),6,100.0f,317.5f,100.0f,35.0f,QualifierState.Determined),
           TwoGameQualifierPositionAndSize(Some("6"),Some("3"),7,100.0f,387.5f,100.0f,35.0f,QualifierState.Determined),
           TwoGameQualifierPositionAndSize(None,None,8,200.0f,335.0f,100.0f,70.0f,QualifierState.Undetermined),
           TwoGameQualifierPositionAndSize(None,None,9,300.0f,160.0f,100.0f,210.0f,QualifierState.Undetermined))
        )


    }


    "complete tournament from start to finish" in new InApplicationContext with TestDataGameSetup {
      var competitionDao = factory.getBean(classOf[CompetitionDao])
      var competitionService = factory.getBean(classOf[CompetitionService])
      var gameReservationService = factory.getBean(classOf[GameReservationService])
      var resultService = factory.getBean(classOf[ResultService])

      var competitorService = factory.getBean(classOf[CompetitorService])

      import scala.collection.JavaConversions.asScalaBuffer

      def findQualifier(tournament: Tournament, teamNames: List[String])  = {
        val tournamentBis = competitionDao.find(tournament.id).get.asInstanceOf[Tournament]
        val teams = teamDao.findAll.filter(s => teamNames.contains(s.nameAccessor))
        val qualifier = tournamentBis.structure.find(
          g => {
            var participants = g.gameOccassionOpt.get.participants
            //println(participants.map(_.id.competitor.id).mkString(","))
            g.gameOccassionOpt.get.participants.forall(s => teams.contains(s.id.competitor))
          }
        ).get
        qualifier
      }


      (0 until 5).foreach ( i => competitorService.createTeam(new Team(i.toString)))

      var teamDao = factory.getBean(classOf[TeamDao])
      teamDao.findAll.size == 5


      var tournament = new Tournament("lal", CompetitorType.TEAM, gst, CompetitionState.SIGNUP)

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
      numOfPlayers === 5

      competitionService.startTournament(tournamentPrim)
      val l= competitionService.getTournamentQualifierStructure(tournamentPrim)
      l === Qualifier(4,List(
        Qualifier(2,List(
          Qualifier(1,List(),Some(2))
        ),Some(4)),
        Qualifier(3,List(),Some(4))
      ),None)
      var layout = competitionService.createLayout2(tournamentPrim)
      layout must haveTheSameElementsAs(
        List(TwoGameQualifierPositionAndSize(Some("0"),Some("2"),1,0.0f,37.5f,100.0f,35.0f,QualifierState.Determined),
          TwoGameQualifierPositionAndSize(None,Some("4"),2,100.0f,55.0f,100.0f,52.5f,QualifierState.Undetermined),
          TwoGameQualifierPositionAndSize(Some("1"),Some("3"),3,100.0f,177.5f,100.0f,35.0f,QualifierState.Determined),
          TwoGameQualifierPositionAndSize(None,None,4,200.0f,90.0f,100.0f,105.0f,QualifierState.Undetermined))
      )


      gameReservationService.gameServerRepository = mock[GameServerRepositoryService]
      gameReservationService.gameServerRepository.findBy(any) returns Some(new GameProcessTemplate("","",Map(),null))


      doInTx {
        val tournamentBis = competitionDao.find(tournament.id).get.asInstanceOf[Tournament]
        val qualifier = findQualifier(tournament, List("4"))
        qualifier.gameOccassionOpt.get.participants.size === 1
        qualifier.gameOccassionOpt.get.participants(0).seqId === 1
      }



      val (comps, qualifier ) = doInTxWithRes {
        val qualifier = findQualifier(tournament, List("2", "0"))
        qualifier.gameOccassionOpt.get.participants.size === 2
        qualifier.gameOccassionOpt.get.participants(0).seqId === 0
        qualifier.gameOccassionOpt.get.participants(0).id.competitor.nameAccessor === "0"
        qualifier.gameOccassionOpt.get.participants(1).seqId === 1
        qualifier.gameOccassionOpt.get.participants(1).id.competitor.nameAccessor === "2"
        //qualifier.gameOccassionOpt.get.participants.map( p => p.id.competitor.id)
        (qualifier.gameOccassionOpt.get.participants.map(_.id.competitor),qualifier)
      }
        //gameReservationService.playScheduledClosed(qualifier.gameOccassion.id,)

      comps.size === 2

      gameReservationService.startPersistedGameServer(qualifier.gameOccassionOpt.get)

      var serializedResult: String = s"""{"@class":"se.bupp.cs3k.example.ExampleScoreScheme$$ExContestScore","s":{"${comps(0).id}":{"a":10,"b":1},"${comps(1).id}":{"a":3,"b":4}}}"""
      resultService.endGame(qualifier.gameOccassionOpt.get.gameSessionId,serializedResult)




      val (comps2, qualifier2 ) = doInTxWithRes {
        val qualifier = findQualifier(tournament, List("4", "0"))
        qualifier.gameOccassionOpt.get.participants.size === 2
        qualifier.gameOccassionOpt.get.participants(0).seqId === 1
        qualifier.gameOccassionOpt.get.participants(0).id.competitor.nameAccessor === "4"
        qualifier.gameOccassionOpt.get.participants(1).seqId === 0
        qualifier.gameOccassionOpt.get.participants(1).id.competitor.nameAccessor === "0"

        (qualifier.gameOccassionOpt.get.participants.map(_.id.competitor),qualifier)
      }

      comps2.size === 2

      gameReservationService.startPersistedGameServer(qualifier2.gameOccassionOpt.get)

      var serializedResult2: String = s"""{"@class":"se.bupp.cs3k.example.ExampleScoreScheme$$ExContestScore","s":{"${comps2(0).id}":{"a":10,"b":1},"${comps2(1).id}":{"a":3,"b":4}}}"""
      resultService.endGame(qualifier2.gameOccassionOpt.get.gameSessionId,serializedResult2)

      doInTx {

        val qualifier = findQualifier(tournament, List("4"))
        var participants = qualifier.gameOccassionOpt.get.participants.toList
        participants.size === 1
        participants(0).seqId === 0
        participants(0).id.competitor.nameAccessor === "4"
        //participants(1).seqId === 0
        //participants(1).id.competitor.nameAccessor === "2"
      }

      var layout2 = competitionService.createLayout2(tournamentPrim)
      layout2 must haveTheSameElementsAs(
        List(TwoGameQualifierPositionAndSize(Some("0"),Some("2"),1,0.0f,37.5f,100.0f,35.0f,QualifierState.Played),
          TwoGameQualifierPositionAndSize(Some("0"),Some("4"),2,100.0f,55.0f,100.0f,52.5f,QualifierState.Played),
          TwoGameQualifierPositionAndSize(Some("1"),Some("3"),3,100.0f,177.5f,100.0f,35.0f,QualifierState.Determined),
          TwoGameQualifierPositionAndSize(Some("4"),None,4,200.0f,90.0f,100.0f,105.0f,QualifierState.Undetermined))
      )

      val (comps3, qualifier3 ) = doInTxWithRes {
        val qualifier = findQualifier(tournament, List("1", "3"))
        qualifier.gameOccassionOpt.get.participants.size === 2
        qualifier.gameOccassionOpt.get.participants(0).seqId === 0
        qualifier.gameOccassionOpt.get.participants(0).id.competitor.nameAccessor === "1"
        qualifier.gameOccassionOpt.get.participants(1).seqId === 1
        qualifier.gameOccassionOpt.get.participants(1).id.competitor.nameAccessor === "3"

        (qualifier.gameOccassionOpt.get.participants.map(_.id.competitor),qualifier)
      }

      comps2.size === 2

      gameReservationService.startPersistedGameServer(qualifier3.gameOccassionOpt.get)

      var serializedResult3: String = s"""{"@class":"se.bupp.cs3k.example.ExampleScoreScheme$$ExContestScore","s":{"${comps3(0).id}":{"a":10,"b":1},"${comps3(1).id}":{"a":3,"b":4}}}"""
      resultService.endGame(qualifier3.gameOccassionOpt.get.gameSessionId,serializedResult3)

      var layout3 = competitionService.createLayout2(tournamentPrim)
      layout3 must haveTheSameElementsAs(
        List(TwoGameQualifierPositionAndSize(Some("0"),Some("2"),1,0.0f,37.5f,100.0f,35.0f,QualifierState.Played),
          TwoGameQualifierPositionAndSize(Some("0"),Some("4"),2,100.0f,55.0f,100.0f,52.5f,QualifierState.Played),
          TwoGameQualifierPositionAndSize(Some("1"),Some("3"),3,100.0f,177.5f,100.0f,35.0f,QualifierState.Played),
          TwoGameQualifierPositionAndSize(Some("4"),Some("1"),4,200.0f,90.0f,100.0f,105.0f,QualifierState.Determined))
      )


      val (comps4, qualifier4 ) = doInTxWithRes {
        val qualifier = findQualifier(tournament, List("4", "1"))
        qualifier.gameOccassionOpt.get.participants.size === 2
        qualifier.gameOccassionOpt.get.participants(0).seqId === 0
        qualifier.gameOccassionOpt.get.participants(0).id.competitor.nameAccessor === "4"
        qualifier.gameOccassionOpt.get.participants(1).seqId === 1
        qualifier.gameOccassionOpt.get.participants(1).id.competitor.nameAccessor === "1"

        (qualifier.gameOccassionOpt.get.participants.map(_.id.competitor),qualifier)
      }

      comps4.size === 2

      gameReservationService.startPersistedGameServer(qualifier4.gameOccassionOpt.get)

      var serializedResult4: String = s"""{"@class":"se.bupp.cs3k.example.ExampleScoreScheme$$ExContestScore","s":{"${comps4(1).id}":{"a":10,"b":1},"${comps4(0).id}":{"a":3,"b":4}}}"""
      resultService.endGame(qualifier4.gameOccassionOpt.get.gameSessionId,serializedResult4)

      var layout4 = competitionService.createLayout2(tournamentPrim)
      layout4 must haveTheSameElementsAs(
        List(TwoGameQualifierPositionAndSize(Some("0"),Some("2"),1,0.0f,37.5f,100.0f,35.0f,QualifierState.Played),
          TwoGameQualifierPositionAndSize(Some("0"),Some("4"),2,100.0f,55.0f,100.0f,52.5f,QualifierState.Played),
          TwoGameQualifierPositionAndSize(Some("1"),Some("3"),3,100.0f,177.5f,100.0f,35.0f,QualifierState.Played),
          TwoGameQualifierPositionAndSize(Some("4"),Some("1"),4,200.0f,90.0f,100.0f,105.0f,QualifierState.Played))
      )

      /*val sessionId = service.allocateGameSession()
      val t1 = service.createVirtualTeam(Some("Ena"))
      service.addTeamToSession(sessionId,t1)
      var t2 = service.createVirtualTeam(Some("Tjing"))
      service.addTeamToSession(sessionId,t2)*/



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