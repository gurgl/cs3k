package se.bupp.cs3k.server.service.dao

import javax.persistence._
import se.bupp.cs3k.server.model._
import org.springframework.stereotype.Repository
import se.bupp.cs3k.server.service.GameReservationService._
import se.bupp.cs3k.server.model.User
import org.springframework.transaction.annotation.Transactional
import javax.persistence.criteria.{CriteriaBuilder, Root, CriteriaQuery}

import se.bupp.cs3k.server.model.Ladder
import se.bupp.cs3k.server.model.GameOccassion
import org.slf4j.{LoggerFactory, Logger}

import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.server.model.Ladder
import se.bupp.cs3k.server.model.GameOccassion

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


  def getSingle[T](q:TypedQuery[T]) : Option[T] = {
    import scala.collection.JavaConversions.asScalaBuffer
    q.getResultList.headOption
  }

  import scala.collection.JavaConversions.asScalaBuffer
  def findAll[T] : List[T] = em.createQuery("select p from " + clz.getSimpleName+ " p").getResultList.toList.map(_.asInstanceOf[T])
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

  import scala.collection.JavaConversions.asScalaBuffer

  def selectRange(off:Int,max:Int) : List[T] = {

    val resultList = selectRange().setMaxResults(max).setFirstResult(off).getResultList();
    resultList.toList

  }
}



@Repository
class GameResultDao extends GenericDaoImpl[GameResult](classOf[GameResult]) {

}





@Repository
class GameParticipationDao extends GenericDaoImpl[GameParticipation](classOf[GameParticipation]) {

}




@Repository
class GameDao extends GenericDaoImpl[GameOccassion](classOf[GameOccassion]) {

  def findGame(gameSessionId:Long) = {
    var q = em.createQuery("from GameOccassion g where g.gameSessionId = :o", classOf[GameOccassion])
    q.setParameter("o", gameSessionId)
    import scala.collection.JavaConversions.asScalaBuffer
    log.info("gameSessionId" + gameSessionId +  " " + q.getResultList.toList)
    getSingle(q)
  }
}


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
}


//@NamedQueries({(
//  )}
//)

@Repository
class CompetitorDao extends GenericDaoImpl[Competitor](classOf[Competitor]) {
  import scala.collection.JavaConversions.asScalaBuffer
  def findByUser(u:User)  =  {

    val q = em.createNamedQuery("Competitor.findByUser")
    q.setParameter("user1",u)
    q.setParameter("user2",u)
    q.getResultList.toList.map(_.asInstanceOf[Competitor])
  }


  def findLadderParticipants(u:Ladder) : Query  =  {

    val q = em.createNamedQuery("Competitor.findLadderParticipants")
    q.setParameter("ladder",u)
    q
  }
  def findLadderParticipantsCount(u:Ladder) = {
    findLadderParticipants(u).getResultList.size()
  }

  def findLadderParticipants(u:Ladder,p1: Long, p2: Long) : List[Competitor]=  {
    findLadderParticipants(u).setMaxResults(p2.toInt).setFirstResult(p1.toInt).getResultList.toList.map(_.asInstanceOf[Competitor])
  }


}

@Repository
@Transactional
class LadderDao extends GenericDaoImpl[Ladder](classOf[Ladder]) {


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

}
