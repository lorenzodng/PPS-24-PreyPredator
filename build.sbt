ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "EcosystemSimulation",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.19",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),

    assembly / mainClass := Some("controller.Simulation"),

    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )
