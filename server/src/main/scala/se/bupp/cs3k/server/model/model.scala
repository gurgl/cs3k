package se.bupp.cs3k.server.model

import se.bupp.cs3k.server.GameServerPool.GameProcessSettings
import javax.persistence._
import java.io.Serializable
import se.bupp.cs3k.api.{Ticket => ApiTicket}
import scala.Predef._

import java.util.{List => JUList, ArrayList => JUArrayList }
import se.bupp.cs3k.model.CompetitorType


@Entity
@PrimaryKeyJoinColumn(name="competitor_id")
case class User(var username:String) extends Competitor {

  var password:String = _
  var email:String = _

  @Transient var wiaPasswordConfirm:String = _
  def isAdmin = true

  def this() = this("")
  override def toString = id + " " + username
}

object Model {
  type UserId = java.lang.Long
}

abstract class AbstractGameOccassion {
  def occassionId:Long

  def timeTriggerStart:Boolean
}

trait Same[T] {
  var id:T
  def isSame(s:Same[T]) = id == s.id
}

/*
object CompetitorType extends Enumeration with Enumv with Serializable {
  type CompetitorType = Value
  val Team = Value("Team")
  val Individual = Value("Individual")
} */

@Entity
case class Ladder() extends Serializable with Same[java.lang.Long] {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:java.lang.Long = _

  var name:String = _

  //@Type(`type` = "se.bupp.cs3k.server.model.CompetitorType")
  @Enumerated(EnumType.ORDINAL)
  var competitorType:CompetitorType= _

  @OneToMany(mappedBy = "id.ladder")
  var participants:JUList[LadderEnrollment] =  new JUArrayList[LadderEnrollment]()


  /*
  override def hashCode(): Int = Util._hashCode(Ladder.this);

  override def equals(x$1: Any): Boolean = Ladder.this.eq(x$1).||(x$1 match {
    case (i: Int,s: String)A((i$1 @ _), (s$1 @ _)) if i$1.==(i).&&(s$1.==(s)) => x$1.asInstanceOf[Ladder].canEqual(Ladder.this)
    case _ => false
  });
  override def canEqual(x$1: Any): Boolean = x$1.$isInstanceOf[Ladder]()
  */

}

@Embeddable
case class LadderEnrollmentPk() extends Serializable{

  @ManyToOne
  var competitor:Competitor = _

  @ManyToOne
  var ladder:Ladder = _

}

@Entity
class LadderEnrollment {

  @Id
  var id:LadderEnrollmentPk = _

}

@Embeddable
case class TeamMemberPk() extends Serializable{

  @ManyToOne
  var team:Team = _

  @ManyToOne
  var user:User = _

}

@Entity
class TeamMember {
  @Id
  var id:TeamMemberPk = _

}

@Entity
@PrimaryKeyJoinColumn(name="COMPETITOR_ID")
class Team extends Competitor with Same[java.lang.Long] {
  //@Id @GeneratedValue(strategy=GenerationType.AUTO) var id:java.lang.Long = _

  var name:String = _

  @OneToMany(mappedBy = "id.team")
  var members:JUList[TeamMember] =  new JUArrayList[TeamMember]()

  /*@OneToOne
  @JoinColumn(name = "COMPETITOR_ID", referencedColumnName = "ID")
  var competitor:Competitor = _*/
}


@Entity
@NamedQueries(Array(
  new NamedQuery(name = "Competitor.findByUser", query = "select c from Competitor c left join c.members t where c = :user1 or t.id.user = :user2")
))
@Inheritance(strategy=InheritanceType.JOINED)
class Competitor extends Serializable {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:java.lang.Long = _

}



@Entity
case class GameOccassion(var occassionId:Long) extends AbstractGameOccassion {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:Long = _

  def timeTriggerStart = true
}

case class NonPersisentGameOccassion(val occassionId:Long) extends AbstractGameOccassion {

  def timeTriggerStart = true
}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 03:20
 * To change this template use File | Settings | File Templates.
 */

@Entity
class Ticket() extends ApiTicket with Serializable {

  @ManyToOne
  var user:User = _

  @ManyToOne
  var game:GameOccassion = _

  def this(id: Long) {
    this()
    this.id = id
  }

  override def getId: java.lang.Long = {
    id
  }

  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id: Long = _
}



case class RunningGame(var game:AbstractGameOccassion, var processSettings:GameProcessSettings) {

  def isPublic = game == null

  def requiresTicket = game != null && game.isInstanceOf[GameOccassion]
}
