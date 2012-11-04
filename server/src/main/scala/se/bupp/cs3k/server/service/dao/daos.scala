package se.bupp.cs3k.server.service.dao

import javax.persistence.{TypedQuery, EntityManager, PersistenceContext}
import se.bupp.cs3k.server.model.{Competitor, GameOccassion, Ticket}
import org.springframework.stereotype.Repository
import se.bupp.cs3k.server.service.GameReservationService._
import se.bupp.cs3k.server.User

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-04
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */


class GenericDaoImpl[T](clz:Class[T]) {

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

}







@Repository
class GameDao extends GenericDaoImpl[GameOccassion](classOf[GameOccassion]) {

  def findGame(occassionId:Long) = {
    var q = em.createQuery("from GameOccassion g where g.occassionId = :o", classOf[GameOccassion])
    q.setParameter("o", occassionId)
    getSingle(q)
  }
}

@Repository
class TicketDao extends GenericDaoImpl[Ticket](classOf[Ticket]) {

  def findTicket(id:Long) : Option[Ticket] = {
    Option(em.find(classOf[Ticket], id))
  }

  def findTicketByUserAndGame(id:Long,occId:OccassionId) : Option[Ticket] = {
    var q = em.createQuery("from Ticket t where t.user.id = :u and t.game.occassionId = :o", classOf[Ticket])
    q.setParameter("u", id)
    q.setParameter("o", occId)
    getSingle(q)
  }
}

@Repository
class CompetitorDao extends GenericDaoImpl[Competitor](classOf[Competitor]) {

}

  @Repository
class UserDao extends GenericDaoImpl[User](classOf[User]) {

  def findUser(id:Long) : Option[User] = {
    Option(em.find(classOf[User], id))
  }

  def findUser(s:String) = {
    var q: TypedQuery[User] = em.createQuery[User]("from User p where p.username = :name",classOf[User])
    q.setParameter("name",s)
    getSingle(q).getOrElse(null)
  }

}
