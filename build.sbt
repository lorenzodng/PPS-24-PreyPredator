ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "EcosystemSimulation",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.21",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    )
  )
