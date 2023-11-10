import sbt.Keys.libraryDependencies

lazy val scala = "2.13.11"

ThisBuild / scalaVersion := scala
ThisBuild / organization := "uk.gov.homeoffice"
ThisBuild / organizationName := "drt"
ThisBuild / version := "v" + sys.env.getOrElse("DRONE_BUILD_NUMBER", sys.env.getOrElse("BUILD_ID", "DEV"))

val artifactory = "https://artifactory.digital.homeoffice.gov.uk/"

lazy val root = project.in(file(".")).
  aggregate(cross.js, cross.jvm).
  settings(
    name := "drt-lib",
    publish := {},
    publishLocal := {},
    crossScalaVersions := Nil,
    logLevel := Level.Debug
  )

lazy val akkaVersion = "2.7.0"
lazy val akkaPersistenceInMemoryVersion = "2.5.15.2"
lazy val jodaVersion = "2.12.5"
lazy val upickleVersion = "3.1.0"
lazy val sparkMlLibVersion = "3.4.1"
lazy val sslConfigCore = "0.6.1"
lazy val scalaTestVersion = "3.2.16"
lazy val autowireVersion = "0.3.3"
lazy val booPickleVersion = "1.3.3"
lazy val specs2 = "4.20.0"
lazy val csvCommonsVersion = "1.10.0"
lazy val catsVersion = "2.9.0"
lazy val slickVersion = "3.4.1"
lazy val postgresqlVersion = "42.6.0"

lazy val cross = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .settings(
    name := "drt-lib",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
      "com.lihaoyi" %% "upickle" % upickleVersion,
      "com.lihaoyi" %% "autowire" % autowireVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "org.specs2" %% "specs2-core" % specs2 % Test,
      "org.apache.commons" % "commons-csv" % csvCommonsVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "com.outr" %% "scribe-slf4j" % "3.11.5"
    ),
    resolvers ++= Seq(
      "Artifactory Snapshot Realm" at "https://artifactory.digital.homeoffice.gov.uk/artifactory/libs-snapshot/",
      "Artifactory Release Realm" at "https://artifactory.digital.homeoffice.gov.uk/artifactory/libs-release/"
    )
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "joda-time" % "joda-time" % jodaVersion,
      "org.apache.spark" %% "spark-mllib" % sparkMlLibVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
      "com.github.dnvriend" %% "akka-persistence-inmemory" % akkaPersistenceInMemoryVersion % "test",
      "com.typesafe" %% "ssl-config-core" % sslConfigCore,
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "org.postgresql" % "postgresql" % postgresqlVersion,
      "com.h2database" % "h2" % "2.2.220" % Test
    ),
    Compile / PB.targets := Seq(scalapb.gen() -> (Compile / sourceManaged).value),
    Compile / PB.protoSources := Seq(file("proto/src/main/protobuf")),
    publishTo := Some("release" at artifactory + "artifactory/libs-release")
  ).
  jsSettings(
    publishTo := Some("release" at artifactory + "artifactory/libs-release")
  )
