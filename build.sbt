import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

lazy val theVersion = "1.6-SNAPSHOT"
lazy val theScalaVersion = "2.13.8"

lazy val root = Project(id = "root", base = file("."))
  .settings(commonWithPublishSettings)
  .settings(
    name := "play-csv",
    crossScalaVersions := Seq("2.13.8", "2.12.15"),
    crossVersion := CrossVersion.binary,
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.12.0",
      "org.apache.commons" % "commons-text" % "1.10.0",
      "com.typesafe.play" %% "play" % "2.8.19" % Provided,
      "org.scalatest" %% "scalatest"  % "3.2.15" % Test
    )
  )

lazy val sample = Project(id = "sample", base = file("sample"))
  .settings(commonSettings)
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies += guice,
    routesImport += "com.beachape.play.Csv"
  ).dependsOn(root)

lazy val commonWithPublishSettings =
  commonSettings ++
    publishSettings

lazy val commonSettings = Seq(
  organization := "com.beachape",
  version := theVersion,
  scalaVersion := theScalaVersion,
  Test / testOptions += Tests.Argument("-oF")
) ++
  scalariformSettings(true) ++
  formatterPrefs ++
  compilerSettings

lazy val formatterPrefs = Seq(
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(AlignParameters, true)
    .setPreference(DoubleIndentConstructorArguments, true)
)

lazy val compilerSettings = Seq(
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xlint", "-Xlog-free-terms")
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
      Some("releases"  at s"${nexus}service/local/staging/deploy/maven2")
  }.value,
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false }
)
