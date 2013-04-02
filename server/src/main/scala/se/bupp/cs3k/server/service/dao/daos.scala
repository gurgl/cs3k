package se.bupp.cs3k.server.service.dao

import javax.persistence._
import se.bupp.cs3k.server.model._
import org.springframework.stereotype.Repository
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
import org.joda.time.Interval


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-04
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */


import scala.collection.JavaConversions.asScalaBuffer

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

  private def findUserCompetitionsQ(u:User) : Query  =  {

    val q = em.createNamedQuery("Competition.findUserCompetitions")
    q.setParameter("user1",u)
    q.setParameter("user2",u)
    q
  }
  def findUserCompetitionsCount(u:User) = {
    findUserCompetitionsQ(u).getResultList.size()
  }

  def findUserCompetitions(u:User,p1: Long, p2: Long) : List[Competition]=  {
    findUserCompetitionsQ(u).setMaxResults(p2.toInt).setFirstResult(p1.toInt).getResultList.toList.map(_.asInstanceOf[Competition])
  }




}



@Repository
class GameResultDao extends GenericDaoImpl[GameResult](classOf[GameResult]) {
   def findByCompetition(u:Competition) : List[GameResult] = {

     var q: TypedQuery[GameResult] = em.createNamedQuery[GameResult]("Competitor.findResultsByCompetition", classOf[GameResult])
     q.setParameter("comp1",u.id)
     q.setParameter("comp2",u.id)

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
    val q = em.createQuery("from GameOccassion g where g.gameSessionId = :o", classOf[GameOccassion])
    q.setParameter("o", gameSessionId)
    import scala.collection.JavaConversions.asScalaBuffer
    log.info("gameSessionId" + gameSessionId +  " " + q.getResultList.toList)
    getSingle(q)
  }
  def findMaxSessionId() =  {

    val q = em.createNamedQuery("GameOccassion.findMaxSessionId", classOf[java.lang.Long])
    q.getSingleResult
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
  def findResultsByTeam(team: Team) : List[GameResult] =  {

      val q = em.createNamedQuery("Competitor.findResultsByTeam")
      q.setParameter("team",team)
      //q.setParameter("state",CompetitionState.RUNNING)
      q.getResultList.toList.map(_.asInstanceOf[GameResult])
  }



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
class TeamMemberDao extends GenericDaoImpl[TeamMember](classOf[TeamMember]) {

}
/*
new NamedQuery(name = "NewsItem.findByCompetition", query = "select n from NewsItem n where n.competition = :competition and n.dateTime between :startDate and :endDate"),
new NamedQuery(name = "NewsItem.findByTeam", query = "select n from NewsItem n where n.competitor1 = :team and n.dateTime between :startDate and :endDate"),
new NamedQuery(name = "NewsItem.findAll", query = "select n from NewsItem n where n.dateTime between :startDate and :endDate"),
new NamedQuery(name = "NewsItem.findByUser", query = "select un from UserNewsItem un inner join un.newsItem n where un = :user and n.dateTime between :startDate and :endDate")
*/

@Repository
@Transactional
class UserNewsItemDao extends GenericDaoImpl[UserNewsItem](classOf[UserNewsItem]) {
  /*def select(u:User, i:Interval) = {
    val (select, from, criteriaBuilder, criteriaQuery) = selectCriteria2
    val pred: Predicate = criteriaBuilder.and(
      criteriaBuilder.equal(from.get("user"), u),
      criteriaBuilder.between(from.get("dateTime"), i.getStart, i.getEnd))
    criteriaQuery.where(pred)
    criteriaQuery.select(criteriaBuilder.count(from));
    em.createQuery(select)
  }*/
  def markAllAsRead(u:User) {
    val q = em.createQuery("update UserNewsItem un set un.seen = true where un.user = :user")
    q.setParameter("user",u)
    q.executeUpdate()
  }
}

@Repository
@Transactional
class NewsItemDao extends GenericDaoImpl[NewsItem](classOf[NewsItem]) {


  def findByCompetition(c:Competition, i:Interval,range:Range) = {
    val q = em.createNamedQuery("NewsItem.findByCompetition")
    q.setParameter("competition",c)
    q.setParameter("startDate",i.getStart.toInstant)
    q.setParameter("endDate",i.getEnd.toInstant)
    q.setMaxResults(range.length).setFirstResult(range.start).getResultList.toList.map(_.asInstanceOf[NewsItem])
  }
  def findByTeam(c:Team, i:Interval,range:Range) = {
    val q = em.createNamedQuery("NewsItem.findByTeam")
    q.setParameter("team",c)
    q.setParameter("startDate",i.getStart.toInstant)
    q.setParameter("endDate",i.getEnd.toInstant)
    q.setMaxResults(range.length).setFirstResult(range.start).getResultList.toList.map(_.asInstanceOf[NewsItem])
  }

  def findAll(i:Interval,range:Range) = {
    val q = em.createNamedQuery("NewsItem.findAll")
    q.setParameter("startDate",i.getStart.toInstant)
    q.setParameter("endDate",i.getEnd.toInstant)
    q.setMaxResults(range.length).setFirstResult(range.start).getResultList.toList.map(_.asInstanceOf[NewsItem])
  }

  def findByUser(u:User, i:Interval,range:Range) = {
    val q = em.createNamedQuery("NewsItem.findByUser",classOf[UserNewsItem])
    q.setParameter("user",u)
    q.setParameter("startDate",i.getStart.toInstant)
    q.setParameter("endDate",i.getEnd.toInstant)
    q.setMaxResults(range.length).setFirstResult(range.start).getResultList.toList.map(_.asInstanceOf[UserNewsItem])
  }

  def findByCompetitionCount(c:Competition, i:Interval) = {
    val q = em.createNamedQuery("NewsItem.findByCompetition.count",classOf[java.lang.Long])
    q.setParameter("competition",c)
    q.setParameter("startDate",i.getStart.toInstant)
    q.setParameter("endDate",i.getEnd.toInstant)
    q.getSingleResult
  }
  def findByTeamCount(c:Team, i:Interval) = {
    val q = em.createNamedQuery("NewsItem.findByTeam.count",classOf[java.lang.Long])
    q.setParameter("team",c)
    q.setParameter("startDate",i.getStart.toInstant)
    q.setParameter("endDate",i.getEnd.toInstant)
    q.getSingleResult
  }

  def findAllCount(i:Interval) = {
    val q = em.createNamedQuery("NewsItem.findAll.count",classOf[java.lang.Long])
    q.setParameter("startDate",i.getStart.toInstant)
    q.setParameter("endDate",i.getEnd.toInstant)
    q.getSingleResult
  }

  def findByUserCount(u:User, i:Interval) = {
    val q = em.createNamedQuery("NewsItem.findByUser.count",classOf[java.lang.Long])
    q.setParameter("user",u)
    q.setParameter("startDate",i.getStart.toInstant)
    q.setParameter("endDate",i.getEnd.toInstant)
    q.getSingleResult
  }
  def findUnreadByUserCount(u:User, i:Interval) = {
    val q = em.createNamedQuery("NewsItem.findUnreadByUser.count",classOf[java.lang.Long])
    q.setParameter("user",u)
    q.setParameter("startDate",i.getStart.toInstant)
    q.setParameter("endDate",i.getEnd.toInstant)
    q.getSingleResult
  }

/*
def byUser[X](u:User, i:Interval)(q:TypedQuery[X]) = {
  q.setParameter("user",u)
  q.setParameter("startDate",i.getStart.toInstant)
  q.setParameter("endDate",i.getEnd.toInstant)
}

def sfindByUser(s:byUser.type) = {

}
def sfindByUserCnt() = {

}

def findByUserCount() = {
  val q = em.createNamedQuery("NewsItem.findByUser.count",classOf[java.lang.Long])

  q.getSingleResult
} */

}


@Repository
@Transactional
class TeamDao extends GenericDaoImpl[Team](classOf[Team]) {

private def findUserTeamMemberships(u:User) : Query  =  {

  val q = em.createNamedQuery("Team.findUserTeamMemberships")
  q.setParameter("user",u)
  q
}

def findUserTeamMembershipsCount(u:User) = {
  findUserTeamMemberships(u).getResultList.size()
}

def findUserTeamMemberships(u:User,p1: Long, p2: Long) : List[TeamMember]=  {
  findUserTeamMemberships(u).setMaxResults(p2.toInt).setFirstResult(p1.toInt).getResultList.toList.map(_.asInstanceOf[TeamMember])
}

private def findTeamMemberships(u:Team) : Query  =  {

  val q = em.createNamedQuery("Team.findTeamMembers")
  q.setParameter("team",u)
  q
}

def findTeamMembershipsCount(u:Team) = {
  findTeamMemberships(u).getResultList.size()
}

def findTeamMemberships(u:Team,p1: Long, p2: Long) : List[TeamMember]=  {
  findTeamMemberships(u).setMaxResults(p2.toInt).setFirstResult(p1.toInt).getResultList.toList.map(_.asInstanceOf[TeamMember])
}



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