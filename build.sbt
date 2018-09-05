import Build._
import sbt.Keys._

lazy val `native-packager-poc` = project
  .in(file("."))
  .enablePlugins(NoPublish)
  .aggregate(
    `native-packager-poc-server`
  )

lazy val `native-packager-poc-server` =
  project
    .enablePlugins(NoPublish, AshScriptPlugin)
    .settings(
      dockerSettings,
      libraryDependencies ++= Seq(
       
      )
    )
