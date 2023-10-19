name := "sbt-wirespec-plugin"

organization := "community.flock.wirespec"

version := System.getenv.getOrDefault("VERSION", "0.0.0-SNAPSHOT")

sbtPlugin := true
