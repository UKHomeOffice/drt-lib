import sbt.*

object AppDependencies {
  private val pekkoVersion = "1.4.0"
  private val pekkoHttpVersion = "1.3.0"
  private val slickVersion = "3.5.2"

  private val upickleVersion = "3.3.1"
  private val scalaTestVersion = "3.2.20"
  private val specs2Version = "4.23.0"
  private val commonsCsvVersion = "1.14.1"
  private val catsVersion = "2.13.0"
  private val scribeSlf4jVersion = "3.19.0"

  val sharedCompile: Seq[ModuleID] = Seq(
    "com.lihaoyi"          %% "upickle"         % upickleVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "org.apache.commons"    % "commons-csv"     % commonsCsvVersion,
    "org.typelevel"        %% "cats-core"       % catsVersion,
    "com.outr"             %% "scribe-slf4j"    % scribeSlf4jVersion
  )

  val sharedTest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest"   % scalaTestVersion % Test,
    "org.specs2"    %% "specs2-core" % specs2Version    % Test
  )

  val shared: Seq[ModuleID] = sharedCompile ++ sharedTest

  val jvmCompile: Seq[ModuleID] = Seq(
    "org.apache.pekko"   %% "pekko-actor"             % pekkoVersion,
    "org.apache.pekko"   %% "pekko-persistence"       % pekkoVersion,
    "org.apache.pekko"   %% "pekko-persistence-query" % pekkoVersion,
    "org.apache.pekko"   %% "pekko-http"              % pekkoHttpVersion,
    "org.apache.pekko"   %% "pekko-http-spray-json"   % pekkoHttpVersion,
    "org.apache.pekko"   %% "pekko-slf4j"             % pekkoVersion,
    "joda-time"           % "joda-time"               % "2.14.2",
    "org.apache.spark"   %% "spark-mllib"             % "4.1.1",
    "com.typesafe.slick" %% "slick"                   % slickVersion
  )

  val jvmTest: Seq[ModuleID] = Seq(
    "org.apache.pekko" %% "pekko-testkit"             % pekkoVersion % Test,
    "org.apache.pekko" %% "pekko-persistence-testkit" % pekkoVersion % Test,
    "com.h2database"    % "h2"                        % "2.4.240"    % Test
  )

  val jvm: Seq[ModuleID] = jvmCompile ++ jvmTest
}
