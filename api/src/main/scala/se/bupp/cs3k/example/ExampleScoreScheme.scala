package se.bupp.cs3k.example

import se.bupp.cs3k.api.score.{ScoreScheme, ContestScore, CompetitorScore}
import reflect.BeanProperty
import java.{util, lang}
import se.bupp.cs3k.api.score.ScoreScheme.CompetitorTotal
import com.fasterxml.jackson.annotation.JsonTypeInfo
import se.bupp.cs3k.api.score.ScoreScheme.CompetitorTotal.Render

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-15
 * Time: 00:44
 * To change this template use File | Settings | File Templates.
 */

object ExampleScoreScheme {

  case class ExCompetitorScore(val kills:Int, val diffKills:Int, val trophys:Int) extends CompetitorScore

  case class JavaTuple2(@BeanProperty var a:Int,@BeanProperty var b:Int)  {
    def this() = this(-1,-1)
  }
  class ExContestScore(@BeanProperty var s:java.util.Map[Long,JavaTuple2]) extends ContestScore {
    def this() = this(null)
    import scala.collection.JavaConversions.mapAsScalaMap
    import scala.collection.JavaConversions.mutableMapAsJavaMap
    def competitorScores() = {
      val r = s.map { case (k,v) => new java.lang.Long(k) -> new ExCompetitorScore(v.a,0,v.b) }
      mutableMapAsJavaMap(r)
    }

    def competitorScore(c: lang.Long) : ExCompetitorScore  = {
      val otherTeamsKills = s.filter { case (k,v) => k != c }.foldLeft(0){ case (t,b) => t + b._2.a}
      s.find(_._1 == c).map { case (k,v) => new ExCompetitorScore(v.a,v.a - otherTeamsKills,v.b) }.get
    }
  }

  object ExScoreScheme extends ScoreScheme {

    import  Numeric.Implicits._

    implicit def SubtractTuple[T : Numeric](a: (T,T,T)) = new SubtractingTuple(a)
    class SubtractingTuple[T : Numeric](a: (T,T,T)) {
      def -(b: (T, T, T)) = (a._1 - b._1, a._2 - b._2, a._3 - b._3)
      def +(b: (T, T, T)) = (a._1 + b._1, a._2 + b._2, a._3 + b._3)
    }

    def getContestStoreClass = classOf[ExContestScore]

    def competitorTotalColHeaders() = Array("Kills", "Diff Kills", "Trophys")

    def calculateTotal(contestScores: util.List[CompetitorScore]) = {
      import scala.collection.JavaConversions.asScalaBuffer

      val tot = contestScores.foldLeft((0,0,0)){
        case (t,c:ExCompetitorScore) => t + (c.kills,c.diffKills, c.trophys)
      }

      (ExCompetitorTotal.apply _).tupled(tot)
    }

    def compareField(a: CompetitorTotal, b: CompetitorTotal, field: Int) = 0

    def renderToHtml(cs: ContestScore, competitors: util.Set[lang.Long]) =
      """
        | <b>TJA</b>
      """.stripMargin
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
  case class ExCompetitorTotal(val kills:Int, val diffKills:Int, val trophys:Int) extends CompetitorTotal {
    def getRenderer = new Render {

      def render() = Array(kills.toString,diffKills.toString, trophys.toString)
    }
  }

}
