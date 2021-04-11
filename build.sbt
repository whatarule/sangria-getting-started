import Dependencies._

ThisBuild / scalaVersion     := "2.13.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "halo",
    libraryDependencies += "org.sangria-graphql" %% "sangria" % "2.1.0",
    libraryDependencies += "org.sangria-graphql" %% "sangria-circe" % "1.3.1",
  )

