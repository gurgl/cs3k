name := "scala-wicket1"

version := "0.1"


libraryDependencies ++= Seq(
    "junit" % "junit" % "4.10" % "test",
    "org.scalatest" %% "scalatest" % "1.8" % "test" exclude("org.eclipse.jetty", "jetty"),
    "org.mockito" % "mockito-all" % "1.9.0" % "test" exclude("org.eclipse.jetty", "jetty"),
    "org.apache.wicket" % "wicket-native-websocket-jetty" % "0.1-SNAPSHOT",
//    "org.apache.wicket" % "wicket" % "6.0.0-beta2" exclude("org.eclipse.jetty", "jetty"),
    "com.typesafe.akka" % "akka-actor" % "2.0.2" exclude("org.eclipse.jetty", "jetty"),
"org.slf4j" % "slf4j-log4j12" % "1.6.4",
"log4j" % "log4j" % "1.2.16", 
	"javax.servlet" % "servlet-api" % "2.5" % "provided",
	"org.eclipse.jetty.aggregate" % "jetty-all-server" % "8.1.0.v20120127" % "container"	
)




resolvers += "eclipse" at "http://mirror.csclub.uwaterloo.ca/eclipse/rt/eclipselink/maven.repo/"

resolvers += "more eclipse" at "http://www.eclipse.org/downloads/download.php?r=1&nf=1&file=/rt/eclipselink/maven.repo"

//resolvers += "blaha" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "more apache" at "http://repository.apache.org/snapshots/"




scalacOptions += "-deprecation"

seq(webSettings :_*)



resourceDirectory in Compile <<= baseDirectory(_ / "src/main/scala") 


//    "org.eclipse.jetty" % "jetty-webapp" % "7.5.4.v20111024" % "container",
//    "org.eclipse.jetty" % "jetty-websocket" % "7.5.4.v20111024" % "container",
//    "org.eclipse.jetty" % "jetty-server" % "7.5.4.v20111024" % "container",
// "org.eclipse.jetty" % "jetty-webapp" % "7.6.4.v20120524" % "container" exclude("org.eclipse.jetty.orbit", "javax.servlet"),
//    "org.eclipse.jetty" % "jetty-websocket" % "7.6.4.v20120524" % "container" exclude("org.eclipse.jetty.orbit", "javax.servlet"),
//    "org.eclipse.jetty" % "jetty-server" % "7.6.4.v20120524" % "container" exclude("org.eclipse.jetty.orbit", "javax.servlet"),
//    "org.eclipse.jetty" % "jetty-webapp" % "7.6.0.v20120127" % "container",
//  "org.eclipse.jetty" % "jetty-server" % "8.1.2.v20120308" % "container"
// 	"org.eclipse.jetty.aggregate" % "jetty-all-server" % "8.0.4.v20111024" % "container"
    //"org.eclipse.jetty.aggregate" % "jetty-all-server" % "7.6.4.v20120524" % "container"
//"javax.servlet" % "servlet-api" % "2.5" % "provided",
//	"org.eclipse.jetty" % "jetty-server" % "7.6.4.v20120524" % "container",
//	"org.eclipse.jetty" % "jetty-webapp" % "7.6.4.v20120524" % "container"    
