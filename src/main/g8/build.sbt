lazy val buildSettings = Seq(
  organization := "$organization$",
  scalaVersion := "$scala_version$",
  crossScalaVersions := Seq("$scala_version$")
)

lazy val consoleSettings = Seq(
  shellPrompt := { state =>
    val user = System.getProperty("user.name")
    user + "@" + Project.extract(state).currentRef.project + ":sbt> "
  },
  initialCommands in console :=
    """
      |import java.time.Instant
      |import java.util.UUID
    """.stripMargin
)

lazy val dependencySettings = Seq(
  libraryDependencies ++= {
    val slf4jVersion = "1.7.21"
    val logbackVersion = "1.1.7"
    Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "ch.qos.logback" % "logback-core" % logbackVersion,
      "com.typesafe" % "config" % "1.3.1",
      "junit" % "junit" % "4.12" % "test",
      "org.scalatest" %% "scalatest" % "3.0.0" % "test",
      "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion)
  },
  resolvers ++= Seq(
    Resolver.mavenLocal,
    Resolver.sonatypeRepo("releases"),
    "hohonuuli-bintray" at "http://dl.bintray.com/hohonuuli/maven")
)

lazy val gitHeadCommitSha =
  SettingKey[String]("git-head", "Determines the current git commit SHA")

lazy val makeVersionProperties =
  TaskKey[Seq[File]]("make-version-props", "Makes a version.properties file")

lazy val makeVersionSettings = Seq(
  gitHeadCommitSha := scala.util.Try(Process("git rev-parse HEAD").lines.head).getOrElse(""),
  makeVersionProperties := {
    val propFile = (resourceManaged in Compile).value / "version.properties"
    val content = "version=%s" format (gitHeadCommitSha.value)
    IO.write(propFile, content)
    Seq(propFile)
  },
  resourceGenerators in Compile <+= makeVersionProperties
)

lazy val optionSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8", // yes, this is 2 args
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"),
  javacOptions ++= Seq("-target", "1.8", "-source", "1.8"),
  incOptions := incOptions.value.withNameHashing(true),
  updateOptions := updateOptions.value.withCachedResolution(true),
  scalafmtConfig := Some(file(".scalafmt"))
)

// --- Aliases
addCommandAlias("cleanall", ";clean;clean-files")

// --- Modules
lazy val varsSettings = buildSettings ++ consoleSettings ++ dependencySettings ++
    optionSettings ++ reformatOnCompileSettings

lazy val root = (project in file("."))
  .settings(varsSettings)
  .settings(
    name := "$name$",
    version := "$version$",
    todosTags := Set("TODO", "FIXME", "WTF"),
    fork := true,
    libraryDependencies ++= {
      val jettyVersion = "9.3.11.v20160721"
      val scalatraVersion = "2.5.0-RC1"
      Seq(
        "com.fatboyindustrial.gson-javatime-serialisers" % "gson-javatime-serialisers" % "1.1.1",
        "com.google.code.gson" % "gson" % "2.8.0",
        "commons-codec"            % "commons-codec"                  % "1.10",
        "javax.servlet"            % "javax.servlet-api"              % "3.1.0",
        "javax.transaction"        % "jta"                            % "1.1",
        "net.databinder.dispatch" %% "dispatch-core"                  % "0.11.3",
        "org.eclipse.jetty"        % "jetty-server"                   % jettyVersion          % "compile;test",
        "org.eclipse.jetty"        % "jetty-servlets"                 % jettyVersion          % "compile;test",
        "org.eclipse.jetty"        % "jetty-webapp"                   % jettyVersion          % "compile;test",
        "org.json4s"              %% "json4s-jackson"                 % "3.4.0",
        "org.scalatest"           %% "scalatest"                      % scalaTestVersion      % "test",
        "org.scalatra"            %% "scalatra"                       % scalatraVersion,
        "org.scalatra"            %% "scalatra-json"                  % scalatraVersion,
        "org.scalatra"            %% "scalatra-scalate"               % scalatraVersion,
        "org.scalatra"            %% "scalatra-slf4j"                 % scalatraVersion,
        "org.scalatra"            %% "scalatra-swagger"               % scalatraVersion,
        "org.scalatra"            %% "scalatra-swagger-ext"           % scalatraVersion,
        "org.scalatra"            %% "scalatra-scalatest"            % scalatraVersion)
          .map(_.excludeAll(ExclusionRule("org.slf4j", "slf4j-jdk14"),
            ExclusionRule("org.slf4j", "slf4j-log4j12"),
            ExclusionRule("javax.servlet", "servlet-api")))
      )
    },
    mainClass in assembly := Some("JettyMain")
  )

// -- SBT-PACK
// For sbt-pack
packAutoSettings

// For sbt-pack
val apps = Seq("jetty-main")

packAutoSettings ++ Seq(packExtraClasspath := apps.map(_ -> Seq("${PROG_HOME}/conf")).toMap,
  packJvmOpts := apps.map(_ -> Seq("-Duser.timezone=UTC", "-Xmx4g")).toMap,
  packDuplicateJarStrategy := "latest",
  packJarNameConvention := "original")
  
// OTHER SETTINGS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// -- sbt-native-packager
enablePlugins(JavaServerAppPackaging)

// -- xsbt-web-plugin
enablePlugins(JettyPlugin)