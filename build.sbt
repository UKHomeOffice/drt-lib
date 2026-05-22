import net.nmoncho.sbt.dependencycheck.settings.{ AnalyzerSettings, NvdApiSettings }

ThisBuild / scalaVersion := "2.13.18"
ThisBuild / organization := "uk.gov.homeoffice"
ThisBuild / organizationName := "drt"
ThisBuild / version := "v" + sys.env.getOrElse("DRONE_BUILD_NUMBER", sys.env.getOrElse("BUILD_ID", "DEV"))

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")

val artifactory = "https://artifactory.digital.homeoffice.gov.uk/"
val artifactoryResolvers = Seq(
  "Artifactory Snapshot Realm" at s"${artifactory}artifactory/libs-snapshot/",
  "Artifactory Release Realm" at s"${artifactory}artifactory/libs-release/"
)

lazy val crossSettings =
  CodeCoverageSettings.codeCoverageSettings ++
    SbtUpdatesSettings.sbtUpdatesSettings ++
    WartRemoverSettings.wartRemoverSettings ++
    Seq(
      name := "drt-lib",
      libraryDependencies ++= AppDependencies.shared,
      resolvers ++= artifactoryResolvers
    )

lazy val jvmSettings = Seq(
  libraryDependencies ++= AppDependencies.jvm,
  ThisBuild / dependencyCheckAnalyzers := dependencyCheckAnalyzers.value.copy(
    ossIndex = AnalyzerSettings.OssIndex(
      enabled = Some(false),
      url = None,
      batchSize = None,
      requestDelay = None,
      useCache = None,
      warnOnlyOnRemoteErrors = None,
      username = None,
      password = None
    )
  ),
  Compile / PB.targets := Seq(scalapb.gen() -> (Compile / sourceManaged).value),
  Compile / PB.protoSources := Seq(file("proto/src/main/protobuf")),
  publishTo := Some("release" at artifactory + "artifactory/libs-release")
)

lazy val root = project.in(file(".")).
  aggregate(cross.js, cross.jvm).
  settings(
    name := "drt-lib",
    publish := {},
    publishLocal := {},
    crossScalaVersions := Nil,
    logLevel := Level.Debug
  )

lazy val cross = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .settings(crossSettings)
  .jvmSettings(jvmSettings)
  .jsSettings(
    publishTo := Some("release" at artifactory + "artifactory/libs-release")
  )

val nvdAPIKey = sys.env.getOrElse("NVD_API_KEY", "")

ThisBuild / dependencyCheckNvdApi := NvdApiSettings(apiKey = nvdAPIKey)
