lazy val theScalaVersion = "2.13.8"

lazy val root =
  Project(id = "root", base = file("."))
    .settings(commonWithPublishSettings)
    .settings(
      name := "play-csv",
      crossScalaVersions := Seq("2.12.15", "2.13.8"),
      crossVersion := CrossVersion.binary,
      libraryDependencies ++= Seq(
        "org.apache.commons" % "commons-lang3" % "3.12.0",
        "org.apache.commons" % "commons-text" % "1.9",
        "com.typesafe.play" %% "play" % "2.8.0" % Provided,
        "org.scalatest" %% "scalatest" % "3.2.11" % Test
      )
    )

lazy val sample =
  Project(id = "sample", base = file("sample"))
    .settings(commonSettings)
    .enablePlugins(PlayScala)
    .settings(
      routesImport += "com.beachape.play.Csv",
      libraryDependencies ++= Seq(guice)
    )
    .dependsOn(root)

lazy val commonWithPublishSettings =
  commonSettings ++
    publishSettings

lazy val commonSettings = Seq(
  organization := "com.beachape",
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
    "-Xlog-free-terms",
    "-Wconf:any&src=target/.*:s"
  )
)

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/lloydmeta/play-csv")),
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  developers := List(
    Developer(
      "lloydmeta",
      "Lloyd Chan",
      "lloydmeta@gmail.com",
      url("https://beachape.com/")
    )
  )
)
