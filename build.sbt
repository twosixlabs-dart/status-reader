import sbt._
import Dependencies._
import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy

/*
   ##############################################################################################
   ##                                                                                          ##
   ##                                  SETTINGS DEFINITIONS                                    ##
   ##                                                                                          ##
   ##############################################################################################
 */

// integrationConfig and wipConfig are used to define separate test configurations for integration testing
// and work-in-progress testing
lazy val IntegrationConfig = config( "integration" ) extend( Test )
lazy val WipConfig = config( "wip" ) extend( Test )

lazy val commonSettings =
    inConfig( IntegrationConfig )( Defaults.testTasks ) ++
    inConfig( WipConfig )( Defaults.testTasks ) ++
    Seq(
        organization := "com.twosixlabs.dart.status.reader",
        scalaVersion := "2.12.7",
        resolvers ++= Seq(
            "Maven Central" at "https://repo1.maven.org/maven2/",
            "JCenter" at "https://jcenter.bintray.com",
            "Local Ivy Repository" at s"file://${System.getProperty( "user.home" )}/.ivy2/local/default"
        ),
        javacOptions ++= Seq( "-source", "1.8", "-target", "1.8" ),
        scalacOptions += "-target:jvm-1.8",
        useCoursier := false,
        libraryDependencies ++= logging ++
                                scalaTest ++
                                scalaMock ++
                                dartCommonsCommon,
        // `sbt test` should skip tests tagged IntegrationTest
        Test / testOptions := Seq( Tests.Argument( "-l", "annotations.IntegrationTest" ) ),
        // `sbt integration:test` should run only tests tagged IntegrationTest
        IntegrationConfig / parallelExecution := false,
        IntegrationConfig / testOptions := Seq( Tests.Argument( "-n", "annotations.IntegrationTest" ) ),
        // `sbt wip:test` should run only tests tagged WipTest
        WipConfig / testOptions := Seq( Tests.Argument( "-n", "annotations.WipTest" ) ),
    )

lazy val disablePublish = Seq(
    skip.in( publish ) := true,
)

lazy val assemblySettings = Seq(
    libraryDependencies ++= scalatra ++ jackson,
    assemblyMergeStrategy in assembly := {
        case PathList( "META-INF", "MANIFEST.MF" ) => MergeStrategy.discard
        case PathList( "reference.conf" ) => MergeStrategy.concat
        case x => MergeStrategy.last
    },
    Compile / unmanagedResourceDirectories += baseDirectory.value / "src/main/webapp",
    test in assembly := {},
    mainClass in( Compile, run ) := Some( "Main" ),
)


/*
   ##############################################################################################
   ##                                                                                          ##
   ##                                  PROJECT DEFINITIONS                                     ##
   ##                                                                                          ##
   ##############################################################################################
 */

lazy val root = ( project in file( "." ) )
  .disablePlugins( sbtassembly.AssemblyPlugin )
  .aggregate( coreProject, clientProject, controllerProject, microserviceProject )
  .settings(
      name := "status-reader",
      disablePublish
  )

lazy val coreProject = ( project in file( "status-reader-core" ) )
  .configs( IntegrationConfig, WipConfig )
  .disablePlugins( sbtassembly.AssemblyPlugin )
  .settings(
      commonSettings,
      libraryDependencies ++= database ++ operationsApi,
  )

lazy val clientProject = ( project in file( "status-reader-client" ) )
  .dependsOn( coreProject )
  .configs( IntegrationConfig, WipConfig )
  .disablePlugins( sbtassembly.AssemblyPlugin )
  .settings(
      commonSettings,
      libraryDependencies ++= database ++ operationsApi ++ operationsClient,
  )

lazy val controllerProject = ( project in file( "status-reader-controller" ) )
  .dependsOn( clientProject )
  .configs( IntegrationConfig, WipConfig )
  .disablePlugins( sbtassembly.AssemblyPlugin )
  .settings(
      commonSettings,
      libraryDependencies ++= scalatra ++ okhttp ++ jackson ++ dartRest ++ corpexApi,
  )

lazy val microserviceProject = ( project in file( "status-reader-microservice" ) )
  .dependsOn( controllerProject )
  .configs( IntegrationConfig, WipConfig )
  .enablePlugins( JavaAppPackaging )
  .settings(
      commonSettings,
      libraryDependencies ++= betterFiles ++ scalatra ++ jackson ++ dartRest ++ dartCli,
      assemblySettings,
      disablePublish,
  )

