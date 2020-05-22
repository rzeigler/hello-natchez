val Http4sVersion = "0.21.3"
val CirceVersion = "0.13.0"
val Specs2Version = "4.9.3"
val LogbackVersion = "1.2.3"
val NatchezVersion = "0.0.11"
val PostgresqlVersion = "42.2.12"

lazy val root = (project in file("."))
  .settings(
    organization := "rzeigler",
    name := "hello-natchez",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.specs2" %% "specs2-core" % Specs2Version % "test",
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.tpolecat" %% "natchez-core" % NatchezVersion,
      "org.tpolecat" %% "natchez-log" % NatchezVersion,
      "org.tpolecat" %% "doobie-core" % "0.9.0",
      "org.postgresql" % "postgresql" % PostgresqlVersion,
      "io.chrisdavenport" %% "log4cats-core" % "1.0.1",
      "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1"
    ),
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
    ),
    addCompilerPlugin(
      "com.olegpy" %% "better-monadic-for" % "0.3.1"
    )
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings"
)
