organization := "com.bpairan"
scalaVersion := "2.13.8"
version := "1.0.0-SNAPSHOT"

coverageEnabled := true

// logger dependencies
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.36",
  "org.slf4j" % "slf4j-simple" % "1.7.36"
)
libraryDependencies += "org.typelevel" %% "cats-core" % "2.7.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"