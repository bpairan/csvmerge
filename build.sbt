lazy val scala212 = "2.12.15"
lazy val scala213 = "2.13.8"
lazy val supportedScalaVersions = List(scala212, scala213)

organization := "com.bpairan"
scalaVersion := scala213
version := "1.0.0-SNAPSHOT"

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

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n == 12 =>
      List(compilerPlugin("com.github.bigwheel" % "util-backports" % "2.1" cross CrossVersion.binary))
    case _ => Nil
  }
}

Compile / scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n == 12 => List("-Ypartial-unification")
    case _ => Nil
  }
}

