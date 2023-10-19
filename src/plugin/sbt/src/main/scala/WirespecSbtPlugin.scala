package community.flock.wirespec

import sbt._
import Keys._

object WirespecSbtPlugin extends AutoPlugin {

  object autoImport {
    val input = settingKey[String]("Input file ")
    val output = taskKey[String]("Output directory ")
    val languages = taskKey[Unit]("Language type")
    val packageName = taskKey[Unit]("Package name")
  }

}
