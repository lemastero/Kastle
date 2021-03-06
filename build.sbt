import Dependencies._
import sbt.Resolver

lazy val commonTestDependencies = Seq(
  scalaTest,
  mockitoCore
) ++ logbackRelated

inThisBuild(
  List(
    organization := "com.tenable.katsle.library",
    scalaVersion := "2.12.10",
    crossScalaVersions := Seq("2.12.10", "2.13.1")
  )
)

lazy val root = (project in file("."))
  .settings(
    publishTo := None,
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
  .aggregate(kafkaClient)

lazy val kafkaClient = (project in file("kafka-library"))
  .overrideConfigs(IntegrationSettings.config)
  .settings(IntegrationSettings.configSettings)
  .settings(
    name := "Kafka Client",
    addCompilerPlugin(silencerPlugin),
    addCompilerPlugin(kindProjector),
    libraryDependencies ++= Seq(
      silencerPlugin,
      slf4jApi,
      catsCore,
      catsFree,
      catsEffect,
      simulacrum,
      avro,
      typesafeConfig,
      jacksonDatabind
    )
      ++ kafkaRelated
      ++ commonTestDependencies.map(_  % Test)
      ++ commonTestDependencies.map(_  % IntegrationTest)
      ++ Seq("io.github.embeddedkafka" %% "embedded-kafka" % "2.4.0" % IntegrationTest)
  )

lazy val doNotPublishArtifact = Seq(
  publishArtifact := false,
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in (Compile, packageSrc) := false,
  publishArtifact in (Compile, packageBin) := false
)

lazy val site = project
  .in(file("site"))
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)
  .settings(doNotPublishArtifact)
  .settings {
    import microsites._
    Seq(
      micrositeName := "Kastle",
      micrositeDescription := "Purely functional, effectful, resource-safe, kafka library for Scala",
      micrositeHomepage := "https://tenable.github.io/Kastle",
      micrositeBaseUrl := "Kastle",
      micrositeOrganizationHomepage := "https://www.tenable.com",
      micrositeTwitter := "@TenableSecurity",
      micrositeAuthor := "Tenable",
      micrositeGithubOwner := "tenable",
      micrositeGithubRepo := "Kastle",
      micrositeFooterText := Some("Copyright Tenable, Inc 2020"),
      micrositeHighlightTheme := "atom-one-light",
      micrositeCompilingDocsTool := WithMdoc,
      fork in mdoc := true, // ?????
      // sourceDirectory in Compile := baseDirectory.value / "src",
      // sourceDirectory in Test := baseDirectory.value / "test",
      mdocIn := (sourceDirectory in Compile).value / "docs",
      micrositeExtraMdFilesOutput := resourceManaged.value / "main" / "jekyll",
      micrositeExtraMdFiles := Map(
        file("README.md") -> ExtraMdFileConfig(
          "index.md",
          "home",
          Map("title" -> "Home", "section" -> "home", "position" -> "0")
        )
      )
    )
  }
  .dependsOn(kafkaClient)
