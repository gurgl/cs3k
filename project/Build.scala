import com.github.siasia.{PluginKeys, Deployment, Container, WebPlugin}
import com.github.siasia.PluginKeys._
import sbt._
import Keys._
import com.github.siasia.WebPlugin._
import com.github.siasia.WebappPlugin._

import WebStartPlugin._

object MyBuild extends Build {

  val WICKET_VERSION = "6.2.0"


  override def settings = super.settings ++ Seq(scalaVersion := "2.10.0")

  lazy val root = Project(id = "root",
    base = file("."), settings = defaultSettings) aggregate(serverProject, lobbyProject)

  lazy val defaultSettings = Defaults.defaultSettings ++ Seq(scalaVersion := "2.10.0"
    //, publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
  )

  lazy val serverProject = Project(
    "server",
    file("server"),
    settings = defaultSettings ++ Seq(
      unmanagedClasspath in Runtime <+= (baseDirectory) map { bd => Attributed.blank(bd) }
    ) ++ serverSettings
      //++ net.virtualvoid.sbt.graph.Plugin.graphSettings
  ) dependsOn(commonProject, apiProject) aggregate(lobbyProject,commonProject)



  //val Start = config()

  lazy val serverSettings = Seq(
    name := "cs3k Server",
    version := "0.1",
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-log4j12" % "1.6.4",
      "log4j" % "log4j" % "1.2.16",
      "javax.servlet" % "servlet-api" % "2.5" % "provided",
      "org.apache.commons" % "commons-exec" % "1.1"
    ) ++ Seq(
      "org.springframework" % "spring-core" % "3.1.2.RELEASE",
      "org.springframework" % "spring-context" % "3.1.2.RELEASE",
      "org.springframework" % "spring-web" % "3.1.2.RELEASE",
      "org.springframework" % "spring-tx" % "3.1.2.RELEASE",
      "org.springframework" % "spring-asm" % "3.1.2.RELEASE",
      "org.springframework" % "spring-orm" % "3.1.2.RELEASE",
      "org.springframework" % "spring-beans" % "3.1.2.RELEASE",
      "org.codehaus.fabric3.api" % "javax-jta" % "1.1.0",
      "org.hibernate" % "hibernate-core" % "4.1.7.Final",
      "org.hibernate" % "hibernate-entitymanager" % "4.1.7.Final",
      "org.hibernate.javax.persistence" % "hibernate-jpa-2.0-api" % "1.0.1.Final",
      "org.apache.wicket" % "wicket-bootstrap" % "0.7",
      "org.apache.wicket" % "wicket-spring" % WICKET_VERSION, //exclude("org.apache.wicket","wicket-ioc"),
      "org.wicketstuff" % "wicketstuff-inmethod-grid" % WICKET_VERSION exclude("org.apache.wicket","wicket-core"),
      "com.fasterxml.jackson.core" % "jackson-core" % "2.1.0",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.0",
      "org.hsqldb" % "hsqldb" % "2.2.9"
    ) ++ Seq(
      "junit" % "junit" % "4.10" % "test",
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "org.specs2" %% "specs2" % "1.12.3" % "test",
      "com.fasterxml.jackson.module" % "jackson-module-scala" % "2.1.0" % "test"
  ),
    resolvers ++= Seq("eclipse" at "http://mirror.csclub.uwaterloo.ca/eclipse/rt/eclipselink/maven.repo/",
      "sonatype-snap" at "http://oss.sonatype.org/content/repositories/snapshots",
      "more apache" at "http://repository.apache.org/snapshots/"
    ),
    scalacOptions += "-deprecation"
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

  )

  lazy val webProject = Project(
    "web",
    file("web"),
    settings = defaultSettings ++ webSettings ++ Seq(
      unmanagedClasspath in Runtime <+= (baseDirectory) map { bd => Attributed.blank(bd) }
    ) ++ webModSettings
    //++ net.virtualvoid.sbt.graph.Plugin.graphSettings
  ) dependsOn(serverProject, apiProject) aggregate(serverProject)

  lazy val webModSettings = Seq(
    name := "cs3k Web",
    version := "0.1",
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-log4j12" % "1.6.4",
      "log4j" % "log4j" % "1.2.16",
      "javax.servlet" % "servlet-api" % "2.5" % "provided",
      "org.eclipse.jetty.aggregate" % "jetty-server" % "8.1.0.v20120127" % "container",
      "org.eclipse.jetty.aggregate" % "jetty-webapp" % "8.1.0.v20120127" % "container",
      "org.apache.commons" % "commons-exec" % "1.1"
    ) ++ Seq(
      "org.springframework" % "spring-core" % "3.1.2.RELEASE",
      "org.springframework" % "spring-context" % "3.1.2.RELEASE",
      "org.springframework" % "spring-web" % "3.1.2.RELEASE",
      "org.springframework" % "spring-tx" % "3.1.2.RELEASE",
      "org.springframework" % "spring-asm" % "3.1.2.RELEASE",
      "org.springframework" % "spring-orm" % "3.1.2.RELEASE",
      "org.springframework" % "spring-beans" % "3.1.2.RELEASE",
      "org.codehaus.fabric3.api" % "javax-jta" % "1.1.0",
      "org.hibernate" % "hibernate-core" % "4.1.7.Final",
      "org.hibernate" % "hibernate-entitymanager" % "4.1.7.Final",
      "org.hibernate.javax.persistence" % "hibernate-jpa-2.0-api" % "1.0.1.Final",
      "org.apache.wicket" % "wicket-bootstrap" % "0.7",
      "org.apache.wicket" % "wicket-spring" % WICKET_VERSION, //exclude("org.apache.wicket","wicket-ioc"),
      "org.wicketstuff" % "wicketstuff-inmethod-grid" % WICKET_VERSION exclude("org.apache.wicket","wicket-core"),
      "com.fasterxml.jackson.core" % "jackson-core" % "2.1.0",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.0",
      "org.hsqldb" % "hsqldb" % "2.2.9"
    ) ++ Seq(
      "junit" % "junit" % "4.10" % "test",
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "org.specs2" %% "specs2" % "1.12.3" % "test",
      "com.fasterxml.jackson.module" % "jackson-module-scala" % "2.1.0" % "test"
    ),
    resolvers ++= Seq("eclipse" at "http://mirror.csclub.uwaterloo.ca/eclipse/rt/eclipselink/maven.repo/",
      "sonatype-snap" at "http://oss.sonatype.org/content/repositories/snapshots",
      "more apache" at "http://repository.apache.org/snapshots/"
    ),
    scalacOptions += "-deprecation",
    unmanagedResourceDirectories in Compile <<= baseDirectory( bd => Seq(bd / "src/main/scala", bd / "src/main/resources")),
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
    },
    webappResources in Compile in Compile <+= (webstartOutputDirectory in lobbyProject)(sd => sd )
  )




  lazy val lobbyProject = Project(
    id = "lobby",
    base = file("lobby"),
    settings = defaultSettings ++ lobbySettings ++ webStartSettings ++ Seq(
      mappings in(Compile, packageBin) ~= {
        (ms: Seq[(File, String)]) =>
          ms filter {
            case (file, toPath) =>
              !toPath.contains("client")
          }
      }
    )
  ) dependsOn(commonProject)

  lazy val lobbySettings =
    Seq(
      resolvers := Seq(),
      libraryDependencies ++= Seq(
        "com.intellij" % "forms_rt" % "7.0.3",
        "com.sun" % "javaws" % "1.6.0" from (Path.fileProperty("java.home").asFile / "lib" / "javaws.jar").asURL.toString
      ),
      name := "cs3k Lobby",
      organization := "se.pearshine.cs3k",
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
      fileName = "lobbyX.jnlp",
      codeBase = "http://localhost:8080/",
      title = "CS3K Lobby",
      vendor          = "PäronGlans",
      description     = "Multiplayer game",
      iconName = None,
      splashName = None,
      offlineAllowed = true,
      allPermissions = false,
      j2seVersion = "1.6",
      maxHeapSize = 192

    ))
  )

  lazy val commonProject = Project(
    "common",
    file("common"),
    settings = defaultSettings ++ Seq(
      javacOptions ++= Seq("-source", "1.6"),
      compileOrder := CompileOrder.JavaThenScala,
      libraryDependencies ++= Seq(
      "com.esotericsoftware.kryo" % "kryo" % "2.20" classifier "shaded" exclude("org.ow2.asm", "asm"),
      "com.sun" % "javaws" % "1.6.0" from (Path.fileProperty("java.home").asFile / "lib" / "javaws.jar").asURL.toString
  //"com.typesafe.akka" % "akka-actor" % "2.0.2" exclude("org.eclipse.jetty", "jetty")
    ))
  )

  lazy val apiProject = Project(
    "api",
    file("api"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-core" % "2.1.0",
      "com.fasterxml.jackson.core" % "jackson-annotations" % "2.1.0",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.0"
      //"com.typesafe.akka" % "akka-actor" % "2.0.2" exclude("org.eclipse.jetty", "jetty")
    )) ++ Seq(
      //publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository"))),
      //name := 'My Project'
      organization := "se.paronglans.cs3k",
      version := "0.3-SNAPSHOT"
    )

  )

}