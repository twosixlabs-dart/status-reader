package com.twosixlabs.dart.status.config

import com.typesafe.config.{Config, ConfigException, ConfigFactory}

import java.util.concurrent.TimeUnit
import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

trait DartConfig {
    val config : Config

    implicit class configMethods( config : Config ) {
        def getFiniteDuration( path : String ) : FiniteDuration = {
            FiniteDuration( config.getDuration( path ).toNanos, TimeUnit.NANOSECONDS )
        }

        def getDartStringList( path : String ) : List[ String ] = Try {
            config.getStringList( path ).asScala.toList
        } recover {
            case e : ConfigException.WrongType =>
                config.getString( path ).split( "," ).toList.map( _.trim )
        } get

        def getDartIntList( path : String ) : List[ Int ] = Try {
            config.getIntList( path ).asScala.toList.map( _.toInt )
        } getOrElse {
                config.getString( path ).split( ',' ).toList.map( _.trim.toInt )
        }

        def getDartKeyValueList( path : String ) : List[ (String, String) ] = Try {
            config.getConfigList( path ).asScala.toList.map { subConfig =>
                val key = subConfig.getString( "key" )
                val value = subConfig.getString( "value" )
                (key, value)
            }
        } getOrElse {
            Try {
                config.getStringList( path ).asScala.toList.map { str =>
                    val strArray = str.split( ':' ).map( _.trim )
                    (strArray( 0 ), strArray( 1 ))
                }
            } getOrElse {
                config.getString( path )
                  .split( ',' ).toList
                  .map { str =>
                      val strArray = str.split( ':' ).map( _.trim )
                      (strArray( 0 ), strArray( 1 ))
                  }
            }
        }
    }
}

trait DartConfigDI extends DartConfig {
    override val config : Config = ConfigFactory.parseResources( s"application.conf" ).resolve()
}

object DartConfigDI extends DartConfigDI
