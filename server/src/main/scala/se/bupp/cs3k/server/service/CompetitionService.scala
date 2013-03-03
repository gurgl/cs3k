package se.bupp.cs3k.server.service

import dao.{GameSetupTypeDao, GameOccassionDao, CompetitorDao, LadderDao}
import org.springframework.stereotype.Service
import se.bupp.cs3k.server.model._
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.server.model.Ladder
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.model.{CompetitionState, CompetitorType}
import org.slf4j.LoggerFactory
import se.bupp.cs3k.example.ExampleScoreScheme.{ExContestScore, ExScoreScheme, ExCompetitorScore}



/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-09
 * Time: 22:11
 * To change this template use File | Settings | File Templates.
 */

object TournamentHelper {

  case class TwoGameQualifierPositionAndSize(var p1:String, var p2:String, var id:Int, var left:Float,var top:Float,var width:Float,var height:Float) extends Serializable {

  }


  class ArmHeightVisualizer[T]( create:(Float,List[Int], Int,Int) => T) {


    def build(q:Qualifier,offsetY:Float, stepsToBottom:Int) : (List[T],/*Height*/Int) = {
      val (nodes,cnts,cntTot) = q.children match {
        case Nil => (Nil, Nil, 0)
        case children =>
          val (nodes,cntTot) = children.foldLeft((List[T](),List[Int]())) {
            case ((l,cnts),c) => {
              val (ll,cntC)= build(c, offsetY + cnts.foldLeft(0)((a,b) => a + b), stepsToBottom - 1)
              (l ++ ll, cnts :+ cntC)
            }
          }
          (nodes,cntTot, cntTot.foldLeft(0)((a,b) => math.max(a,b)) * 2)

      }

      // center about subtree - leaf nodes center about a tree their own size
      val subTreeMaxHeight = if (cntTot == 0) 1 else cntTot
      val n = create(offsetY,cnts,subTreeMaxHeight, stepsToBottom)

      println(s"offsetY $offsetY , $n , $stepsToBottom : $subTreeMaxHeight + $cntTot")
      (nodes :+ n, subTreeMaxHeight )
    }
  }


  def appendLevel(q:QualifierWithParentReference) {
    q.childrenOpt match {
      case Some(children) =>
        children.foreach( c =>
          appendLevel(c)
        )
      //*var childrenNew = children.map(c => appendLevel(c))
      //new QualifierWithParentReference(q.parentOpt, Some(childrenNew))
      case None =>
        var children = (0 until 2).map(v => new QualifierWithParentReference(Some(q),None))
        q.childrenOpt = Some(children.toList)
      //new QualifierWithParentReference(q.parentOpt, children)
    }
  }


  def addOne(q:QualifierWithParentReference, levelsLeft:Int) : Boolean = {
    def doIt = {
      if (levelsLeft == 0) {
        val childs= q.childrenOpt.getOrElse(List[QualifierWithParentReference]())
        q.childrenOpt = Some(childs :+ new QualifierWithParentReference(Some(q),None))
        true
      } else false
    }

    q.childrenOpt match {
      case Some(children @ List(aa,bb)) => children.find( qq => addOne(qq, levelsLeft-1)).isDefined
      case Some(children @ List(aa)) => doIt
      case None => doIt
    }
  }
  def nearest2Pot(vv:Int) = {
    var v = vv - 1 ;
    v = v | v >> 1;
    v = v | v >> 2;
    v = v | v >> 4;
    v = v | v >> 8;
    v = v | v >> 16;
    v + 1
  }
  def log2(vv:Int) = {
    var i = 0 ; var j = vv ; while(j>1) { println(j); j = j >> 1 ; i = i + 1 ; i} ; i
  }
  def count(q:QualifierWithParentReference,cnt:Int = 0) :Int = {
    q.childrenOpt.map( _.foldLeft(0) { case (t,q) => (t + count(q,cnt)) }).getOrElse(0) + 1
  }

  def index(q:QualifierWithParentReference,idxSeq:Int = 0) : IndexedQualifier = {

    var curIdx = idxSeq
    val newC = q.childrenOpt match {
      case Some(cs) =>
        val ncs = cs.map { c =>
          val n = index(c,curIdx)
          curIdx = n.idx
          n
        }
        Some(ncs)
      case None => None
    }

    val n = IndexedQualifier(None, newC, curIdx + 1)
    //newC.foreach(cs => cs.foreach(c => c.parentOpt=Some(n)))
    //n.ensuring { _ match { case IndexedQualifier(a,b,c) => a != null && b != null && c != null } }
    n
  }

  def indexedToSimple(i:IndexedQualifier,parentId:Option[Int] = None) : Qualifier = {
    val newC = i.childrenOpt match {
      case Some(cs) =>
        val ncs = cs.map { c =>
          val n = indexedToSimple(c,Some(i.idx))
          n
        }
        ncs
      case None => Nil
    }
    Qualifier(i.idx, newC, parentId)
  }

  def createTournamentStructure(numOfPlayers:Int) = {
    val numOfCompleteLevels:Int = log2(numOfPlayers)
    val numOfPlayersMoreThanCompleteLevels:Int = numOfPlayers % (1 << numOfCompleteLevels)

    var allComplete = (1 until numOfCompleteLevels).foldLeft(new QualifierWithParentReference(None,None)) {
      case (q, _) => appendLevel(q) ; q
    }
    val a = count(allComplete)

    (0 until numOfPlayersMoreThanCompleteLevels).foreach( b => addOne(allComplete,numOfCompleteLevels -1 ))
    allComplete
  }

  def buildPersistableTournament(t:IndexedQualifier,to:Tournament) : List[TournamentStageQualifier]= {
    var chlds = t.childrenOpt match {
      case Some(cs) => cs.flatMap(c => buildPersistableTournament(c, to))
      case None => Nil
    }

    var chldIdx = t.childrenOpt.toList.flatMap( x => x.map(_.idx))
    chlds ++ List(new TournamentStageQualifier(t.idx,to, chldIdx.toList))
  }

  def buildTournament(tournament:Tournament) = {
    var numOfPlayers = tournament.participants.size
    if (numOfPlayers > 2) {
      val structure = createTournamentStructure(numOfPlayers)
      var indexed = index(structure)

      // assign

    }
  }

  def fromPersistedToQualifierTree(l:List[TournamentStageQualifier]) : Qualifier = {
    var mapped = l.map(i => (i.nodeId, new Qualifier(i.nodeId,Nil,None))).toMap

    l.foreach { case n:TournamentStageQualifier =>
      n.childNodeIds match {
        case Nil =>
        case list =>
          val q = mapped(n.nodeId)
          val chls = list.map {
            x =>
              val m = mapped(x)
              m.parentOpt = Some(n.nodeId)
              m
          }

          q.children = chls

      }
    }

    mapped.find { case (k,v) => v.parentOpt.isEmpty}.get._2
  }


  case class XY(x:Float,y:Int)
  def dummyVisualizer = new PositionOnlyVisualizer[XY]( (offsetY, subTreeMaxHeight,stepsToBottom) => XY(offsetY.toFloat - 0.5f + subTreeMaxHeight.toFloat/2f ,stepsToBottom) )

  class PositionOnlyVisualizer[T]( create:(Float,Int,Int) => T) {


    def build(q:QualifierWithParentReference,offsetY:Float, stepsToBottom:Int) : (List[T],/*Height*/Int) = {
      val (nodes,cnts,cntTot) = q.childrenOpt match {
        case Some(children) =>
          val (nodes,cntTot) = children.foldLeft((List[T](),List[Int]())) {
            case ((l,cnts),c) => {
              val (ll,cntC)= build(c, offsetY + cnts.foldLeft(0)((a,b) => a + b), stepsToBottom - 1)
              (l ++ ll, cnts :+ cntC)
            }
          }
          (nodes,cntTot, cntTot.foldLeft(0)((a,b) => math.max(a,b)) * 2)
        case None => (Nil, Nil, 0)
      }
      var service = new CompetitionService()



      // center about subtree - leaf nodes center about a tree their own size
      var subTreeMaxHeight = if (cntTot == 0) 1 else cntTot
      //val thisTreeMaxHeight = service.nearest2Pot(cntTot + 1)
      //val b = service.log2(subTreeMaxHeight)
      val n = create(offsetY,subTreeMaxHeight,stepsToBottom)

      println(s"offsetY $offsetY , $n , $stepsToBottom : $subTreeMaxHeight + $cntTot")
      (nodes :+ n, subTreeMaxHeight )
    }
  }
}

@Service
class CompetitionService {

  val log = LoggerFactory.getLogger(this.getClass)

  @Autowired
  var ladderDao:LadderDao = _
  @Autowired
  var competitorDao:CompetitorDao = _

  @Autowired
  var gameDao:GameOccassionDao = _
  @Autowired
  var gameSetupDao:GameSetupTypeDao = _


  @Autowired
  var gameReservationService:GameReservationService = _

  @Transactional
  def storeCompetition(c:Competition) {

    gameSetupDao.find(c.gameSetup.id).ensuring(_ != null)
    ladderDao.em.merge(c.gameSetup)
   /* if (c.id != null)
      ladderDao.em.merge(c)*/
    ladderDao.em.persist(c)

  }

  @Transactional
  def getTournamentQualifierStructure(t:Tournament) : Qualifier = {
    var tournament = ladderDao.em.merge(t)
    import scala.collection.JavaConversions.asScalaBuffer
    val tree = TournamentHelper.fromPersistedToQualifierTree(tournament.structure.toList)
    tree
  }

  @Transactional
  def generateTournamentStructure(tournament:Tournament, indexed:IndexedQualifier) = {
    val tournamentPrim = ladderDao.em.merge(tournament)
    var persistableStructure = TournamentHelper.buildPersistableTournament(indexed, tournamentPrim )
    import scala.collection.JavaConversions.seqAsJavaList
    tournamentPrim.structure = persistableStructure
    storeCompetition(tournamentPrim)
    tournamentPrim
  }

  def storeCompetitiondd(c:Competition) {
    gameSetupDao.find(c.gameSetup.id).ensuring(_ != null)
    ladderDao.em.merge(c.gameSetup)
    if (c.id != null)
      ladderDao.em.merge(c)
    ladderDao.em.persist(c)

  }

  @Transactional
  def isUserMemberOfLadder(t:Competitor, l:Ladder) = {
    import scala.collection.JavaConversions.asScalaBuffer

    val ll = ladderDao.em.merge(l)

    log.debug("ll.participants.size" + ll.participants.size)
    ll.participants.exists( p => p.id.competitor.id == t.id)
  }

  def findApplicableCompetitors(t:User, l:Competition) = {
    val allCompetitorsByUser = competitorDao.findByUser(t)

    import scala.collection.JavaConversions.asScalaBuffer
    log.info("allCompetitorsByUser " + allCompetitorsByUser.size)
    allCompetitorsByUser.filter {
      case cc:Team => l.competitorType == CompetitorType.TEAM
      case cc:User => l.competitorType == CompetitorType.INDIVIDUAL
    }
  }

  def findParticipants(ladder:Ladder, p1: Long, p2: Long) = {
     competitorDao.findCompetitionParticipants(ladder,p1,p2)
  }

  @Transactional
  def isCompetitorInCompetition(t:Competitor, l:Competition) = {
    import scala.collection.JavaConversions.asScalaBuffer
    log.debug("isCompetitorInCompetition")

    val ll = ladderDao.em.merge(l)

    log.debug("ll.participants.size" + ll.participants.size)
    ll.participants.exists( p => p.id.competitor.id == t.id)
  }

  @Transactional
  def addCompetitorToCompetition(u:Competitor, t:Competition) = {

    val pk = new CompetitionParticipantPk



    pk.competitor = ladderDao.em.merge(u)
    pk.competition = ladderDao.em.merge(t)

    val tm = new CompetitionParticipant

    tm.id = pk

    ladderDao.em.persist(tm)
  }

  @Transactional
  def leaveCompetition(u:Competitor, t:Competition) = {

    val pk = new CompetitionParticipantPk
    pk.competitor = ladderDao.em.merge(u)
    pk.competition = ladderDao.em.merge(t)

    val tm = ladderDao.em.find(classOf[CompetitionParticipant],pk)

    ladderDao.em.remove(tm)
  }
  @Autowired
  var resultService:ResultService = _

  def decoratePariticpantResults(participants:List[Competitor], ladder:Ladder) = {
    //val participants = competitorDao.findCompetitionParticipants(ladder, start, stop)
    val results = ladderDao.findLadderResults(ladder)
    var resultMap = results.groupBy(_._1).map { case (k,v) => (k, v.map(_._2))}

    participants.map { p =>
      resultMap.get(p) match {
        case Some(l) =>
          val total = resultService.getParticipantResult(p,l)
        Pair(p,Some(total))
        case None => (p,None)
      }

    }
  }

  @Transactional
  def startLadder(l:Ladder) = {
    if (l.state == CompetitionState.SIGNUP) {
      import scala.collection.JavaConversions.asScalaBuffer
      import scala.collection.JavaConversions.seqAsJavaList
      val participants = l.participants.map( p => p.id.competitor)
      val range = 0 until participants.size
      val combinations = for(a <- range ; b <- range if b > a) yield (a,b)
      val matches = combinations.map {
        case (i, j) => (participants(i), participants(j))
      }
      matches.foreach {
        case (i, j) =>
          val go = new GameOccassion()
          go.participants = List(i,j).map(p => new GameParticipation(new GameParticipationPk(p,go)))
          go.game == l.gameSetup
          go.competitionGame = new LadderGame(l)
          gameDao.insert(go)
      }
    }
  }


  /*def startTournament(l:Tournament) = {
    if (l.state == CompetitionState.SIGNUP) {
      import scala.collection.JavaConversions.asScalaBuffer
      import scala.collection.JavaConversions.seqAsJavaList
      val participants = l.participants.map( p => p.id.competitor)
      val range = 0 until participants.size
      val combinations = for(a <- range ; b <- range if b > a) yield (a,b)
      val matches = combinations.map {
        case (i, j) => (participants(i), participants(j))
      }
      matches.foreach {
        case (i, j) =>
          val go = new GameOccassion()
          go.participants = List(i,j).map(p => new GameParticipation(new GameParticipationPk(p,go)))
          go.game == l.gameSetup
          go.competitionGame = new LadderGame(l)
          gameDao.insert(go)
      }
    }
  }*/
}
