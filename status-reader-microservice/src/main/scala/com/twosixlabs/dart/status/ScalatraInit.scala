package com.twosixlabs.dart.status

import com.twosixlabs.dart.rest.ApiStandards
import com.twosixlabs.dart.rest.scalatra.DartRootServlet
import com.twosixlabs.dart.status.config.{DartConfigDI, StatusReaderControllerDI}
import org.scalatra.LifeCycle
import org.slf4j.{Logger, LoggerFactory}

import javax.servlet.ServletContext

class ScalatraInit extends LifeCycle {

    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    private val basePath : String = ApiStandards.DART_API_PREFIX_V1 + "/status"

    private val rootController = new DartRootServlet( Some( basePath ),
                                                      Some( getClass.getPackage.getImplementationVersion ) )

    private val allowedOrigins = {
        if (DartConfigDI.config.hasPath( "cors.allowed.origins" ))
            DartConfigDI.config.getString( "cors.allowed.origins" )
        else "*"
    }

    private val docStatusController = new StatusReaderController( StatusReaderControllerDI )

    // Initialize scalatra: mounts servlets
    override def init( context : ServletContext ) : Unit = {
        context.setInitParameter( "org.scalatra.cors.allowedOrigins", allowedOrigins )
        context.mount( rootController, "/*" )
        context.mount( docStatusController, basePath )
    }

    // Scalatra callback to close out resources
    override def destroy( context : ServletContext ) : Unit = {
        super.destroy( context )
    }

}
