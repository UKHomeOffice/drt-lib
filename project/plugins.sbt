ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" % "scala-xml" % VersionScheme.Always
)

addSbtPlugin("org.scalameta"          % "sbt-scalafmt"             % "2.6.1")
addSbtPlugin("org.scoverage"          % "sbt-scoverage"            % "2.4.4")
addSbtPlugin("org.portable-scala"     % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.scala-js"           % "sbt-scalajs"              % "1.20.2")
addSbtPlugin("org.wartremover"        % "sbt-wartremover"          % "3.5.7")
addSbtPlugin("com.timushev.sbt"       % "sbt-updates"              % "0.6.4")
addSbtPlugin("com.thesamet"           % "sbt-protoc"               % "1.0.8")
addSbtPlugin("org.johnnei.scapegoat" %% "sbt-scapegoat"            % "1.3.7")
addSbtPlugin("net.nmoncho"            % "sbt-dependency-check"     % "1.9.0")
addSbtPlugin("software.purpledragon"  % "sbt-dependency-lock"      % "1.5.1")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.20"
