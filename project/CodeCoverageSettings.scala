import sbt.*
import sbt.Keys.*
import scoverage.ScoverageKeys.coverageExcludedPackages

object CodeCoverageSettings {
  val codeCoverageSettings: Seq[Def.Setting[?]] = Seq(
    Test / parallelExecution := false,
    Test / javaOptions += "-Duser.timezone=UTC",
    Test / testOptions ++= Seq(
      Tests.Argument(
        TestFrameworks.ScalaTest,
        "-u",
        ((Test / target).value / "test-reports").toString
      ),
      Tests.Argument(
        TestFrameworks.Specs2,
        "junitxml",
        s"junitxml.outdir=${(Test / target).value / "test-reports"}"
      )
    ),
    coverageExcludedPackages := "<empty>;.*Main.*"
  )
}
