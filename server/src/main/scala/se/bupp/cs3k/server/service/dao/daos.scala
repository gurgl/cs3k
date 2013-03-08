package se.bupp.cs3k.server.service.dao

import javax.persistence._
import se.bupp.cs3k.server.model._
import org.springframework.stereotype.Repository
import se.bupp.cs3k.server.service.GameReservationService._
import se.bupp.cs3k.server.model.User
import org.springframework.transaction.annotation.Transactional
import criteria._

import se.bupp.cs3k.server.model.Ladder
import se.bupp.cs3k.server.model.GameOccassion
import org.slf4j.{LoggerFactory, Logger}

import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.server.model.Ladder
import se.bupp.cs3k.server.model.GameOccassion
import se.bupp.cs3k.server.model.Model._
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.server.model.Ladder
import se.bupp.cs3k.model.CompetitionState
import scala.Option
import se.bupp.cs3k.server.model.User
import org.hibernate.ejb.criteria.predicate.CompoundPredicate
import se.bupp.cs3k.server.model.User

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-04
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */

@Transactional
class GenericDaoImpl[T](clz:Class[T]) {

  var log = LoggerFactory.getLogger(clz)

  @PersistenceContext(unitName="MyPersistenceUnit")
  var em:EntityManager = _

  def insert(o:T) = {
    em.persist(o)
    o
  }

  def find[I](i:AnyRef) = Option(em.find(clz,i))
  def delete(o:T) = em.remove(o)
  def update(o:T) = em.merge(o)


  def getSingle(q:TypedQuery[T]) : Option[T] = {
    import scala.collection.JavaConversions.asScalaBuffer
    q.getResultList.headOption
  }

  import scala.collection.JavaConversions.asScalaBuffer

  def findAll : List[T] = em.createQuery("select p from " + clz.getSimpleName+ " p").getResultList.toList.map(_.asInstanceOf[T])
  def count = em.createQuery("select count(p) from " + clz.getSimpleName+ " p").getSingleResult.asInstanceOf[Long]

  def selectRangeCount = {
    selectRange().getResultList.size
  }
  def selectRange() = {
    val (select , from , builder )= selectCriteria
    val typedQuery:TypedQuery[T] = em.createQuery(select);
    typedQuery
  }

  def selectCriteria: (CriteriaQuery[T], Root[T], CriteriaBuilder) = {
    val criteriaBuilder: CriteriaBuilder = em.getCriteriaBuilder();
    val criteriaQuery: CriteriaQuery[T] = criteriaBuilder.createQuery[T](clz);
    val from: Root[T] = criteriaQuery.from(clz);
    val select: CriteriaQuery[T] = criteriaQuery.select(from);
    (select, from, criteriaBuilder)
  }


  def selectRange(off:Int,max:Int) : List[T] = {

    val resultList = selectRange().setMaxResults(max).setFirstResult(off).getResultList();
    resultList.toList
  }
  import scala.collection.JavaConversions.asScalaBuffer


  def selectRangeCount2(t:TypedQuery[T]) = {
    t.getResultList.size
  }


  def selectRange2Base(bodyOpt:Option[(CriteriaBuilder,Root[T])=> Expression[java.lang.Boolean]])  = {
    val (select , from , builder , critQry) = selectCriteria2
    bodyOpt.foreach { body =>
      val condition:Expression[java.lang.Boolean] = body(builder,from)
      critQry.where(condition)
    }
    val typedQuery:TypedQuery[T] = em.createQuery(select);
    typedQuery
  }

  def selectCriteria2: (CriteriaQuery[T], Root[T], CriteriaBuilder, CriteriaQuery[T]) = {
    val criteriaBuilder: CriteriaBuilder = em.getCriteriaBuilder();
    val criteriaQuery: CriteriaQuery[T] = criteriaBuilder.createQuery[T](clz);
    val from: Root[T] = criteriaQuery.from(clz);
    val select: CriteriaQuery[T] = criteriaQuery.select(from);


    (select, from, criteriaBuilder, criteriaQuery)
  }


  def selectRange2(tq:TypedQuery[T],off:Int,max:Int) : List[T] = {

    val resultList = tq.setMaxResults(max).setFirstResult(off).getResultList();
    resultList.toList
  }

}


@Repository
class CompetitionDao extends GenericDaoImpl[Competition](classOf[Competition]) {

  private def stateCrit(m:Option[CompetitionState]) = {
    m.map( mo => { (b:CriteriaBuilder,r:Root[Competition]) => b.equal(r.get("state"),mo) })
  }

  def withStateCnt(m:Option[CompetitionState]) = {
    selectRangeCount2(selectRange2Base(stateCrit(m)))
  }
  def withState(m:Option[CompetitionState], off: Int, max: Int) = {
    selectRange2(selectRange2Base(stateCrit(m)),off,max)
  }


}

@Repository
class GameResultDao extends GenericDaoImpl[GameResult](classOf[GameResult]) {
   def findByCompetition(u:Competition) : List[GameResult] = {

     var q: TypedQuery[GameResult] = em.createNamedQuery[GameResult]("Competitor.findResultsByCompetition", classOf[GameResult])
     q.setParameter("comp1",u.id)
     q.setParameter("comp2",u.id)
     import scala.collection.JavaConversions.asScalaBuffer
     q.getResultList.toList.map(_.asInstanceOf[GameResult])
   }
}

@Repository
class GameParticipationDao extends GenericDaoImpl[GameParticipation](classOf[GameParticipation]) {

}



@Repository
class GameSetupTypeDao extends GenericDaoImpl[GameSetupType](classOf[GameSetupType]) {
  def findGameSetupType(gtId:GameServerTypeId, gstId:GameProcessTemplateId) = {
    var q = em.createQuery("select gst from GameSetupType gst join gst.gameType gt where gt.id= :gtId and gst.setupId = :gstId", classOf[GameSetupType])
    q.setParameter("gtId", gtId)
    q.setParameter("gstId", gstId)
    import scala.collection.JavaConversions.asScalaBuffer
    log.info("gtId " +gtId  +  ", gstId " + gstId+ " " + q.getResultList.toList)
    getSingle(q)
  }

  def findGameType(gtId:GameServerTypeId) = {

    Option(em.find(classOf[GameType],gtId))
  }

}

@Repository
class TournamentDao extends GenericDaoImpl[Tournament](classOf[Tournament]) {

}

@Repository
class GameOccassionDao extends GenericDaoImpl[GameOccassion](classOf[GameOccassion]) {

  def findGame(gameSessionId:Long) = {
    var q = em.createQuery("from GameOccassion g where g.gameSessionId = :o", classOf[GameOccassion])
    q.setParameter("o", gameSessionId)
    import scala.collection.JavaConversions.asScalaBuffer
    log.info("gameSessionId" + gameSessionId +  " " + q.getResultList.toList)
    getSingle(q)
  }
}

/*
@Repository
class TicketDao extends GenericDaoImpl[Ticket](classOf[Ticket]) {

  def findTicket(id:Long) : Option[Ticket] = {
    Option(em.find(classOf[Ticket], id))
  }

  def findTicketByUserAndGame(id:Long,occId:GameSessionId) : Option[Ticket] = {
    var q = em.createQuery("from Ticket t where t.user.id = :u and t.game.gameSessionId = :o", classOf[Ticket])
    q.setParameter("u", id)
    q.setParameter("o", occId)
    getSingle(q)
  }
}*/


//@NamedQueries({(
//  )}
//)

@Repository
class CompetitorDao extends GenericDaoImpl[Competitor](classOf[Competitor]) {
  import scala.collection.JavaConversions.asScalaBuffer

  def findPlayerTeams(id:Long) = {
      var q: TypedQuery[Team] = em.createNamedQuery[Team]("User.findUserTeams", classOf[Team])
      q.setParameter("userId",id)
      q.getResultList.toList.map(_.asInstanceOf[Team])

    }

  def findByUser(u:User)  =  {

    val q = em.createNamedQuery("Competitor.findByUser")
    q.setParameter("user1",u)
    q.setParameter("user2",u)
    q.getResultList.toList.map(_.asInstanceOf[Competitor])
  }


  def findLadderParticipants(u:Competition) : Query  =  {

    val q = em.createNamedQuery("Competitor.findCompetitionParticipants")
    q.setParameter("competition",u)
    q
  }
  def findCompetitionParticipantsCount(u:Competition) = {
    findLadderParticipants(u).getResultList.size()
  }

  def findCompetitionParticipants(u:Competition,p1: Long, p2: Long) : List[Competitor]=  {
    findLadderParticipants(u).setMaxResults(p2.toInt).setFirstResult(p1.toInt).getResultList.toList.map(_.asInstanceOf[Competitor])
  }

  def findUserGames(u:User) : List[GameOccassion] =  {

    val q = em.createNamedQuery("Competitor.findGamesByUser")
    q.setParameter("user1",u)
    q.setParameter("user2",u)
    //q.setParameter("state",CompetitionState.RUNNING)
    q.getResultList.toList.map(_.asInstanceOf[GameOccassion])
  }

  def findResultsByUser(u:User) : List[GameResult] =  {

    val q = em.createNamedQuery("Competitor.findResultsByUser")
    q.setParameter("user1",u)
    q.setParameter("user2",u)
    //q.setParameter("state",CompetitionState.RUNNING)
    q.getResultList.toList.map(_.asInstanceOf[GameResult])
  }
}
import scala.collection.JavaConversions.asScalaBuffer

@Repository
@Transactional
class LadderDao extends GenericDaoImpl[Ladder](classOf[Ladder]) {


  def findLadderResults(u:Ladder) : List[(Competitor, GameResult)] =  {

    val q = em.createNamedQuery("Competitor.findLadderResults")
    q.setParameter("ladder",u)
    q.getResultList.toList.toList.map(_.asInstanceOf[(Competitor, GameResult)])
  }
}

@Repository
@Transactional
class TeamDao extends GenericDaoImpl[Team](classOf[Team]) {

}

@Repository
@Transactional
class UserDao extends GenericDaoImpl[User](classOf[User]) {

  def findUser(id:Long) : Option[User] = {
    Option(em.find(classOf[User], id))
  }

  def findCompetitor(id:Long) : Option[Competitor] = {
    Option(em.find(classOf[Competitor], id))
  }

  def findUser(s:String) = {
    var q: TypedQuery[User] = em.createQuery[User]("from User p where p.username = :name",classOf[User])
    q.setParameter("name",s)
    getSingle(q).getOrElse(null)
  }

  override def delete(o: User) {
    throw new RuntimeException("Disallowed method for UserDao")
  }
}
