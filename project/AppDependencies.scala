import sbt._

object AppDependencies {
  object Versions {
    val pekko = "1.4.0"
    val pekkoHttp = "1.3.0"
    val slick = "3.5.2"
    val joda = "2.14.0"
    val upickle = "3.3.1"
    val sparkMlLib = "4.1.1"
    val scalaTest = "3.2.19"
    val specs2 = "4.23.0"
    val commonsCsv = "1.14.1"
    val cats = "2.13.0"
    val scribeSlf4j = "3.17.0"
    val h2 = "2.4.240"
  }

  val sharedCompile: Seq[ModuleID] = Seq(
    "com.lihaoyi"          %% "upickle"         % Versions.upickle,
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "org.apache.commons"    % "commons-csv"     % Versions.commonsCsv,
    "org.typelevel"        %% "cats-core"       % Versions.cats,
    "com.outr"             %% "scribe-slf4j"    % Versions.scribeSlf4j
  )

  val sharedTest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest"   % Versions.scalaTest % Test,
    "org.specs2"    %% "specs2-core" % Versions.specs2    % Test
  )

  val shared: Seq[ModuleID] = sharedCompile ++ sharedTest

  val jvmCompile: Seq[ModuleID] = Seq(
    "org.apache.pekko"   %% "pekko-actor"             % Versions.pekko,
    "org.apache.pekko"   %% "pekko-persistence"       % Versions.pekko,
    "org.apache.pekko"   %% "pekko-persistence-query" % Versions.pekko,
    "org.apache.pekko"   %% "pekko-http"              % Versions.pekkoHttp,
    "org.apache.pekko"   %% "pekko-http-spray-json"   % Versions.pekkoHttp,
    "org.apache.pekko"   %% "pekko-slf4j"             % Versions.pekko,
    "joda-time"           % "joda-time"               % Versions.joda,
    "org.apache.spark"   %% "spark-mllib"             % Versions.sparkMlLib,
    "com.typesafe.slick" %% "slick"                   % Versions.slick
  )

  val jvmTest: Seq[ModuleID] = Seq(
    "org.apache.pekko" %% "pekko-testkit"             % Versions.pekko % Test,
    "org.apache.pekko" %% "pekko-persistence-testkit" % Versions.pekko % Test,
    "com.h2database"    % "h2"                        % Versions.h2    % Test
  )

  val jvm: Seq[ModuleID] = jvmCompile ++ jvmTest
}
