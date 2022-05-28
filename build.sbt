lazy val scala212 = "2.12.15"
lazy val scala213 = "2.13.8"
lazy val supportedScalaVersions = List(scala212, scala213)

organization := "io.github.bpairan"
homepage := Some(url("https://github.com/bpairan/csvmerge"))
scmInfo := Some(ScmInfo(url("https://github.com/bpairan/csvmerge"), "git@github.com:bpairan/csvmerge.git"))
developers := List(Developer("bpairan", "Bharathi Pairan", "barathi@gmail.com", url("https://gitlab.com/bpairan")))
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle := true

scalaVersion := scala213
name := "csvmerge"
version := "1.0.0-SNAPSHOT"

publishTo := Some(
  if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging
)


coverageEnabled := true

crossPaths := true
crossScalaVersions := supportedScalaVersions
// logger dependencies
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.36",
  "org.slf4j" % "slf4j-simple" % "1.7.36"
)
libraryDependencies += "org.typelevel" %% "cats-core" % "2.7.0"
libraryDependencies += "com.github.bigwheel" %% "util-backports" % "2.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % Test

Compile / scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n == 12 => List("-Ypartial-unification")
    case _ => Nil
  }
}

