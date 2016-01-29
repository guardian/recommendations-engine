name := """recommender"""
version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, RiffRaffArtifact, JavaAppPackaging)
  .settings(
    mappings in Universal ++= (baseDirectory.value / "resources" ***).get pair relativeTo(baseDirectory.value),
    riffRaffPackageType := (packageZipTarball in config("universal")).value,
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.0.6",
      "com.gu" %% "configuration" % "4.0",
      ws,
      json,
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
      "org.specs2" %% "specs2" % "2.3.12" % "test"
    ),
    routesImport += "binders._",
    routesImport += "models._",
    routesImport += "org.joda.time.DateTime"
  )

addCommandAlias("dist", ";riffRaffArtifact")
