name := """PlayWithIt"""
organization := "com.solynaranjas"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
      scalaVersion := "3.3.1",
      scalacOptions ++= {
          if (scalaVersion.value.startsWith("3.")) scala3Options
          else scala2Options
      }
  )
  .enablePlugins(PlayScala)

lazy val sharedScalacOptions =
    Seq("-encoding", "UTF-8", "-Wunused:imports,privates,locals")

lazy val scala2Options = sharedScalacOptions ++
  Seq("-Xsource:3", "-explaintypes")

lazy val scala3Options = sharedScalacOptions ++
  Seq("-Xunchecked-java-output-version:8", "-explain")

javaOptions ++= Seq(
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "-XX:MaxMetaspaceSize=6024m", // Set the MaxMetaspaceSize to 256 megabytes
    // Other JVM options can be added here as well
)

libraryDependencies ++= Seq(
    ("org.typelevel" %% "simulacrum" % "1.0.0").cross(CrossVersion.for3Use2_13),
    "org.typelevel" %% "cats-core"% "2.6.1",
    "org.typelevel" %% "cats-effect" % "2.5.1",
    "org.fusesource.jansi" % "jansi" % "1.18"
)

libraryDependencies += caffeine
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0" % Test

