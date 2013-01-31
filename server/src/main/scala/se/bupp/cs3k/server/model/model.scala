package se.bupp.cs3k.server.model


import javax.persistence._
import java.io.Serializable
import se.bupp.cs3k.api.{Ticket => ApiTicket}
import scala.Predef._

import java.util.{List => JUList, ArrayList => JUArrayList, Date}
import se.bupp.cs3k.model.CompetitorType
import org.hibernate.metamodel.source.binder.Orderable
import java.lang.{Long => JLLong }
import se.bupp.cs3k.server.service.gameserver.{GameServerRepository, GameProcessSettings}
import java.util.Date
import se.bupp.cs3k.server.Cs3kConfig
import se.bupp.cs3k.server.service.GameReservationService
import java.lang


@NamedQueries(Array(
  new NamedQuery(name = "User.findUserTeams", query = "select t from TeamMember tm left join tm.id.team t where tm.id.user.id = :userId")
))
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
  type TeamId = java.lang.Long
}

import Model._


sealed abstract class AbstractUser {
  // TODO : am i used?
  var reservationId:Option[GameReservationService.GameServerReservationId] = None
}
case class RegedUser(var id:UserId) extends AbstractUser
case class AnonUser(var name:String) extends AbstractUser

trait AbstractGameOccassion {
  def gameSessionId:JLLong

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
@NamedQueries(Array(

))
case class Ladder() extends Serializable with Same[JLLong] {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

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

sealed abstract class AbstractTeamRef
case class TeamRef(val id:TeamId) extends AbstractTeamRef
case class VirtualTeamRef(val virtaulTeamId:Long, val name:Option[String]) extends AbstractTeamRef

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

  var accumulatedResultSerializedVersion: Int = _
  var accumulatedResultSerialized: String = _

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
class Team(var name:String) extends Competitor with Same[JLLong] {
  //@Id @GeneratedValue(strategy=GenerationType.AUTO) var id:java.lang.Long = _
  def this() = this(null)



  @OneToMany(mappedBy = "id.team")
  var members:JUList[TeamMember] =  new JUArrayList[TeamMember]()

  /*@OneToOne
  @JoinColumn(name = "COMPETITOR_ID", referencedColumnName = "ID")
  var competitor:Competitor = _*/
}


@Entity
@NamedQueries(Array(
  new NamedQuery(name = "Competitor.findByUser", query = "select c from Competitor c left join c.members t where c = :user1 or t.id.user = :user2"),
  new NamedQuery(name = "Competitor.findLadderParticipants", query = "select c from Ladder l inner join l.participants p inner join p.id.competitor c where l = :ladder")
))
@Inheritance(strategy=InheritanceType.JOINED)
class Competitor extends Serializable {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  def nameAccessor = this match {
    case t:Team => t.name
    case u:User => u.username
  }
}

@Embeddable
case class GameParticipationPk() extends Serializable{
  @ManyToOne
  var competitor:Competitor = _

  @ManyToOne
  var game:GameOccassion = _

  def this(_competitor:Competitor, _game:GameOccassion) = { this() ; competitor = _competitor ; game = _game }

}

@Entity
class GameParticipation() {

  @Id
  var id:GameParticipationPk = _

  def this(_id:GameParticipationPk) = { this() ; this.id = _id}
}


/* @org.hibernate.annotations.GenericGenerator(
    name = "customForeignGenerator",
    strategy = "foreign",
    parameters = Array(new org.hibernate.annotations.Parameter(name = "property", value = "game"))
  )
  @Id @GeneratedValue(generator = "customForeignGenerator")
  */

//@Id @GeneratedValue(strategy=GenerationType.AUTO)

/*@OneToOne(mappedBy = "result")
@PrimaryKeyJoinColumn*/


@Entity
class GameResult extends Serializable {

  @Id @Column(name="id") var id:JLLong = _

  var resultSerializedVersion: Int = _
  var resultSerialized:String = _

  @MapsId
  @OneToOne(mappedBy = "result")
  @JoinColumn(name = "id")
  var game:GameOccassion = _

  def this(_resultSerializedVersion: Int, _resultSerialized:String ) = { this() ; resultSerialized = _resultSerialized ; resultSerializedVersion = _resultSerializedVersion }
}

@Entity
//@Inheritance(strategy=InheritanceType.JOINED)
class GameOccassion extends AbstractGameOccassion with Serializable with Same[JLLong] {
  @Id @GeneratedValue(strategy=GenerationType.AUTO)  var id:JLLong = _

  @OneToMany(mappedBy = "id.game")
  var participants:JUList[GameParticipation] =  new JUArrayList[GameParticipation]()

  //TODO : Make me private
  //@Column("game_session_id")
  var gameSessionId:JLLong = _


  var gameServerStartedAt:Date = _
  def gameSessionIdOpt = Option(gameSessionId)
  def gameSessionIdOpt_=(v:Option[GameReservationService.GameSessionId]):Unit = { gameSessionId = v.map(new lang.Long(_)).getOrElse(null.asInstanceOf[lang.Long])}

  def hasStarted = gameServerStartedAt != null

  def hasFinished = result != null

  def timeTriggerStart = false

  def gameAndRulesId = Cs3kConfig.TEMP_FIX_FOR_STORING_GAME_TYPE

  var competitorType:String = _

  @OneToOne(cascade = Array(CascadeType.ALL))
  @PrimaryKeyJoinColumn
  var result:GameResult = _

  def game = new GameType(
    "tanks",
    "se.bupp.cs3k.example.ExampleScoreScheme.ExContestScore",
    "se.bupp.cs3k.example.ExampleScoreScheme.ExScoreScheme")

  def this(_gameSessionId:Long,_competitorType:String) = { this() ; gameSessionId = _gameSessionId ; competitorType = _competitorType}

   override def toString = "(id = " + id + ", gameSessionId = " + gameSessionId + ")"
}

class LineUp {

}


class GameType(val name:String, val contestScoreClass:String, val scoreSchemeClass:String) {

}

case class NonPersisentGameOccassion(val gameSessionId:JLLong) extends AbstractGameOccassion {

  def timeTriggerStart = true
}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 03:20
 * To change this template use File | Settings | File Templates.
 */


/*
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

  override def getReportableId: JLLong = {
    id
  }

  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id: Long = _
}*/


import se.bupp.cs3k.api.score.ScoreScheme.CompetitorTotal

class TotalAwaredPointsAndScore(var competitor: Competitor, val points:Long, val ct:CompetitorTotal) extends Serializable {
  import scala.collection.JavaConversions.asScalaBuffer
  private val ctAll = ct.getRenderer.render().toSeq
  def elements = java.util.Arrays.asList(ct.getRenderer.render():_*)
}


case class RunningGame(var game:AbstractGameOccassion, var processSettings:GameProcessSettings) {

  def isPublic = game == null

  def requiresTicket = game != null && game.isInstanceOf[GameOccassion]
}
