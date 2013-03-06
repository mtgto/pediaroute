// -*- scala -*-
name := "pediaroute-gen"

organization := "net.mtgto"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.0"

libraryDependencies := Seq(
  "com.typesafe.slick" %% "slick" % "1.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "mysql" % "mysql-connector-java" % "5.1.23",
  "org.specs2" %% "specs2" % "1.14" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test"
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-feature", "-encoding", "UTF8")

fork in test := true