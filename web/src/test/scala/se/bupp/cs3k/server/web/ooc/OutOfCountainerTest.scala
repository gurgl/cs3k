package se.bupp.cs3k.server.web.ooc

import org.specs2.mutable.{BeforeAfter, Specification}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.apache.wicket.util.tester.WicketTester
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{BasicCookieStore, DefaultHttpClient, HttpClients}
import java.util.Scanner
import java.sql.{ResultSet, PreparedStatement, Connection, DriverManager}
import scala.io.Source
import org.slf4j.{LoggerFactory, Logger}
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.params.BasicHttpParams
import org.apache.http.client.protocol.ClientContext
import org.apache.http.Header
import org.apache.http.message.BasicHeader

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-10-06
 * Time: 00:05
 * To change this template use File | Settings | File Templates.
 */
class OutOfCountainerTest extends Specification {

  private val logger = LoggerFactory.getLogger(classOf[OutOfCountainerTest])

  "Other" should {
    "tmp" in {
      val s= """ <a id="link8" href="#"><span>blubb</span></a> """


    }
  }
  "Out of container" should {
    "handle tournament start" in new BeforeAfter {

      private var server:Server = _

      def before = {


        server = new Server(8990)
        val context = new WebAppContext();

        val path = "web/src/main/webapp/";
        context.setDescriptor(path + "WEB-INF/web.xml");

        context.setResourceBase(path);
        context.setContextPath("/");
        context.setParentLoaderPriority(true);

        server.setHandler(context);

        server.start();
        while(!(server.isStarted() && server.isRunning())) {
          Thread.sleep(100L)
        }
        println("yo")
        //server.join();
      }


      def after = {
        server.stop()
      }


      //val connection = DriverManager.getConnection("jdbc:hsqldb:file:testdb","SA", "")
      //private val lines: Iterator[String] = Source.fromFile("C:\\Users\\karlw\\Documents\\src\\cs3k\\saved-mydb.script").getLines()
      /*
      connection.prepareStatement("DROP SCHEMA PUBLIC CASCADE").execute()
      //connection.prepareStatement("CREATE SCHEMA PUBLIC AUTHORIZATION DBA").execute()
      lines.foreach { l =>
        if(!l.startsWith("CREATE USER") && !l.startsWith("CREATE SCHEMA PUBLIC") && !l.startsWith("GRANT DBA TO SA")) {
          val statement: PreparedStatement = connection.prepareStatement(l)
          statement.execute()
        }

      }*/
      val httpclient = HttpClients.createDefault();
      val get = new HttpGet("http://localhost:8990")

      //val httpclient = new DefaultHttpClient();
      val  localContext = new BasicHttpContext();

      // HTTP parameters stores header etc.
      //val params = new BasicHttpParams();
      //params.setParameter("http.protocol.handle-redirects",false);

      // Create a local instance of cookie store
      val cookieStore = new BasicCookieStore();

      // Bind custom cookie store to the local context
      localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

      // connect and receive
      val httpget = new HttpGet("http://localhost:8990");
      //httpget.setParams(params);
      val response = httpclient.execute(httpget, localContext);

      // obtain redirect target
      val locationHeader = response.getFirstHeader("location");
      if (locationHeader != null) {
        val redirectLocation = locationHeader.getValue();
        logger.info("loaction: " + redirectLocation);
      } else {
        // The response is invalid and did not provide the new location for
        // the resource.  Report an error or possibly handle the response
        // like a 404 Not Found error.
        logger.info("not a redirect")
      }


      //private val response: CloseableHttpResponse = httpclient.execute(get)



      logger.info(new Scanner(response.getEntity.getContent).useDelimiter("\\A").next)
      logger.info(response.getAllHeaders.toList.mkString("\n"))

      val requestBlubbQuery = "?0-1.IBehaviorListener.0-contentContainer-content-openCompetitions-table-body-rows-2-cells-1-cell-link&_=1382916805434"
      import scala.collection.JavaConversions.asScalaBuffer
      logger.info("cookie store" + cookieStore.getCookies.toList)
      val httpget2 = new HttpGet("http://localhost:8990/;jsessionid=" + cookieStore.getCookies.toList.find(_.getName == "JSESSIONID").get.getValue + requestBlubbQuery );
      httpget2.setHeader(new BasicHeader("Referer","http://localhost:8080/?0"))
      httpget2.setHeader(new BasicHeader("Wicket-Ajax","true"))
      httpget2.setHeader(new BasicHeader("Wicket-Ajax-BaseURL","."))
      httpget2.setHeader(new BasicHeader("X-Requested-With","XMLHttpRequest"))


      val response2 = httpclient.execute(httpget2,localContext)
      logger.info(new Scanner(response2.getEntity.getContent).useDelimiter("\\A").next)
      logger.info(response2.getAllHeaders.toList.mkString("\n"))

      private val startTournamentQueryString = "?0-1.IBehaviorListener.0-contentContainer-content-breadCrumbPanel-contentContainer-content-tab~panel-contentContainer-content-startGameDebug&_=1382916805634"

      val httpget3 = new HttpGet("http://localhost:8990/" + startTournamentQueryString);
      httpget3.setHeader(new BasicHeader("Referer","http://localhost:8080/?0"))
      httpget3.setHeader(new BasicHeader("Wicket-Ajax","true"))
      httpget3.setHeader(new BasicHeader("Wicket-Ajax-BaseURL","."))
      httpget3.setHeader(new BasicHeader("X-Requested-With","XMLHttpRequest"))
      val response3 = httpclient.execute(httpget3,localContext)
      logger.info(new Scanner(response3.getEntity.getContent).useDelimiter("\\A").next)
      logger.info(response3.getAllHeaders.toList.mkString("\n"))


      var connection:Connection = null
      private var resultSet:ResultSet = null
      private var state:String = null
      try {
        connection  = DriverManager.getConnection("jdbc:hsqldb:file:web/src/test/resources/testtourdb/mydb","SA", "")
        resultSet = connection.prepareStatement("SELECT * FROM competition where name='blubb'").executeQuery()
        resultSet.next()
        state = resultSet.getString("state")
      } finally {
        resultSet.close()
        connection.close()
      }
      state === "RUNNING"



1 === 1
}
}
}
