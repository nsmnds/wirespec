ThisBuild / organization := "community.flock.wirespec   "
ThisBuild / organizationName := "Flock."
ThisBuild / organizationHomepage := Some(url("https://flock.community"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/flock-community/wirespec"),
    "scm:git@github.com:flock-community/wirespec.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "Veelenturf",
    name = "Willem Veelenturf",
    email = "willem.veelenturf@flock.community",
    url = url("https://flock.community")
  )
)

ThisBuild / description := "Sbt Kotlin plugin"
ThisBuild / licenses := List(
  "MIT" ->  url("http://opensource.org/licenses/MIT")
)

ThisBuild / homepage := Some(url("https://github.com/flock-community/wirespec"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true