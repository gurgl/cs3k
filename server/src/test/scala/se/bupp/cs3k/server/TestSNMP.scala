package se.bupp.cs3k.server

import org.specs2.mutable.Specification
import io.Source
import java.net.{URL, URI}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
class TestSNMP extends Specification {

  args(skipAll=true)
  "should return my address" should {

    "handle conversions" in {

      val stackOverflowURL = "http://automation.whatismyip.com/n09230945.asp"
      val requestProperties = Map(
        "User-Agent" -> "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:12.0) Gecko/20100101 Firefox/12.0"
      )
      val connection = new URL(stackOverflowURL).openConnection
      requestProperties.foreach({
        case (name, value) => connection.setRequestProperty(name, value)
      })

      var response = Source.fromInputStream(connection.getInputStream).getLines.mkString("\n")
      //var respons: String = Source.fromURL("http://automation.whatismyip.com/n09230945.asp").getLines().mkString("\\n")

      response.shouldEqual("81.235.61.31")

    }
  }
}
