package se.bupp.cs3k.server.service

import dao.{GameOccassionDao, CompetitorDao, LadderDao}
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

object Blabb {
  case class XY(x:Float,y:Int)
  class Yo2[T]( create:(Float,List[Int], Int,Int) => T) {


    def build(q:Qualifier,offsetY:Float, stepsToBottom:Int) : (List[T],/*Height*/Int) = {
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
      var service = new LadderService()

      //println("cnts" + cnts)

      // center about subtree - leaf nodes center about a tree their own size
      var subTreeMaxHeight = if (cntTot == 0) 1 else cntTot
      //val thisTreeMaxHeight = service.nearest2Pot(cntTot + 1)
      //val b = service.log2(subTreeMaxHeight)
      val n = create(offsetY,cnts,subTreeMaxHeight, stepsToBottom)

      println(s"offsetY $offsetY , $n , $stepsToBottom : $subTreeMaxHeight + $cntTot")
      (nodes :+ n, subTreeMaxHeight )
    }
  }

  def yeah = new Yo[XY]( (offsetY, subTreeMaxHeight,stepsToBottom) => XY(offsetY.toFloat - 0.5f + subTreeMaxHeight.toFloat/2f ,stepsToBottom) )
  class Yo[T]( create:(Float,Int,Int) => T) {


    def build(q:Qualifier,offsetY:Float, stepsToBottom:Int) : (List[T],/*Height*/Int) = {
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
      var service = new LadderService()



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
class LadderService {

  val log = LoggerFactory.getLogger(this.getClass)

  @Autowired
  var ladderDao:LadderDao = _
  @Autowired
  var competitorDao:CompetitorDao = _

  @Autowired
  var gameDao:GameOccassionDao = _


  @Autowired
  var gameReservationService:GameReservationService = _


  @Transactional
  def isUserMemberOfLadder(t:Competitor, l:Ladder) = {
    import scala.collection.JavaConversions.asScalaBuffer

    val ll = ladderDao.em.merge(l)

    log.debug("ll.participants.size" + ll.participants.size)
    ll.participants.exists( p => p.id.competitor.id == t.id)
  }

  def findApplicableCompetitors(t:User, l:Ladder) = {
    val allCompetitorsByUser = competitorDao.findByUser(t)

    import scala.collection.JavaConversions.asScalaBuffer
    log.info("allCompetitorsByUser " + allCompetitorsByUser.size)
    allCompetitorsByUser.filter {
      case cc:Team => l.competitorType == CompetitorType.TEAM
      case cc:User => l.competitorType == CompetitorType.INDIVIDUAL
    }
  }

  def findParticipants(ladder:Ladder, p1: Long, p2: Long) = {
     competitorDao.findLadderParticipants(ladder,p1,p2)
  }

  @Transactional
  def isCompetitorMemberOfLadder(t:Competitor, l:Ladder) = {
    import scala.collection.JavaConversions.asScalaBuffer
    log.debug("isCompetitorMemberOfLadder")

    val ll = ladderDao.em.merge(l)

    log.debug("ll.participants.size" + ll.participants.size)
    ll.participants.exists( p => p.id.competitor.id == t.id)
  }

  @Transactional
  def storeLadderMember(u:Competitor, t:Ladder) = {

    val pk = new LadderEnrollmentPk



    pk.competitor = ladderDao.em.merge(u)
    pk.ladder = ladderDao.em.merge(t)

    val tm = new LadderEnrollment

    tm.id = pk

    ladderDao.em.persist(tm)
  }

  @Transactional
  def leaveLadder(u:Competitor, t:Ladder) = {

    val pk = new LadderEnrollmentPk
    pk.competitor = ladderDao.em.merge(u)
    pk.ladder = ladderDao.em.merge(t)

    val tm = ladderDao.em.find(classOf[LadderEnrollment],pk)

    ladderDao.em.remove(tm)
  }
  @Autowired
  var resultService:ResultService = _

  def decoratePariticpantResults(participants:List[Competitor], ladder:Ladder) = {
    //val participants = competitorDao.findLadderParticipants(ladder, start, stop)
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

  def appendLevel(q:Qualifier) {
    q.childrenOpt match {
      case Some(children) =>
        children.foreach( c =>
          appendLevel(c)
        )
        //*var childrenNew = children.map(c => appendLevel(c))
        //new Qualifier(q.parentOpt, Some(childrenNew))
      case None =>
        var children = (0 until 2).map(v => new Qualifier(Some(q),None))
        q.childrenOpt = Some(children.toList)
        //new Qualifier(q.parentOpt, children)
    }
  }


  def addOne(q:Qualifier, levelsLeft:Int) : Boolean = {
    def doIt = {
      if (levelsLeft == 0) {
        val childs= q.childrenOpt.getOrElse(List[Qualifier]())
        q.childrenOpt = Some(childs :+ new Qualifier(Some(q),None))
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
  def count(q:Qualifier,cnt:Int = 0) :Int = {
    q.childrenOpt.map( _.foldLeft(0) { case (t,q) => (t + count(q,cnt)) }).getOrElse(0) + 1
  }

  def buildATournament(numOfPlayers:Int) = {
    val numOfCompleteLevels:Int = log2(numOfPlayers)
    val numOfPlayersMoreThanCompleteLevels:Int = numOfPlayers % (1 << numOfCompleteLevels)

     var allComplete = (1 until numOfCompleteLevels).foldLeft(new Qualifier(None,None)) {
      case (q, _) => appendLevel(q) ; q
    }
    val a = count(allComplete)

    (0 until numOfPlayersMoreThanCompleteLevels).foreach( b => addOne(allComplete,numOfCompleteLevels -1 ))
    allComplete
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
