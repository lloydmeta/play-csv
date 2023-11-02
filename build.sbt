lazy val theVersion = "1.8-SNAPSHOT"
lazy val theScalaVersion = "2.13.12"

lazy val root = Project(id = "root", base = file("."))
  .settings(commonWithPublishSettings)
  .settings(
    name := "play-csv",
    crossScalaVersions := Seq("2.13.12", "3.3.1"),
    crossVersion := CrossVersion.binary,
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.13.0",
      "org.apache.commons" % "commons-text" % "1.11.0",
      "com.typesafe.play" %% "play" % "2.9.0" % Provided,
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    )
  )

lazy val sample = Project(id = "sample", base = file("sample"))
  .settings(commonSettings)
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies += guice,
    routesImport += "com.beachape.play.Csv"
  )
  .dependsOn(root)

lazy val commonWithPublishSettings =
  commonSettings ++
    publishSettings

lazy val commonSettings = Seq(
  organization := "com.beachape",
  version := theVersion,
  scalaVersion := theScalaVersion,
  Test / testOptions += Tests.Argument("-oF")
) ++
  compilerSettings

lazy val compilerSettings = Seq(
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Xlint",
    "-Xlog-free-terms"
  )
)

lazy val publishSettings = Seq(
  pomExtra :=
    <url>https://github.com/lloydmeta/play-csv</url>
      <licenses>
        <license>
          <name>MIT</name>
          <url>https://opensource.org/licenses/MIT</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:lloydmeta/play-csv.git</url>
        <connection>scm:git:git@github.com:lloydmeta/play-csv.git</connection>
      </scm>
      <developers>
        <developer>
          <id>lloydmeta</id>
          <name>Lloyd Chan</name>
          <url>https://beachape.com/</url>
        </developer>
      </developers>,
  publishTo := version { v =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at s"${nexus}content/repositories/snapshots")
    else
      Some("releases" at s"${nexus}service/local/staging/deploy/maven2")
  }.value,
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false }
)
