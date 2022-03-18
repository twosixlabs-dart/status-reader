import sbt._

object Dependencies {

    val slf4jVersion = "1.7.20"
    val logbackVersion = "1.2.3"
    val scalaTestVersion = "3.0.5"
    val scalatraVersion = "2.7.1"
    val dartRestVersion = "3.0.4"
    val okhttpVersion = "4.1.0"
    val jettyWebappVersion = "9.4.18.v20190429"
    val servletApiVersion = "3.1.0"
    val betterFilesVersion = "3.8.0"
    val jacksonVersion = "2.9.9"
    val scalaMockVersion = "4.1.0"
    val dartCommonsVersion = "3.0.30"
    val slickVersion = "3.3.2"
    val c3p0Version = "0.9.5.1"
    val postgresVersion = "42.0.0"
    val slickPgVersion = "0.18.1"
    val operationsVersion = "3.0.14"
    val corpexApiVersion = "3.0.11"


    val logging = Seq( "org.slf4j" % "slf4j-api" % slf4jVersion,
                       "ch.qos.logback" % "logback-classic" % logbackVersion )

    val betterFiles = Seq( "com.github.pathikrit" %% "better-files" % betterFilesVersion )

    val scalatra = Seq( "org.scalatra" %% "scalatra" % scalatraVersion,
                        "org.scalatra" %% "scalatra-scalate" % scalatraVersion,
                        "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test",
                        "org.eclipse.jetty" % "jetty-webapp" % jettyWebappVersion,
                        "javax.servlet" % "javax.servlet-api" % servletApiVersion )

    val dartRest = Seq( "com.twosixlabs.dart.rest" %% "dart-scalatra-commons" % dartRestVersion )

    val okhttp = Seq( "com.squareup.okhttp3" % "okhttp" % okhttpVersion,
                      "com.squareup.okhttp3" % "mockwebserver" % okhttpVersion )

    val scalaTest = Seq( "org.scalatest" %% "scalatest" % scalaTestVersion % "test" )

    val jackson = Seq( "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
                       "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion )

    val scalaMock = Seq( "org.scalamock" %% "scalamock" % scalaMockVersion )

    val dartCommonsCommon = Seq( "com.twosixlabs.dart" %% "dart-exceptions" % dartCommonsVersion,
                                 "com.twosixlabs.dart" %% "dart-test-base" % dartCommonsVersion % "test",
                                 "com.twosixlabs.dart" %% "dart-utils" % dartCommonsVersion )

    val dartCli = Seq( "com.twosixlabs.dart" %% "dart-cli" % dartCommonsVersion )

    val database = Seq( "com.typesafe.slick" %% "slick" % slickVersion,
                        "org.postgresql" % "postgresql" % postgresVersion,
                        "com.github.tminglei" %% "slick-pg" % slickPgVersion,
                        "com.mchange" % "c3p0" % c3p0Version )

    val operationsApi = Seq( "com.twosixlabs.dart.operations" %% "status-api" % operationsVersion )
    val operationsClient = Seq( "com.twosixlabs.dart.operations" %% "status-client" % operationsVersion )

    val corpexApi = Seq( "com.twosixlabs.dart.corpex" %% "corpex-api" % corpexApiVersion )
}

