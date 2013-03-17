package se.bupp.cs3k.server.service

import dao.{GameSetupTypeDao, GameOccassionDao, CompetitorDao, LadderDao}
import org.springframework.stereotype.Service
import se.bupp.cs3k.server.model._
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import se.bupp.cs3k.model.{CompetitionState, CompetitorType}
import org.slf4j.LoggerFactory
import se.bupp.cs3k.example.ExampleScoreScheme.{ExContestScore, ExScoreScheme, ExCompetitorScore}
import se.bupp.cs3k.server.service.TournamentHelper.{QualifierState, TwoGameQualifierPositionAndSize}
import se.bupp.cs3k.server.model.Qualifier
import se.bupp.cs3k.server.service.TournamentHelper.TwoGameQualifierPositionAndSize
import scala.Some
import se.bupp.cs3k.server.model.CompetitionParticipantPk
import se.bupp.cs3k.server.model.User
import se.bupp.cs3k.server.model.IndexedQualifier
import se.bupp.cs3k.server.model.GameParticipationPk
import se.bupp.cs3k.server.model.QualifierWithParentReference
import java.lang


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-09
 * Time: 22:11
 * To change this template use File | Settings | File Templates.
 */

object TournamentHelper {

  /*case class TwoGameQualifierPositionAndSize(var p1:String, var p2:String, var id:Int, var left:Float,var top:Float,var width:Float,var height:Float) extends Serializable {

  }*/

  case class TwoGameQualifierPositionAndSize(var p1:Option[String], var p2:Option[String], var id:Int, var left:Float,var top:Float,var width:Float,var height:Float,state:QualifierState.QualifierState = QualifierState.Undetermined) extends Serializable {
      var winnerPosOpt:Option[(Float,Float)] = None
  }


  object QualifierState extends Enumeration {
    type QualifierState = Value
    val Played,Determined,Undetermined = Value
  }
  val topTextAreaHeight = 10
  val lineToTextMargin = 5
  val textLeftMargin = 5


  def createLayout(numOfPlayers:Int ): List[TwoGameQualifierPositionAndSize] = {
    val yMod = 70
    val screenOffsetY = 20
    var i = 0

    val yo = new TournamentHelper.ArmHeightVisualizer[TwoGameQualifierPositionAndSize](
      (offsetY, subTreesHeights, subTreeHeight, stepsToBottom, nodeId) => {
        def bupp(height: Float) = {
          yMod * (offsetY.toFloat + (subTreeHeight.toFloat / 2f + height / 2f))
        }

        val (top, bot) = subTreesHeights match {
          case x :: y :: Nil =>
            println(x + " " + y)
            (bupp(-x), bupp(y))
          case x :: Nil => (bupp(-x), bupp(0.5f))
          case Nil => (bupp(-0.5f), bupp(0.5f))
        }
        //i = i +1
        //println(s"$top $bot ${subTreesHeights.size} $subTreesHeights")
        new TwoGameQualifierPositionAndSize(subTreesHeights.lift(0).map(_.toString), subTreesHeights.lift(1).map(_.toString), 1234, stepsToBottom * 100, screenOffsetY + top, 100, math.abs(top - bot),QualifierState.Undetermined)
      }
    )

    val numOfCompleteLevels: Int = TournamentHelper.log2(numOfPlayers)
    val numOfPlayersMoreThanCompleteLevels: Int = numOfPlayers % (1 << numOfCompleteLevels)
    val numOfLevels = numOfCompleteLevels + (if (numOfPlayersMoreThanCompleteLevels > 0) 1 else 0) - 1

    val qa = TournamentHelper.indexedToSimple(TournamentHelper.index(TournamentHelper.createTournamentStructure(numOfPlayers)))
    var listn = yo.build(qa, 0.0f, numOfLevels)._1
    listn
  }

  /**
   * Distributes elements list:List[T] into the seq of (0 .. list.size -1)
   * @return
   */
  type SlotDistributor[T] = (List[T]) => Map[Int,T]


  def deterministic:TournamentHelper.SlotDistributor[Competitor] = (competitorIds:List[Competitor]) => {
    var slots = (0 until competitorIds.size).map( i => (i,Option.empty[Competitor])).toMap


    val numOfSlots = competitorIds.size
    var ptr = 5
    competitorIds.foreach { cid =>

      ptr = numOfSlots % numOfSlots
      while(slots(ptr).isDefined) {
        ptr = ptr + 3
        ptr = ptr % numOfSlots
      }
      slots = slots + (ptr -> Some(cid))
    }
    slots.map { case (k,v) => k -> v.get }
  }

  class ArmHeightVisualizer[T]( create:(Float,List[Int], Int, Int, Qualifier) => T) {

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
      val n = create(offsetY,cnts,subTreeMaxHeight, stepsToBottom, q)

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

  @Autowired
  var resultService:ResultService = _


  @Transactional
  def storeCompetition(c:Competition) {

    gameSetupDao.find(c.gameSetup.id).ensuring(_ != null)
    ladderDao.em.merge(c.gameSetup)
   /* if (c.id != null)
      ladderDao.em.merge(c)*/
    ladderDao.em.persist(c)

  }

  @Transactional
  def getNumberOfParticipants(cDet:Competition) = {
    val cc = ladderDao.em.find(classOf[Competition], cDet.id)
    cc.participants.size
  }

  @Transactional
  def getTournamentQualifierStructure(t:Tournament) : Qualifier = {
    var tournament = ladderDao.em.merge(t)
    import scala.collection.JavaConversions.asScalaBuffer
    val tree = TournamentHelper.fromPersistedToQualifierTree(tournament.structure.toList)
    tree
  }

  @Transactional
  def storeTournamentStructure(tournament:Tournament, indexed:IndexedQualifier) = {
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

  /**
   * Distribute players in leafs ordered from top top bottom. 0 representing top and numOPlayers-1 at the bottom.
   *
   * @param tournamentDet
   * @param numOfPlayersPerStage
   * @param distributor
   */
  @Transactional
  def distributePlayersInTournament(tournamentDet:Tournament, numOfPlayersPerStage:Int, distributor:TournamentHelper.SlotDistributor[Competitor]) {
    import scala.collection.JavaConversions.asScalaBuffer
    val tournament = ladderDao.em.find(classOf[Tournament],tournamentDet.id)
    val competitors = tournament.participants.map(_.id.competitor).toList
    val firstChallanges = tournament.structure.filter(_.childNodeIds.size < numOfPlayersPerStage)
    firstChallanges.foreach(x => println("b" + x.nodeId + " " + x.childNodeIds.toList))

    val matchupLottery = distributor(competitors)
    println(matchupLottery.size)
    var leafIndex = 0
    firstChallanges.foreach { t =>
      val s = "tour_" + tournament.id + "_" + t.nodeId
      val go = tournament.createGameFromTournament(t)
      val gameCompetitors:Map[Int,Competitor] = (t.childNodeIds.size until numOfPlayersPerStage).map {
        i =>
        val comp = matchupLottery(leafIndex)
        leafIndex = leafIndex + 1
        (i -> comp)
      }.toMap

      val orderedCompetitors = gameCompetitors.toList.sortBy(_._1)
      val go2 = gameReservationService.addCompitorsAndStore(go,orderedCompetitors)
      //ladderDao.em.persist(go2)
      t.gameOccassionOpt = Some(go2)
      ladderDao.em.persist(t)
    }
  }

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
          go.competitionGameOpt = Some(new LadderGame(l))
          gameDao.insert(go)
      }
    }
  }

  def onGameEnded(cg:CompetitionGame,ranking:Map[Int,Long]) {
    cg match {
      case t:TournamentStageQualifier =>
        import scala.collection.JavaConversions.asScalaBuffer
        t.tournament.structure.find(_.childNodeIds.contains(t.nodeId)) match {
          case Some(qualifier) =>
            val (_,winnerComp) = ranking.head
            val comp = competitorDao.find(new lang.Long(winnerComp)).get
            val (_,idx) = qualifier.childNodeIds.zipWithIndex.find(_._1 == t.nodeId).get

            val (go,postCreate) = qualifier.gameOccassionOpt match {
              case Some(go) => (go, (g:GameOccassion) => ())
              case None =>
                (t.tournament.createGameFromTournament(qualifier), (g:GameOccassion) => {
                   qualifier.gameOccassionOpt = Some(g)
                   ladderDao.em.persist(qualifier)
                   ()
                })
            }

            val go2 = gameReservationService.addCompitorsAndStore(go,List((idx,comp)))
            postCreate(go2)

            //ladderDao.em.persist(go2)

          case None => // Winner!?
        }
      case l:LadderGame =>
    }
  }

  @Transactional
  def startCompetition(competition:Competition) {
    competition match {
      case t:Tournament => startTournament(t)
      case t:Ladder => startLadder(t)
    }
  }

  @Transactional
  def startTournament(tournamentDet:Tournament) = {
    val tournament = ladderDao.em.find(classOf[Tournament], tournamentDet.id)

    if (List(CompetitionState.SIGNUP,CompetitionState.SIGNUP_CLOSED).contains(tournament.state)) {
      val numOfPlayers = tournament.participants.size

      val structure = TournamentHelper.createTournamentStructure(numOfPlayers)
      val indexed = TournamentHelper.index(structure)

      tournament.state = CompetitionState.RUNNING
      val tourPrep1 = storeTournamentStructure(tournament, indexed)

      distributePlayersInTournament(tourPrep1,2,TournamentHelper.deterministic)


    }
  }

  @Transactional
  def createLayout2(tournamentDet:Tournament, screenOffsetY:Int = 20): List[TwoGameQualifierPositionAndSize] = {
    val tournament = ladderDao.em.find(classOf[Tournament],tournamentDet.id)
    import scala.collection.JavaConversions.asScalaBuffer
    val participants = tournament.participants.map(_.id.competitor)
    val compIdToName = participants.map(p => (p.id, p.nameAccessor)).toMap

    val nodeIdsByGame = tournament.structure.map(s => (s.nodeId, s.gameOccassionOpt)).toMap
    val nodeIdsByCompetitorsSortedBySequenceId = nodeIdsByGame.map {
      case (k, v) => (k, Map.empty ++ v.toList.flatMap(_.participants.map( p => (p.seqId.toInt,p.id.competitor.id)).toMap))
    }




    val numOfPlayers = participants.size

    val yMod = 70


    val tournamentRenderModelBuilder = new TournamentHelper.ArmHeightVisualizer[TwoGameQualifierPositionAndSize](
      (offsetY, subTreesHeights, subTreeHeight, stepsToBottom, qualifier) => {
        def bupp(height: Float) = {
          yMod * (offsetY.toFloat + (subTreeHeight.toFloat / 2f + height / 2f))
        }

        val (top, bot) = subTreesHeights match {
          case x :: y :: Nil =>
            println(x + " " + y)
            (bupp(-x), bupp(y))
          case x :: Nil => (bupp(-x), bupp(0.5f))
          case Nil => (bupp(-0.5f), bupp(0.5f))
        }
        //i = i +1
        //println(s"$top $bot ${subTreesHeights.size} $subTreesHeights")
        val game = nodeIdsByGame(qualifier.nodeId)
        val competitorsInSequenceOrder = nodeIdsByCompetitorsSortedBySequenceId(qualifier.nodeId)


        // Competitors are assigned slot in competitorsInSequenceOrder.last = bottom position
        val namesOpts = (0 until 2).map {
          i =>
            val name = competitorsInSequenceOrder.lift(i).flatMap(ii => compIdToName.get(ii))
            (i, name)
        }.toMap

        val state = game match {
          case None => QualifierState.Undetermined
          case Some(g) => //Option(g.result).map(QualifierState.Played).getOrElse(QualifierState.Determined)
            if (g.result == null) {
              if(g.participants.size < 2) QualifierState.Undetermined else QualifierState.Determined
            } else {
              QualifierState.Played
            }
        }

        val effectiveTop: Float = screenOffsetY + top
        val width: Float = 100
        val left: Float = stepsToBottom * 100
        val qualifierRenderModel: TwoGameQualifierPositionAndSize = new TwoGameQualifierPositionAndSize(namesOpts(0), namesOpts(1), qualifier.nodeId, left, effectiveTop, width, math.abs(top - bot), state) {

        }
        if(qualifier.parentOpt.isEmpty) {
          qualifierRenderModel.winnerPosOpt = Some(left+width, screenOffsetY + bupp(0f))
        }
        qualifierRenderModel
      }
    )

    val structure = getTournamentQualifierStructure(tournament)



    val numOfCompleteLevels: Int = TournamentHelper.log2(numOfPlayers)
    val numOfPlayersMoreThanCompleteLevels: Int = numOfPlayers % (1 << numOfCompleteLevels)
    val numOfLevels = numOfCompleteLevels + (if (numOfPlayersMoreThanCompleteLevels > 0) 1 else 0) - 1

    //val qa = TournamentHelper.indexedToSimple(TournamentHelper.index(TournamentHelper.createTournamentStructure(numOfPlayers)))
    val listn = tournamentRenderModelBuilder.build(structure, 0.0f, numOfLevels)._1
    listn
  }
}
