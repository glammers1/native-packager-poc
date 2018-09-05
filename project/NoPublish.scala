import sbt.Keys._
import sbt.{AutoPlugin, plugins}

/**
  * For projects that are not to be published.
  */
object NoPublish extends AutoPlugin {

  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
}
