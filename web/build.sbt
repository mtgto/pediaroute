name := "pediaroute"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
//  "net.mtgto" %% "pediaroute-core" % "0.1.0-SNAPSHOT"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(core)
  .dependsOn(core)

lazy val core = (project in file("core"))

