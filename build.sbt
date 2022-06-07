lazy val scala212 = "2.12.15"
lazy val scala213 = "2.13.8"
lazy val supportedScalaVersions = List(scala212, scala213)

ThisBuild / scalaVersion := scala213

ThisBuild / organization := "io.github.bpairan"

ThisBuild / organizationName := "bpairan"

ThisBuild / organizationHomepage := Some(url("https://github.com/bpairan/csvmerge"))

ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/bpairan/csvmerge"), "scm:git@github.com:bpairan/csvmerge.git"))

ThisBuild / developers := List(Developer("bpairan", "Bharathi Pairan", "bharathi.pairan@gmail.com", url("https://github.com/bpairan")))

ThisBuild / licenses := Seq(("Apache 2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))

ThisBuild / homepage := Some(url("https://github.com/bpairan/csvmerge"))

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

name := "csvmerge"

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / coverageEnabled := true

ThisBuild / crossPaths := true
ThisBuild / crossScalaVersions := supportedScalaVersions
// logger dependencies
ThisBuild / libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.36",
  "org.slf4j" % "slf4j-simple" % "1.7.36"
)
ThisBuild / libraryDependencies += "org.typelevel" %% "cats-core" % "2.7.0"
ThisBuild / libraryDependencies += "com.github.bigwheel" %% "util-backports" % "2.1"

ThisBuild / libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % Test

ThisBuild / Compile / scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n == 12 => List("-Ypartial-unification")
    case _ => Nil
  }
}
