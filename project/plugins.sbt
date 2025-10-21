ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" % "scala-xml" % VersionScheme.Always
)

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.4.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.18.1")

addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.2.10")

addSbtPlugin("net.nmoncho" % "sbt-dependency-check" % "1.7.1")
