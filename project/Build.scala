import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.Docker
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.Universal
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

import scala.util.Properties

object Build extends AutoPlugin {

  override def requires: Plugins = JvmPlugin && DockerPlugin

  override def trigger: PluginTrigger = allRequirements

  lazy val jvmSettings: Seq[String] = Seq(
    "-XX:+UnlockExperimentalVMOptions",
    "-XX:+UseCGroupMemoryLimitForHeap",
    "-XX:MaxRAMFraction=2",
    "-XX:MaxMetaspaceSize=100M",
    "-XX:+PrintCommandLineFlags"
  )

  lazy val dockerSettings: Seq[Setting[_]] = Seq(
    maintainer in Docker := "FOO",
    dockerBaseImage := "openjdk:8u181-jre-slim",
    dockerRepository := Some("hub.docker.com"),
    dockerExposedVolumes in Docker := Seq("/opt/docker/logs"),
    dockerUpdateLatest :=
      Properties
        .envOrNone("GIT_BRANCH")
        .exists(_.contains("master") && version(!_.contains("SNAPSHOT")).value),
    version in Docker := version.value,
    daemonUser in Docker := "root",
    javaOptions in Universal ++= jvmSettings
  )

  lazy val localJvmSettings: Seq[String] =
    Seq("-Xms384M", "-Xmx384M", "-XX:MaxMetaspaceSize=100M", "-XX:+PrintCommandLineFlags")

  override def projectSettings: Seq[Setting[_]] =
    Vector(
      organization in ThisBuild := "native.packager.poc",
      name in ThisBuild := "native-packager-poc",
      scalaVersion in ThisBuild := Version.Scala.ScalaVersion,
      crossScalaVersions in ThisBuild := Vector(scalaVersion.value),
      crossVersion in ThisBuild := CrossVersion.binary,
      javacOptions in ThisBuild := Seq("-g:none"),
      javaOptions in ThisBuild ++= localJvmSettings,
      fork in run in ThisBuild := true,
      fork in Test in ThisBuild := true,
      scalacOptions in ThisBuild ++= Vector(
        "-deprecation", // Emit warning and location for usages of deprecated APIs.
        "-encoding",
        "utf-8", // Specify character encoding used by source files.
        "-explaintypes", // Explain type errors in more detail.
        "-feature", // Emit warning and location for usages of features that should be imported explicitly.
        "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
        "-language:experimental.macros", // Allow macro definition (besides implementation and application)
        "-language:higherKinds", // Allow higher-kinded types
        "-language:implicitConversions", // Allow definition of implicit functions called views
        "-unchecked", // Enable additional warnings where generated code depends on assumptions.
        "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
        "-Xfatal-warnings", // Fail the compilation if there are any warnings.
        "-Xfuture", // Turn on future language features.
        "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
        "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
        "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
        "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
        "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
        "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
        "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
        "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
        "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
        "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
        "-Xlint:option-implicit", // Option.apply used implicit view.
        "-Xlint:package-object-classes", // Class or object defined in package object.
        "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
        "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
        "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
        "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
        "-Xlint:unsound-match", // Pattern match may not be typesafe.
        "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
        "-Ypartial-unification", // Enable partial unification in type constructor inference
        "-Ywarn-dead-code", // Warn when dead code is identified.
        "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
        "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
        "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
        "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
        "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
        "-Ywarn-numeric-widen", // Warn when numerics are widened.
        "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
        "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
        "-Ywarn-unused:locals", // Warn if a local definition is unused.
        "-Ywarn-unused:params", // Warn if a value parameter is unused.
        "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
        "-Ywarn-unused:privates", // Warn if a private member is unused.
        //"-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
      ),
      // note that the REPL can’t really cope with -Ywarn-unused:imports or -Xfatal-warnings so you should turn them off for the console
      scalacOptions in (Compile, console) ~= (_.filterNot(
        Set(
          "-Xfatal-warnings",
          "-Ywarn-unused:imports"
        ))),
      updateOptions in ThisBuild := updateOptions.value.withCachedResolution(
        cachedResoluton = false),
      // do not build and publish scaladocs
      publishArtifact in (Compile, packageDoc) in ThisBuild := false,
      // show full stack traces and test case durations
      testOptions in Test in ThisBuild += Tests.Argument("-oDF"),
      // -v Log "test run started" / "test started" / "test run finished" events on log level "info" instead of "debug"
      // -a Show stack traces and exception class name for AssertionErrors
      testOptions in Test in ThisBuild += Tests.Argument(TestFrameworks.JUnit, "-v", "-a"),
      unmanagedSourceDirectories.in(Compile) in ThisBuild := Vector(scalaSource.in(Compile).value),
      unmanagedSourceDirectories.in(Test) in ThisBuild := Vector(scalaSource.in(Test).value),
      // scalafmt settings
      scalafmtOnCompile in ThisBuild := true
    )
}
