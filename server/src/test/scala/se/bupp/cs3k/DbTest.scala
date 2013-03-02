package se.bupp.cs3k

import model.{CompetitorType, CompetitionState}
import org.specs2.mutable.Specification
import com.fasterxml.jackson.databind.ObjectMapper

import se.bupp.cs3k.server.service.GameReservationService._
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.support.{FileSystemXmlApplicationContext, ClassPathXmlApplicationContext}
import server.model._
import server.model.GameParticipationPk
import server.model.GameParticipationPk
import server.model.Ladder
import server.model.Ladder
import server.model.Ladder
import server.model.CompetitionParticipantPk
import server.model.CompetitionParticipantPk
import server.model.CompetitionParticipantPk
import server.model.User
import server.model.User
import server.model.User
import server.service.dao._
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.support.{TransactionCallbackWithoutResult, TransactionTemplate, DefaultTransactionDefinition}
import org.springframework.transaction.{PlatformTransactionManager, TransactionStatus}
import javax.persistence.{EntityManagerFactory, TypedQuery}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import se.bupp.cs3k.server.model.Model._
import scala.Some
import scala.Some
import server.service.LadderService

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class DbTest extends Specification {


  def inTx[G](txMgr:PlatformTransactionManager)(body : => G) = {
    new TransactionTemplate(txMgr).execute(new TransactionCallbackWithoutResult {
      def doInTransactionWithoutResult(p1: TransactionStatus) {
        body
      }
      })
  }

  def withTx[T](body:(PlatformTransactionManager, BeanFactory) => T) : T = {
    val appContext = new FileSystemXmlApplicationContext("server/src/test/resources/applicationContext.xml");
    val factory =  appContext.asInstanceOf[BeanFactory];

    //val emf = factory.getBean(classOf[EntityManagerFactory])
    //var em = emf.createEntityManager()
    var txMgr = factory.getBean("transactionManager", classOf[JpaTransactionManager])
    //val tdef = new DefaultTransactionDefinition();
    var tx = null//txMgr.getTransaction(tdef)

    val res = body(txMgr, factory)
    //em.close()
    //emf.close()
    appContext.close()
    res
  }

  "db" should {
    "handle user insert" in {

      withTx {
        case (txMgr, factory) => {
          var userDao = factory.getBean("userDao").asInstanceOf[UserDao]

          var user = new User("tjena")
          inTx(txMgr) {
              userDao.insert(user)
            }



          user.shouldNotEqual(null)
          user.id must not be(null)
          var user2Opt: Option[User] = userDao.find(user.id)
          user.shouldEqual(user2Opt.get)
        }
      }
      1.shouldEqual(1)
    }

    "handle ladder enrollment" in {

      withTx {
        case (txMgr, factory) => {
          val compDao = factory.getBean(classOf[CompetitorDao])
          val ladderService= factory.getBean(classOf[LadderService])


          var comp = new Competitor()

          inTx(txMgr) {
            compDao.insert(comp)
          }
          //txMgr.commit(tx)

          comp.shouldNotEqual(null)
          comp.id must not be(null)



          val gs = new GameType('super_mario2, "Super Mario")
          val gst = new GameSetupType(Symbol("2vs2"), "Versus Mode",
            "se.bupp.cs3k.example.ExampleScoreScheme.ExContestScore",
            "se.bupp.cs3k.example.ExampleScoreScheme.ExScoreScheme")
          gst.gameType = gs
          inTx(txMgr) {
            compDao.em.persist(gs)
            compDao.em.persist(gst)
          }

          val ladder = new Ladder("ladder1",CompetitorType.TEAM,gst,CompetitionState.SIGNUP)

          inTx(txMgr) {
            compDao.em.persist(ladder)
          }
          //var tx2: TransactionStatus = txMgr.getTransaction(new DefaultTransactionDefinition())
          //txMgr.commit(tx2)

          ladder.id must not be(null)


          val pk: CompetitionParticipantPk = new CompetitionParticipantPk()
          pk.competition = ladder
          pk.competitor = comp

          val enrollment: CompetitionParticipant = new CompetitionParticipant()
          enrollment.id = pk
          inTx(txMgr) {
            compDao.em.persist(enrollment)
          }
          //txMgr.commit(txMgr.getTransaction(new DefaultTransactionDefinition()))
          enrollment.id must not be(null)

          var q: TypedQuery[CompetitionParticipant] = compDao.em.createQuery("select le from CompetitionParticipant le where le.id.competition = :l and le.id.competitor = :c", classOf[CompetitionParticipant])
          q.setParameter("l", ladder)
          q.setParameter("c", comp)
          q.getSingleResult must not be(null)



          var readLadder = compDao.em.find(classOf[Ladder], ladder.id)
          readLadder.state === CompetitionState.SIGNUP




        }
      }
      2.shouldEqual(2)
    }
    "handle game occassion transistion to game result" in {

      withTx {
        case (txMgr, factory) => {
          var gameOccasionDao = factory.getBean(classOf[GameOccassionDao])
          var userDao = factory.getBean(classOf[UserDao])
          var gameSetupDao = factory.getBean(classOf[GameSetupTypeDao])

          val gs = new GameType('super_mario3, "Super Mario")
          val gst = new GameSetupType(Symbol("1vs1"), "Versus Mode",
            "se.bupp.cs3k.example.ExampleScoreScheme.ExContestScore",
            "se.bupp.cs3k.example.ExampleScoreScheme.ExScoreScheme")
          gst.gameType = gs
          inTx(txMgr) {
            userDao.em.persist(gs)
            userDao.em.persist(gst)
          }

          var gameType = gameSetupDao.findGameType('super_mario)
          gameType must beAnInstanceOf[Some[GameType]]

          var gameSetupType = gameSetupDao.findGameSetupType('super_mario, Symbol("1vs1"))
          gameSetupType must beAnInstanceOf[Some[GameSetupType]]


          val user = new User("leffe")
          val gameOcc = new GameOccassion(12, "individual")
          gameOcc.game = gst



          val gppk = new GameParticipationPk(user,gameOcc)
          val gp = new GameParticipation(gppk)
          gameOcc.participants.add(gp)
          inTx(txMgr) {
            userDao.insert(user)
            gameOccasionDao.insert(gameOcc)
            //gameOccasionDao.insert(gp)
          }

          user.id !== null
          gameOcc.id !== null

          val queriedGameOcc = gameOccasionDao.find(gameOcc.id)
          queriedGameOcc !== None
          queriedGameOcc.get.id !== null

          val gr = new GameResult(1, "asdfasdf")
          queriedGameOcc.get.result = gr
          gr.game = queriedGameOcc.get

          inTx(txMgr) {
            userDao.em.merge(queriedGameOcc.get)

            //gameOccasionDao.insert(gp)
          }
          val queriedGameOcc2 = gameOccasionDao.find(gameOcc.id)

          queriedGameOcc2.get.result !== null


        }
      }
      2.shouldEqual(2)
    }

    "handle torunaments" in {

      withTx {
        case (txMgr, factory) => {
          var tournamentDao = factory.getBean(classOf[TournamentDao])
          var gameSetupDao = factory.getBean(classOf[GameSetupTypeDao])

          val gs = new GameType('super_mario, "Super Mario")
          val gst = new GameSetupType(Symbol("1vs1"), "Versus Mode",
            "se.bupp.cs3k.example.ExampleScoreScheme.ExContestScore",
            "se.bupp.cs3k.example.ExampleScoreScheme.ExScoreScheme")
          gst.gameType = gs
          inTx(txMgr) {
            gameSetupDao.em.persist(gs)
            gameSetupDao.em.persist(gst)
          }

          val tournament = new Tournament("ladder1",CompetitorType.TEAM,gst,CompetitionState.SIGNUP)

          import scala.collection.JavaConversions.seqAsJavaList
          import scala.collection.JavaConversions.asScalaBuffer
          val q1 = new TournamentStageQualifier(1,tournament,List(2,3))
          val q2 = new TournamentStageQualifier(2,tournament,List())
          val q3 = new TournamentStageQualifier(3,tournament,List())

          tournament.structure = List(q1,q2,q3)

          inTx(txMgr) {

            tournamentDao.em.persist(tournament)
            tournament.structure.foreach( q => {
              q.tournament = tournament
              tournamentDao.em.persist(q)
            })

            //gameOccasionDao.insert(gp)
          }

          var t = tournamentDao.find(tournament.id).get


          inTx(txMgr) {

            val t2 = tournamentDao.em.merge(t)
            t2.structure.size === 3
            val(parent, children) = t2.structure.partition( _.id == 1 )

            parent.head.childNodeIds must haveTheSameElementsAs(List(2,3))

            children.size === 2
            children.forall(_.childNodeIds.size === 0)
          }
        }
      }
      2.shouldEqual(2)
    }

    "handle ladders" in {

    }
  }
}
