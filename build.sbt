ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / organization := "is.ashley"

ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "lwjglgen",
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.9.7" // set minimum sbt version
      }
    },
    libraryDependencies ++= Seq(
      "org.scalameta"        %% "scalafmt-dynamic" % "3.7.17",
      "org.typelevel"        %% "paiges-core"      % "0.4.3",
      "com.thoughtworks.qdox" % "qdox"             % "2.0.3"
    ),
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
