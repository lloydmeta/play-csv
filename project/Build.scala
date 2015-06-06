import play.PlayImport.PlayKeys._
import play.PlayScala
import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._
import play.sbt.routes.RoutesKeys._

object Build extends Build {

  lazy val theVersion = "1.2"
  lazy val theScalaVersion = "2.11.6"

  lazy val root = Project(id = "root", base = file("."), settings = commonWithPublishSettings)
    .settings(
      name := "play-csv",
      crossScalaVersions := Seq("2.10.5", "2.11.6"),
      crossVersion := CrossVersion.binary,
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play" % "2.4.0" % "provided",
        "org.scalatest" %% "scalatest"  % "2.2.3" % Test
      )
    )

  lazy val sample = Project(id = "sample", base = file("sample"), settings = commonSettings)
    .enablePlugins(PlayScala)
    .settings(
      routesImport += "com.beachape.play.Csv"
    ).dependsOn(root)

  lazy val commonWithPublishSettings =
    commonSettings ++
      publishSettings

  lazy val commonSettings = Seq(
    organization := "com.beachape",
    version := theVersion,
    scalaVersion := theScalaVersion,
    testOptions in Test += Tests.Argument("-oF")
  ) ++
    scalariformSettings ++
    formatterPrefs ++
    compilerSettings ++
    resolverSettings

  lazy val formatterPrefs = Seq(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignParameters, true)
      .setPreference(DoubleIndentClassDeclaration, true)
  )

  lazy val resolverSettings = Seq(
    resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )

  lazy val compilerSettings = Seq(
    // the name-hashing algorithm for the incremental compiler.
    incOptions := incOptions.value.withNameHashing(nameHashing = true),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xlint", "-Xlog-free-terms")
  )

  lazy val publishSettings = Seq(
    pomExtra :=
      <url>https://github.com/lloydmeta/play-csv</url>
      <licenses>
        <license>
          <name>MIT</name>
          <url>http://opensource.org/licenses/MIT</url>
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
          <url>http://lloydmeta.github.io</url>
        </developer>
      </developers>,
    publishTo <<= version { v =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false }
  )
}
