ThisBuild / organization := "io.github.bpairan"

ThisBuild / organizationName := "bpairan"

ThisBuild / licenses := Seq(("Apache 2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))

ThisBuild / homepage := Some(url("https://github.com/bpairan/csvmerge"))

ThisBuild / publishTo := sonatypePublishToBundle.value

ThisBuild / versionScheme := Some("early-semver")

ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/bpairan/csvmerge"), "git@github.com:bpairan/csvmerge.git"))

ThisBuild / developers := List(Developer("bpairan", "Bharathi Pairan", "bharathi.pairan@gmail.com", url("https://gitlab.com/bpairan")))

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}