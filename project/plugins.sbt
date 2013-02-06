import sbt._

import Defaults._

resolvers +=  Resolver.file("my-test-repo", file(Path.userHome.asFile.toURI.toURL + "/.ivy2/local"))(Resolver.ivyStylePatterns)

//resolvers +=  Resolver.file("my-test-repo", file("file://" + Path.userHome.absolutePath + "/.ivy2/"))(Resolver.ivyStylePatterns);

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/public/"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies <++= (scalaVersion, sbtVersion) {
	case (scalaVersion, sbtVersion @ "0.11.0") => Seq(sbtPluginExtra("com.github.siasia" % "xsbt-web-plugin" % "0.2.11.1", "0.12.1", scalaVersion ))
	case (scalaVersion, sbtVersion @ "0.12.2") => Seq("com.github.siasia" %%"xsbt-web-plugin" % "0.12.0-0.2.11.1")
	case _ => Seq();
}

//libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % "0.12.1-0.2.11.1"))

//addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.6.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0-SNAPSHOT")

addSbtPlugin("de.djini" % "xsbt-webstart" % "0.0.5")