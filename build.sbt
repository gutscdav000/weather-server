val Http4sVersion = "0.23.5"
val CirceVersion = "0.14.1"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.6"
val MunitCatsEffectVersion = "1.0.6"
val natchezVersion = "0.1.4"
val fs2Version = "3.2.7"

lazy val root = (project in file("."))
  .settings(
    organization := "com.jackhenry",
    name := "weatherserver",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"      %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,
      "io.circe" % "circe-core_2.13" % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe"  %% "circe-parser"   % CirceVersion,
      "org.scalameta"   %% "munit"               % MunitVersion           % Test,
      "org.typelevel"   %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "io.chrisdavenport"           %% "fiberlocal"           % "0.1.1",
      "org.tpolecat" %% "natchez-core" % natchezVersion,
      "org.tpolecat" %% "natchez-jaeger"      % natchezVersion,
      "org.tpolecat" %% "natchez-honeycomb" % natchezVersion,
      "io.chrisdavenport" %% "natchez-http4s-otel" % "0.1.0",
      "com.typesafe" % "config" % "1.4.1",
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.0" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
