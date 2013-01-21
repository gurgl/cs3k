package se.bupp.cs3k

import org.specs2.mutable.Specification
import com.fasterxml.jackson.databind.ObjectMapper

import se.bupp.cs3k.server.service.GameReservationService._
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.support.{FileSystemXmlApplicationContext, ClassPathXmlApplicationContext}
import server.model._
import server.model.Ladder
import server.model.LadderEnrollmentPk
import server.model.User
import server.service.dao.{GameDao, CompetitorDao, UserDao}
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.support.{TransactionCallbackWithoutResult, TransactionTemplate, DefaultTransactionDefinition}
import org.springframework.transaction.{PlatformTransactionManager, TransactionStatus}
import javax.persistence.TypedQuery
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

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
    var txMgr = factory.getBean("transactionManager", classOf[JpaTransactionManager])
    //val tdef = new DefaultTransactionDefinition();
    var tx = null//txMgr.getTransaction(tdef)

    val res = body(txMgr, factory)
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
          var compDao = factory.getBean("competitorDao").asInstanceOf[CompetitorDao]

          var comp = new Competitor()

          inTx(txMgr) {
            compDao.insert(comp)
          }
          //txMgr.commit(tx)

          comp.shouldNotEqual(null)
          comp.id must not be(null)

          val ladder = new Ladder()
          ladder.name = "ladder1"
          inTx(txMgr) {
            compDao.em.persist(ladder)
          }
          //var tx2: TransactionStatus = txMgr.getTransaction(new DefaultTransactionDefinition())
          //txMgr.commit(tx2)

          ladder.id must not be(null)


          var pk: LadderEnrollmentPk = new LadderEnrollmentPk()
          pk.ladder = ladder
          pk.competitor = comp

          var enrollment: LadderEnrollment = new LadderEnrollment()
          enrollment.id = pk
          inTx(txMgr) {
            compDao.em.persist(enrollment)
          }
          //txMgr.commit(txMgr.getTransaction(new DefaultTransactionDefinition()))
          enrollment.id must not be(null)

          var q: TypedQuery[LadderEnrollment] = compDao.em.createQuery("select le from LadderEnrollment le where le.id.ladder = :l and le.id.competitor = :c", classOf[LadderEnrollment])
          q.setParameter("l", ladder)
          q.setParameter("c", comp)
          q.getSingleResult must not be(null)

          1.shouldEqual(1)
        }
      }
      2.shouldEqual(2)
    }
    "handle game occassion transistion to game result" in {

      withTx {
        case (txMgr, factory) => {
          var gameOccasionDao = factory.getBean("gameDao").asInstanceOf[GameDao]
          var userDao = factory.getBean("userDao").asInstanceOf[UserDao]



          val user = new User("leffe")
          val gameOcc = new GameOccassion(12)

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
  }
}
