
resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/public/"

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.11.1"))

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.6.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0")

addSbtPlugin("de.djini" % "xsbt-webstart" % "0.0.5")