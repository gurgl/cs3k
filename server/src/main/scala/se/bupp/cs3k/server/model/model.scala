



package se.bupp.cs3k.server.model


import javax.persistence._
import java.io.Serializable
import se.bupp.cs3k.api.{Ticket => ApiTicket}
import scala.Predef._

import java.util.{List => JUList, ArrayList => JUArrayList, Date}
import se.bupp.cs3k.model.{NewsItemType, CompetitionState, CompetitorType}
import org.hibernate.metamodel.source.binder.Orderable
import java.lang.{Long => JLLong }
import se.bupp.cs3k.server.service.gameserver.{GameServerRepository, GameProcessSettings}
import java.util.Date
import se.bupp.cs3k.server.Cs3kConfig
import se.bupp.cs3k.server.service.GameReservationService
import java.{util, lang}
import concurrent.Future
import se.bupp.cs3k.server.service.TournamentHelper.TwoGameQualifierPositionAndSize
import org.joda.time.Instant
import org.hibernate.`type`.Type
import se.bupp.cs3k.server.model.Qualifier
import scala.Some
import se.bupp.cs3k.server.model.CompetitionParticipantPk
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.server.model.TeamMemberPk
import se.bupp.cs3k.server.model.IndexedQualifier
import se.bupp.cs3k.server.model.GameParticipationPk
import se.bupp.cs3k.server.model.QualifierWithParentReference


@NamedQueries(Array(
  new NamedQuery(name = "User.findUserTeams", query = "select t from TeamMember tm left join tm.id.team t where tm.id.user.id = :userId"),
  new NamedQuery(name = "Team.findUserTeamMemberships", query = "select tm from TeamMember tm inner join fetch tm.id.team t where tm.id.user = :user")
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
class TeamMember extends Serializable {
  @Id
  var id:TeamMemberPk = _

}


@NamedQueries(Array(
  new NamedQuery(name = "Team.findTeamMembers",
    query = "select m from Team t inner join t.members m inner join fetch m.id.user u where t = :team")
))
@Entity
@PrimaryKeyJoinColumn(name="COMPETITOR_ID")
class Team(var name:String) extends Competitor with Same[JLLong] {
  //@Id @GeneratedValue(strategy=GenerationType.AUTO) var id:java.lang.Long = _
  def this() = this(null)

  @OneToMany(mappedBy = "id.team")
  var members:JUList[TeamMember] =  new JUArrayList[TeamMember]()

  override def toString = id + " " + name
}


@Entity
@NamedQueries(Array(


  new NamedQuery(name = "Competitor.findResultsByUser",
    query = "select r from Competitor c left join c.members t, GameOccassion go inner join go.participants p inner join go.result r where (c = :user1 or t.id.user = :user2) and c.id = p.id.competitor.id"),
  new NamedQuery(name = "Competitor.findResultsByTeam",
    query = "select r from GameOccassion go inner join go.participants p inner join go.result r where p.id.competitor = :team"),

  new NamedQuery(name = "Competitor.findGamesByUser",
    query = "select go from Competitor c left join c.members t, GameOccassion go inner join go.participants p left join go.result r where (c = :user1 or t.id.user = :user2) and c.id = p.id.competitor.id and r is null"),
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

  /*@OneToMany(mappedBy = "id.competitor")
  var engagements:java.List[]*/

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


  @Basic(optional = true)
  @Column(nullable = true)
  var seqId:Integer = _


  def this(_id:GameParticipationPk, _seqId:Integer = null) = { this() ; this.id = _id ; seqId = _seqId }
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
@NamedQueries(Array(
  new NamedQuery(name = "Competitor.findResultsByCompetition",

    query = "select gr from GameResult gr inner join gr.game g inner join g.competitionGame cg left join cg.tournament t left join cg.ladder l where (l.id = :comp1 and t is null) or (t.id = :comp2 and l is null) ")
))
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



@NamedQueries(Array(
  new NamedQuery(name = "GameOccassion.findMaxSessionId", query = "select max(go.gameSessionId) from GameOccassion go")
))
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

  var competitorType:CompetitorType = _

  @OneToOne(cascade = Array(CascadeType.ALL))
  @PrimaryKeyJoinColumn
  var result:GameResult = _

  @OneToOne(mappedBy = "gameOccassion", optional = true)
  private var competitionGame:CompetitionGame = _

  //TODO: REname me
  @ManyToOne(targetEntity = classOf[GameSetupType], optional=false)
  var game:GameSetupType = _


  def competitionGameOpt:Option[CompetitionGame] = Option(competitionGame)
  def competitionGameOpt_=(v:Option[CompetitionGame]) : Unit =  {competitionGame = v.orNull }

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

  def this(_gameSessionId:Long,_competitorType:CompetitorType) = { this() ; gameSessionId = _gameSessionId ; competitorType = _competitorType}

   override def toString = "(id = " + id + ", gameSessionId = " + gameSessionId + ")"
}

class LineUp {

  /*
  INSERT INTO COMPETITOR VALUES(1)
INSERT INTO COMPETITOR VALUES(2)
INSERT INTO COMPETITOR VALUES(3)
INSERT INTO COMPETITOR VALUES(4)
INSERT INTO COMPETITOR VALUES(5)
INSERT INTO COMPETITOR VALUES(6)
INSERT INTO USER VALUES('asdf','asdf','asdf',1)
INSERT INTO USER VALUES(NULL,'admin','admin',2)
INSERT INTO USER VALUES('qwer','qwer','qwer',3)
INSERT INTO USER VALUES('wert','wert','wert',4)
INSERT INTO USER VALUES('sdfg','sdfg','sdfg',5)
INSERT INTO USER VALUES('zxcv','zxcv','zxcv',6)
   */
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

/*
@Entity
class GameType(_id:GameServerTypeId, var name:String) extends Serializable {
  @Id private var id:String = if(_id != null) _id.name else null
  def identifier = Symbol(id)
  def identifier_=(s:GameServerTypeId) {id = s.name }

  def this() = this(null,null)
}

@Entity
@Table(uniqueConstraints=Array(new UniqueConstraint(columnNames=Array("GAMETYPE_ID","setupIdName"))))
class GameSetupType(_setupId:GameProcessTemplateId, val name:String, val contestScoreClass:String, val scoreSchemeClass:String) extends Serializable with Same[JLLong] {

  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  @ManyToOne
  var gameType:GameType = _

  private var setupIdName:String = if(_setupId != null) _setupId.name else null

  def setupId = Symbol(setupIdName)
  def setupId_=(s:GameProcessTemplateId) {setupIdName = s.name }

  def this() = this(null,null,null,null)

}

 */

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


@NamedQueries(Array(
  new NamedQuery(name = "Competition.findUserCompetitions",
    query = "select c2 from Competitor c left join c.members t, Competition c2 inner join c2.participants p where (c = :user1 or t.id.user = :user2) and c.id = p.id.competitor.id")
  ,
  new NamedQuery(name = "Competition.findCompetitionPariticpants",
    query = "select c2 from Competitor c left join c.members t, Competition c2 inner join c2.participants p where (c = :user1 or t.id.user = :user2) and c.id = p.id.competitor.id")
))
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

  def createGameFromTournament(comp:TournamentStageQualifier) = {
    var occassion = new GameOccassion()
    occassion.game = gameSetup
    occassion.competitorType = competitorType
    occassion.competitionGameOpt = Some(comp)
    occassion
  }

  def this() = this(null,null,null,null)
}


@Entity
@Inheritance(strategy=InheritanceType.JOINED)
abstract class CompetitionGame extends Serializable {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  @OneToOne(optional = true)
  @JoinColumn(name = "GAME_OCCASSION_ID", referencedColumnName = "ID", nullable = true)
  private var gameOccassion:GameOccassion = _

  def gameOccassionOpt:Option[GameOccassion] = Option(gameOccassion)
  def gameOccassionOpt_=(v:Option[GameOccassion]) : Unit =  {gameOccassion = v.orNull }


  def competition:Competition
}


trait CanHaveChildren {
  def childrenOpt:Option[List[CanHaveChildren]]
}
case class IndexedQualifier(var parentOpt:Option[IndexedQualifier], var childrenOpt:Option[List[IndexedQualifier]], var idx:Int)

case class QualifierWithParentReference(var parentOpt:Option[QualifierWithParentReference], var childrenOpt:Option[List[QualifierWithParentReference]]) extends CanHaveChildren {}

case class Qualifier(val nodeId:Int, var children:List[Qualifier], var parentOpt:Option[Int]) {

  def flatten : List[Qualifier]= {
    List(this) ++ this.children.flatMap(_.flatten)
  }
}

/*
  override def childrenOpt:Option[List[CanHaveChildren]] = children match  {
    case Nil => None
    case list => Some(list)
  }*/


/*

trait CanHaveChildren {
  def childrenOpt:Option[List[QualifierWithParentReference]]
}

case class Qualifier(var childrenOpt:Option[List[QualifierWithParentReference]]) extends CanHaveChildren {

}

case class QualifierWithIdx(val nodeId:Int, var childrenOpt:Option[List[QualifierWithParentReference]], parentOpt:Option[Int]) extends CanHaveChildren {

}*/


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

  def competition:Competition = tournament

  def this() = this(-1,null,Nil)
}

@Entity
class LadderGame(_ladder:Ladder) extends CompetitionGame {

  @ManyToOne(optional = false)
  var ladder:Ladder = _ladder

  def competition:Competition = ladder

  def this() = this(null)
}

@Entity
class TeamJoinRequest(_player:User, _team:Team,_teamMemberOpt:Option[User] = None) extends Serializable with Same[JLLong] {

  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  @ManyToOne
  var player:User = _player

  @ManyToOne
  var team:Team = _team

  @ManyToOne
  private var teamMember:User = _teamMemberOpt.orNull

  def teamMemberOpt = Option(teamMember)
  def teamMemberOpt_=(v:User) : Unit = { teamMember = v }

  def this() = this(null,null)

}
/*
@Entity
class EventLogEntry(_type:String, _msg:String) extends Serializable with Same[JLLong] {

  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _
}



@Entity
sealed abstract class AbstractNewsItemEvent(_u:User,_competition:Competition,_t:Team, _instant:Instant) {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  @ManyToOne
  var competition = _competition

  @ManyToOne
  var user = _u

  @ManyToOne
  var team = _t

}
*/

trait HasNewsItemFields {
  def id:lang.Long
  def competition:Competition
  def competitor1:Competitor
  def competitor2:Competitor
  def messageType:NewsItemType
  def competitionState:CompetitionState
  def dateTime:Instant
  def seen:Boolean
}



object NewsItemQ {
  final val findByUser: String = " from UserNewsItem un inner join un.newsItem n where un.user = :user and n.dateTime between :startDate and :endDate"
  final val findByCompetition: String = " from NewsItem n where n.competition = :competition and n.dateTime between :startDate and :endDate"
  final val findByTeam: String = " from NewsItem n where n.competitor1 = :team and n.dateTime between :startDate and :endDate"
  final val findAll: String = " from NewsItem n where n.dateTime between :startDate and :endDate"
}

@NamedQueries(Array(
  new NamedQuery(name = "NewsItem.findByCompetition", query = "select n" + " from NewsItem n where n.competition = :competition and n.dateTime between :startDate and :endDate"),
  new NamedQuery(name = "NewsItem.findByCompetition.count", query = "select count(*)" + " from NewsItem n where n.competition = :competition and n.dateTime between :startDate and :endDate"),

  new NamedQuery(name = "NewsItem.findByTeam", query = "select n" + " from NewsItem n where n.competitor1 = :team and n.dateTime between :startDate and :endDate"),
  new NamedQuery(name = "NewsItem.findByTeam.count", query = "select count(*)" + " from NewsItem n where n.competitor1 = :team and n.dateTime between :startDate and :endDate"),

  new NamedQuery(name = "NewsItem.findAll", query = "select n" + " from NewsItem n where n.dateTime between :startDate and :endDate"),
  new NamedQuery(name = "NewsItem.findAll.count", query = "select count(*)" + " from NewsItem n where n.dateTime between :startDate and :endDate"),

  new NamedQuery(name = "NewsItem.findByUser", query = "select un" + " from UserNewsItem un inner join un.newsItem n where un.user = :user and n.dateTime between :startDate and :endDate"),
  new NamedQuery(name = "NewsItem.findByUser.count", query = "select count(*)" +" from UserNewsItem un inner join un.newsItem n where un.user = :user and n.dateTime between :startDate and :endDate")
))
@Entity
class NewsItem(_competition:Competition,_t1:Competitor,_t2:Competitor,_m:NewsItemType,_s:CompetitionState, _date:Instant) extends HasNewsItemFields {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  @ManyToOne(optional = true)
  var competition = _competition

  @ManyToOne(optional = true)
  var competitor1 = _t1

  @ManyToOne(optional = true)
  var competitor2 = _t2

  @Enumerated(EnumType.ORDINAL)
  var messageType:NewsItemType = _m

  @Enumerated(EnumType.ORDINAL)
  var competitionState:CompetitionState = _s

  /*@OneToMany(mappedBy = "newsItem",fetch = FetchType.LAZY)
  var subscribers:JUList[UserNewsItem] =  new JUArrayList[UserNewsItem]()*/

  @Column
  @org.hibernate.annotations.Type( `type`="org.jadira.usertype.dateandtime.joda.PersistentInstantAsTimestamp")
  var dateTime:Instant = _date
  def seen = true

  def this() = this(null,null,null,null,null,null)
}

@Entity
class UserNewsItem(ni:NewsItem, _user:User, _s:Boolean = false) extends HasNewsItemFields {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

  @ManyToOne
  var newsItem:NewsItem = ni

  @ManyToOne
  var user:User = _user

  var seen:Boolean = _s

  def this() = this(null,null,false)

  def competition = newsItem.competition

  def competitor1 = newsItem.competitor1

  def competitor2 = newsItem.competitor2

  def messageType = newsItem.messageType

  def competitionState = newsItem.competitionState

  def dateTime = newsItem.dateTime

}



/*
@Embeddable
class NewsItemChannelPk(_c:Channel,_n:NewsItem) extends Serializable {
  var channel:Channel = _c
  var news:NewsItem = _n

}
@Entity
class NewsItemChannel(_pk:NewsItemChannelPk) {
  @Id var pk:NewsItemChannelPk = _pk
}
*/

      /*
  @Entity
  class NewsItem(_d:Date) {
    @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

    @OneToMany(mappedBy = "id.channel")
    var publishedTo:JUList[NewsItemChannel] =  new JUArrayList[NewsItemChannel]()

    var date:Date = _d
  }

  @Entity
  class Subscription(_c:Channel) {
    @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:JLLong = _

    @ManyToOne(optional = true)
    var channel = _c
  }
  */


  // subscriptions =

    /*
    view team
    view player
    view contest
    */
    /*
            scala: error while loading Instant, class file 'C:\Users\karlw\.ivy2\cache\joda-time\joda-time\jars\joda-time-2.0.jar(org/joda/time/Instant.class)' is broken
(class java.lang.RuntimeException/bad constant pool tag 9 at byte 48)
    case class PlayerJoinedTeam(u:User, t:Team, instant:Instant) extends AbstractNewsItemEvent(u,null,t,instant)
    case class PlayerLeftTeam(u:User, t:Team, instant:Instant) extends AbstractNewsItemEvent(u,null,t,instant)
    case class CompetitorEnteredContest(u:User, c:Competition, instant:Instant) extends AbstractNewsItemEvent(u, c ,null, instant)
    case class CompetitorLeftContest(u:User, c:Competition, instant:Instant) extends AbstractNewsItemEvent(u, c, null,instant)
    case class CompetitorContestVictory(u:User, c:Competition, instant:Instant) extends AbstractNewsItemEvent(u, c, null,instant)
    case class CompetitionStateChange(s:CompetitionState, c:Competition, instant:Instant) extends AbstractNewsItemEvent(null, null, instant)

     */


    // Seen by team + player
/*    case class PlayerJoinedTeam(u:User, t:Team, instant:Instant) extends AbstractNewsItemEvent(u,null,t,instant)
    case class PlayerLeftTeam(u:User, t:Team, instant:Instant) extends AbstractNewsItemEvent(u,null,t,instant)
    // Seen by player/team members : select m from Mess m inner join m.competitor c inner join c.members
    // Seen by competition members : select m from Mess m inner join m.competition co inner join co.participants p inner join p.id.competitor

    case class CompetitorEnteredContest(u:User, c:Competition, instant:Instant) extends AbstractNewsItemEvent(u, c ,null, instant)
    case class CompetitorEnteredContest(t:Team, c:Competition, instant:Instant) extends AbstractNewsItemEvent(null, c ,t, instant)
    case class CompetitorLeftContest(u:User, c:Competition, instant:Instant) extends AbstractNewsItemEvent(u, c, null,instant)
    case class CompetitorLeftContest(u:Team, c:Competition, instant:Instant) extends AbstractNewsItemEvent(null, c, u, instant)
    case class CompetitorContestVictory(u:User, c:Competition, instant:Instant) extends AbstractNewsItemEvent(u, c, null,instant)
    case class CompetitorContestVictory(u:Team, c:Competition, instant:Instant) extends AbstractNewsItemEvent(null, c, u, instant)
    case class CompetitionStateChange(s:CompetitionState, c:Competition, instant:Instant) extends AbstractNewsItemEvent(null, null, instant)

    object NewsItemEvent {

    }
  }
  */




