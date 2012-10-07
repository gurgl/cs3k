
resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/public/"

resolvers +=  Resolver.file("my-test-repo", file(Path.userHome.asFile.toURI.toURL + "/.ivy2/local"))(Resolver.ivyStylePatterns)
//resolvers +=  Resolver.file("my-test-repo", file("file://" + Path.userHome.absolutePath + "/.ivy2/"))(Resolver.ivyStylePatterns)


libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.11.1"))

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.6.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0")

addSbtPlugin("de.djini" % "xsbt-webstart" % "0.0.5")