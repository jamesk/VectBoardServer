organization := "movio.hackathon"
name := "vectboard"
scalaVersion := "2.11.7"

lazy val svc = project.in(file(".")).enablePlugins(PlayScala)

name in Universal := moduleName.value

resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "movio" at "https://artifactory.movio.co/artifactory/repo",
  "sonatype" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.4",
  "org.boofcv" % "all" % "0.20-SNAPSHOT",
  "org.jfree" % "jfreesvg" % "3.0", //Not used
  "batik" % "batik-svg-dom" % "1.6-1"
)

scalacOptions ++= Seq(
  "-Xlint",
  "-deprecation",
  "-feature"
)

fork in Test := true
