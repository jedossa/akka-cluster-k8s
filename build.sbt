import com.typesafe.sbt.packager.docker._

name := "general-balance"
version := "0.2-SNAPSHOT"
scalaVersion := "2.12.7"

val akkaVersion = "2.5.17"
val akkaHttpVersion = "10.1.5"
val akkaMgmtVersion = "0.18.0"
val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion withSources () withJavadoc (),
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion withSources () withJavadoc (),
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion withSources () withJavadoc (),
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion withSources () withJavadoc (),
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.80",
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion withSources () withJavadoc (),
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion withSources () withJavadoc (),
  "com.lightbend.akka.management" %% "akka-management" % akkaMgmtVersion withSources () withJavadoc (),
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaMgmtVersion withSources () withJavadoc (),
  "com.lightbend.akka.discovery" %% "akka-discovery-dns" % akkaMgmtVersion withSources () withJavadoc (),
  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaMgmtVersion withSources () withJavadoc (),
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-java8" % circeVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.22.0",
  "de.heikoseeberger" %% "akka-log4j" % "1.6.1",
  "com.typesafe" % "config" % "1.3.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused-import",
  "-Xfuture"
)

dockerCommands :=
  dockerCommands.value.flatMap {
    case ExecCmd("ENTRYPOINT", args @ _*) =>
      Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
    case v => Seq(v)
  }

dockerExposedPorts := Seq(8080, 8558, 2552)

dockerCommands += Cmd("USER", "root")

mainClass in Compile := Option("co.akka.Main")

javaOptions in Universal ++= Seq("-Dlog4j.configurationFile=logback.xml")

publish := publish.triggeredBy(publishLocal in Docker).value

dockerUpdateLatest := true

enablePlugins(JavaAppPackaging)

scalafmtOnCompile in ThisBuild := true
