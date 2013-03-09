import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "pediaroute"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
//    "net.mtgto" %% "pediaroute-core" % "0.1.0-SNAPSHOT"
  )

  lazy val core = Project(id = "core", base = file("modules/core")).settings(
    scalaVersion := "2.10.0"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  ).dependsOn(core)

}
