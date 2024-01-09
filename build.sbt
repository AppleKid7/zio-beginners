val scala3Version = "3.3.1"

lazy val zioVersion = "2.0.19"
lazy val zioPreludeVersion = "1.0.0-RC21"
lazy val monocleVersion = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
      name := "zio-beginners",
      version := "0.1.0-SNAPSHOT",

      scalaVersion := scala3Version,
      fork := true,

      libraryDependencies += "dev.zio" %% "zio" % zioVersion,
      libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
      libraryDependencies += "dev.optics" %% "monocle-core"  % monocleVersion,
      libraryDependencies += "dev.optics" %% "monocle-macro" % monocleVersion,
      libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
      libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
  )
