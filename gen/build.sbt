// -*- scala -*-
name := "pediaroute-gen"

organization := "net.mtgto"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies := Seq(
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "org.slf4j" % "slf4j-nop" % "1.7.7",
  "mysql" % "mysql-connector-java" % "5.1.34",
  "org.specs2" %% "specs2-core" % "2.4.15" % "test",
  "org.mockito" % "mockito-all" % "1.10.8" % "test"
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-feature", "-encoding", "UTF8")

javaOptions in run += "-Xmx4G"

fork in test := true

