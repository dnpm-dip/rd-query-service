
/*
 build.sbt adapted from https://github.com/pbassiner/sbt-multi-project-example/blob/master/build.sbt
*/


name := "rd-query-service"
ThisBuild / organization := "de.dnpm.dip"
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version      := "1.0-SNAPSHOT"


//-----------------------------------------------------------------------------
// PROJECTS
//-----------------------------------------------------------------------------

lazy val global = project
  .in(file("."))
  .settings(
    settings,
    publish / skip := true
  )
  .aggregate(
     api,
     impl
  )


lazy val api = project
  .settings(
    name := "rd-query-service-api",
    settings,
    libraryDependencies ++= Seq(
      dependencies.rd_model,
      dependencies.service_base
    )
  )


lazy val impl = project
  .settings(
    name := "rd-query-service-impl",
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
      dependencies.rd_generators,
      dependencies.connector_base,
//      dependencies.hpo,
      dependencies.hgnc,
    )
  )
  .dependsOn(
    api
  )



//-----------------------------------------------------------------------------
// DEPENDENCIES
//-----------------------------------------------------------------------------

lazy val dependencies =
  new {
    val scalatest      = "org.scalatest"  %% "scalatest"          % "3.2.17" % Test
    val rd_model       = "de.dnpm.dip"    %% "rd-dto-model"       % "1.0-SNAPSHOT"
    val rd_generators  = "de.dnpm.dip"    %% "rd-dto-generators"  % "1.0-SNAPSHOT"
    val service_base   = "de.dnpm.dip"    %% "service-base"       % "1.0-SNAPSHOT"
    val connector_base = "de.dnpm.dip"    %% "connector-base"     % "1.0-SNAPSHOT"
    val hgnc           = "de.dnpm.dip"    %% "hgnc-gene-set-impl" % "1.0-SNAPSHOT" % Test
//    val hpo            = "de.dnpm.dip"    %% "hp-ontology"        % "1.0-SNAPSHOT" % Test
  }


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings


lazy val compilerOptions = Seq(
  "-encoding", "utf8",
  "-unchecked",
  "-feature",
  "-language:postfixOps",
  "-Xfatal-warnings",
  "-deprecation",
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository") ++
    Resolver.sonatypeOssRepos("releases") ++
    Resolver.sonatypeOssRepos("snapshots")
)

