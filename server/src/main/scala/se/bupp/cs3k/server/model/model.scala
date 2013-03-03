package se.bupp.cs3k.server.model


import javax.persistence._
import java.io.Serializable
import se.bupp.cs3k.api.{Ticket => ApiTicket}
import scala.Predef._

import java.util.{List => JUList, ArrayList => JUArrayList, Date}
import se.bupp.cs3k.model.{CompetitionState, CompetitorType}
import org.hibernate.metamodel.source.binder.Orderable
import java.lang.{Long => JLLong }
import se.bupp.cs3k.server.service.gameserver.{GameServerRepository, GameProcessSettings}
import java.util.Date
import se.bupp.cs3k.server.Cs3kConfig
import se.bupp.cs3k.server.service.GameReservationService
import java.{util, lang}
import concurrent.Future


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

  type ProcessToken = Int


  type GameSessionId = Long
  type GameOccassionId = Long
  type GameServerReservationId = Long
  type ReservationDetails = (AbstractUser, Option[AbstractTeamRef])
  type VirtualTeamId = Long

  type Players = Map[GameServerReservationId,ReservationDetails]
  type TeamsDetailsOpt = Option[List[AbstractTeamRef]]
  type Session = (Players, TeamsDetailsOpt)

  type GameServerTypeId = Symbol
  type GameProcessTemplateId = Symbol
  type GameAndRulesId = (GameServerTypeId, GameProcessTemplateId)




}

import Model._


sealed abstract class AbstractUser {
  // TODO : am i used?
  var reservationId:Option[GameServerReservationId] = None
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

class ReschedulingScheme (
  var numOfReschedulesPerGame:Int,
  var numOfReschedulesTotal:Int,
  var maximumDaysFromCurrent:Int
                          ) {

}

class LadderScheme(
  var numOfGamesPerCompetitor:Int,
  var start:Date,
  var prelimaryEnd:Date,
  var definitiveEnd:Date

           ) {
}



@Entity
@NamedQueries(Array(

))
class Ladder(_name:String,_competitorType:CompetitorType, _gameSetup:GameSetupType, _state:CompetitionState) extends Competition(_name,_competitorType, _gameSetup, _state) with Serializable with Same[JLLong] {

  @OneToMany(mappedBy = "ladder")
  var games:JUList[LadderGame] =  new JUArrayList[LadderGame]()

  def this() = this(null,null,null,null)
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
case class CompetitionParticipantPk() extends Serializable{

  @ManyToOne
  var competitor:Competitor = _

  @ManyToOne
  var competition:Competition = _

}

@Entity
class CompetitionParticipant {

  @Id
  var id:CompetitionParticipantPk = _

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

}


@Entity
@NamedQueries(Array(
  new NamedQuery(name = "Competitor.findByUser", query = "select c from Competitor c left join c.members t where c = :user1 or t.id.user = :user2"),
  new NamedQuery(name = "Competitor.findCompetitionParticipants",
    query = "select c from Competition l inner join l.participants p inner join p.id.competitor c where l = :competition"),
  new NamedQuery(name = "Competitor.findLadderResults",
    query =
      """select new scala.Tuple2(c,r) from Ladder l
        inner join l.games lg
        inner join lg.gameOccassion go
        inner join go.result r
        inner join l.participants p
        inner join p.id.competitor c
        where l = :ladder""")


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

  var competitorType:String = _

  @OneToOne(cascade = Array(CascadeType.ALL))
  @PrimaryKeyJoinColumn
  var result:GameResult = _

  @OneToOne(mappedBy = "gameOccassion", optional = true)
  var competitionGame:CompetitionGame = _

  @ManyToOne(targetEntity = classOf[GameSetupType], optional=false)
  var game:GameSetupType = _


  def gameSessionIdOpt = Option(gameSessionId)
  def gameSessionIdOpt_=(v:Option[GameSessionId]):Unit = { gameSessionId = v.map(new lang.Long(_)).getOrElse(null.asInstanceOf[lang.Long])}

  def hasStarted = gameServerStartedAt != null

  def hasFinished = result != null

  def timeTriggerStart = false

  def gameAndRulesId = Cs3kConfig.TEMP_FIX_FOR_STORING_GAME_TYPE



  /*new GameSetupType(
    "tanks",
    "se.bupp.cs3k.example.ExampleScoreScheme.ExContestScore",
    "se.bupp.cs3k.example.ExampleScoreScheme.ExScoreScheme")*/

  def this(_gameSessionId:Long,_competitorType:String) = { this() ; gameSessionId = _gameSessionId ; competitorType = _competitorType}

   override def toString = "(id = " + id + ", gameSessionId = " + gameSessionId + ")"
}

class LineUp {

}

@Entity
class GameType(_id:GameServerTypeId, var name:String) extends Serializable {
  @Id var id:GameServerTypeId = _id

  def this() = this(null,null)
}

@Entity
@Table(uniqueConstraints=Array(new UniqueConstraint(columnNames=Array("GAMETYPE_ID","setupId"))))
class GameSetupType(var setupId:GameProcessTemplateId, val name:String, val contestScoreClass:String, val scoreSchemeClass:String) extends Serializable with Same[JLLong] {

  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  @ManyToOne
  var gameType:GameType = _


  def this() = this(null,null,null,null)

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



import se.bupp.cs3k.api.score.ScoreScheme.CompetitorTotal

class TotalAwaredPointsAndScore(var competitor: Competitor, val points:Long, val ct:CompetitorTotal) extends Serializable {
  import scala.collection.JavaConversions.asScalaBuffer
  private val ctAll = ct.getRenderer.render().toSeq
  def elements = java.util.Arrays.asList(ct.getRenderer.render():_*)
}


case class RunningGame(var game:AbstractGameOccassion, var processSettings:GameProcessSettings, var done:Future[ProcessToken]) {

  def isPublic = game == null

  def requiresTicket = game != null && game.isInstanceOf[GameOccassion]
}

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
class Competition(_name:String,_competitorType:CompetitorType, _gameSetup:GameSetupType, _state:CompetitionState) extends Serializable {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  var name:String = _name

  //@Type(`type` = "se.bupp.cs3k.server.model.CompetitorType")
  @Enumerated(EnumType.ORDINAL)
  val competitorType:CompetitorType= _competitorType

  @OneToMany(mappedBy = "id.competition")
  var participants:JUList[CompetitionParticipant] =  new JUArrayList[CompetitionParticipant]()

  @ManyToOne(optional = false)
  val gameSetup:GameSetupType = _gameSetup

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  var state:CompetitionState = _state

  def this() = this(null,null,null,null)
}

@Entity
class Tournament(_name:String,_competitorType:CompetitorType, _gameSetup:GameSetupType, _state:CompetitionState) extends Competition(_name,_competitorType, _gameSetup, _state) {

  @OneToMany(mappedBy = "tournament",cascade = Array(CascadeType.ALL))
  var structure:util.List[TournamentStageQualifier] = new util.ArrayList[TournamentStageQualifier]()

  def this() = this(null,null,null,null)
}


@Entity
@Inheritance(strategy=InheritanceType.JOINED)
abstract class CompetitionGame {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  @OneToOne(optional = true)
  @JoinColumn(name = "GAME_OCCASSION_ID", referencedColumnName = "ID", nullable = true)
  var gameOccassion:GameOccassion = _
}


case class IndexedQualifier(var parentOpt:Option[IndexedQualifier], var childrenOpt:Option[List[IndexedQualifier]], var idx:Int)

case class Qualifier(var parentOpt:Option[Qualifier], var childrenOpt:Option[List[Qualifier]]) {

}

@Entity
class TournamentStageQualifier(_nodeId:Int, _tournament:Tournament, _childNodeIds:List[Int]) extends CompetitionGame {
  //@Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  var nodeId:Int = _nodeId

  //var childNodeIdsSerialized:String = _

  import scala.collection.JavaConversions.seqAsJavaList
  import scala.collection.JavaConversions.asScalaBuffer

  @ElementCollection
  private var childNodeIdsJava:util.List[Integer] = _childNodeIds.map( i => new Integer(i))

  def childNodeIds:List[Int] = childNodeIdsJava.map( i => i.toInt).toList

  @ManyToOne(optional = false)
  var tournament:Tournament= _tournament

  def this() = this(-1,null,Nil)
}

@Entity
class LadderGame(_ladder:Ladder) extends CompetitionGame {

  @ManyToOne(optional = false)
  var ladder:Ladder = _ladder

  def this() = this(null)
}




