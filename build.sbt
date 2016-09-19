
// ==================== SETTINGS ====================

lazy val circeVersion  = "0.5.1"
lazy val doobieVersion = "0.3.0"
lazy val h2Version     = "1.4.192"
lazy val http4sVersion = "0.14.6a"
lazy val mgnVersion    = "0.0.1-SNAPSHOT"
lazy val scalaV        = "2.11.8"
lazy val slf4jVersion  = "1.6.4"

lazy val letMorganeyRestSettings = Seq(
  version := "0.0.1-SNAPSHOT",
  scalaVersion := scalaV,
  name := "let-morganey-rest",

  libraryDependencies ++= Seq(
    "com.h2database"     % "h2"                  % h2Version,
    "me.rexim"          %% "morganey"            % mgnVersion,
    "me.rexim"          %% "morganey-kernel"     % mgnVersion,
    "me.rexim"          %% "morganey-macros"     % mgnVersion,
    "org.http4s"        %% "http4s-circe"        % http4sVersion,
    "org.http4s"        %% "http4s-dsl"          % http4sVersion,
    "org.http4s"        %% "http4s-twirl"        % http4sVersion,
    "org.http4s"        %% "http4s-blaze-server" % http4sVersion,
    "org.http4s"        %% "http4s-blaze-client" % http4sVersion,
    "org.slf4j"          % "slf4j-simple"        % slf4jVersion,
    "org.tpolecat"      %% "doobie-core"         % doobieVersion,
    "org.tpolecat"      %% "doobie-contrib-h2"   % doobieVersion,
    "io.circe"          %% "circe-generic"       % circeVersion
  )
)

// ==================== PROJECTS ====================

lazy val proj = (project in file(".")).
  settings(letMorganeyRestSettings: _*).
  enablePlugins(SbtTwirl)
