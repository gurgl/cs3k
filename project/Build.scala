import com.github.siasia.{PluginKeys, Deployment, Container, WebPlugin}
import com.github.siasia.PluginKeys._
import sbt._
import Keys._
import com.github.siasia.WebPlugin._
import com.github.siasia.WebappPlugin._

import WebStartPlugin._

object MyBuild extends Build {

  lazy val root = Project(id = "hello",
    base = file(".")) aggregate(serverProject, lobbyProject)

  lazy val serverProject = Project(
    "cs3k-server",
    file("server"),
    settings = Project.defaultSettings ++ webSettings ++ serverSettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ) aggregate(lobbyProject,commonProject) dependsOn(commonProject)

  //val Start = config()

  lazy val serverSettings = Seq(
    name := "cs3k Server",
    version := "0.1",
    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.10" % "test",
      "org.scalatest" %% "scalatest" % "1.8" % "test" exclude("org.eclipse.jetty", "jetty"),
      //"org.mockito" % "mockito-all" % "1.9.0" % "test" exclude("org.eclipse.jetty", "jetty"),
      "org.apache.wicket" % "wicket-native-websocket-jetty" % "0.1-SNAPSHOT",
      //    "org.apache.wicket" % "wicket" % "6.0.0-beta2" exclude("org.eclipse.jetty", "jetty"),
      //"com.typesafe.akka" % "akka-actor" % "2.0.2" exclude("org.eclipse.jetty", "jetty"),
      "org.slf4j" % "slf4j-log4j12" % "1.6.4",
      "log4j" % "log4j" % "1.2.16",
      "javax.servlet" % "servlet-api" % "2.5" % "provided",
      "org.ow2.asm" % "asm" % "4.0",
      "org.objenesis" % "objenesis" % "1.2",
      "org.eclipse.jetty.aggregate" % "jetty-server" % "8.1.0.v20120127" % "container",
      "org.eclipse.jetty.aggregate" % "jetty-webapp" % "8.1.0.v20120127" % "container"
  ),
    resolvers ++= Seq("eclipse" at "http://mirror.csclub.uwaterloo.ca/eclipse/rt/eclipselink/maven.repo/",
      "more eclipse" at "http://www.eclipse.org/downloads/download.php?r=1&nf=1&file=/rt/eclipselink/maven.repo",
      //resolvers += "blaha" at "http://repo.typesafe.com/typesafe/releases/"
      "more apache" at "http://repository.apache.org/snapshots/"
    ),
    scalacOptions += "-deprecation",
    resourceDirectory in Compile <<= baseDirectory(_ / "src/main/scala"),
    aggregate in Compile := true,
    aggregate in Runtime := false,
    packageWar in Compile <<= (packageWar in Compile, target, webstartBuild.in(lobbyProject)).map {
      (res, target, webstartBuild) => {
        res
      }
    },
    deployment in Compile <<= (deployment  in Compile, target, webstartBuild.in(lobbyProject)).map {
      (res, target, webstartBuild) => {
        res
      }
    }
    /*,
    packageWar in Compile <<= (packageWar in Compile, target, webstartBuild.in(lobbyProject)).map {
        (packa, target, webstartBuild) => {
            System.err.println("Tjaba")

            System.err.println("Tja")
            val newFiles: PathFinder = (webstartBuild * "*")
            val webapp: File = target / "webapp"
            val copyFiles: Seq[(File, File)] = newFiles x (Path rebase(webstartBuild, webapp))
            IO copy copyFiles
            packa
          }


    }*/
     , webappResources in Compile in Compile <+= (webstartOutputDirectory in lobbyProject)(sd => sd )




  )

  /*
         (target, webstartBuild in lobbyProject) map {
      (target, webstartBuild) =>
        System.err.println("Tja")
        () => {
          System.err.println("Tja")
          val newFiles: PathFinder = (webstartBuild * "*")
          val webapp: File = target / "webapp"
          val copyFiles: Seq[(File, File)] = newFiles x (Path rebase(webstartBuild, webapp))
          IO copy copyFiles
        }
    }
   */

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


  lazy val lobbyProject = Project(
    id = "cs3k-lobby",
    base = file("lobby"),
    settings = Project.defaultSettings ++ lobbySettings ++ webStartSettings ++ Seq(
      mappings in(Compile, packageBin) ~= {
        (ms: Seq[(File, String)]) =>
          ms filter {
            case (file, toPath) =>
              !toPath.contains("client")
          }
      }
    )
      //++ addArtifact(Artifact("blaj", "dir"), webstartBuild)
  ) dependsOn(commonProject)

  lazy val lobbySettings =
    Seq(
      resolvers := Seq(),
      libraryDependencies ++= Seq(
        "com.sun" % "javaws" % "1.6.0" from (Path.fileProperty("java.home").asFile / "lib" / "javaws.jar").asURL.toString,
        "org.ow2.asm" % "asm" % "4.0",
        "org.objenesis" % "objenesis" % "1.2"

        //"com.typesafe.akka" % "akka-actor" % "2.0.2" exclude("org.eclipse.jetty", "jetty")
      ),
      name := "cs3k Lobby",
      organization := "se.pearshine",
      version := "1.0"
    )


  lazy val webStartSettings = WebStartPlugin.allSettings ++ Seq(
    webstartGenConf := GenConf(
      dname = "CN=Snake Oil, OU=An Anonymous Hacker, O=Bad Guys Inc., L=Bielefeld, ST=33641, C=DE",
      validity = 365
    ),


    webstartKeyConf := KeyConf(
      keyStore = file("testKeys"),
      storePass = "bobbafett123",
      alias = "jdc",
      keyPass = "bobbafett123"
    ),

    webstartJnlpConf := Seq(JnlpConf(
      mainClass = "se.bupp.cs3k.lobby.LobbyClient",
      fileName = "Test.jnlp",
      codeBase = "http://localhost:8080/",
      title = "My Title",
      vendor = "My Company",
      description = "My Webstart Project",
      iconName = None,
      splashName = None,
      offlineAllowed = true,
      allPermissions = true,
      j2seVersion = "1.6+",
      maxHeapSize = 192

    ))
  )

  lazy val commonProject = Project(
    "cs3k-common",
    file("common"),
    settings = Project.defaultSettings ++ Seq(libraryDependencies ++= Seq(
      "com.sun" % "javaws" % "1.6.0" from (Path.fileProperty("java.home").asFile / "lib" / "javaws.jar").asURL.toString
      //"com.typesafe.akka" % "akka-actor" % "2.0.2" exclude("org.eclipse.jetty", "jetty")
    ))
  )

}