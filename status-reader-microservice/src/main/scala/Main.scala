import better.files.{File, Resource}
import ch.qos.logback.classic.LoggerContext
import com.twosixlabs.dart.commons.config.StandardCliConfig
import com.twosixlabs.dart.exceptions.ExceptionImplicits._
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import org.slf4j.{Logger, LoggerFactory}
import ch.qos.logback.classic.joran.JoranConfigurator
import com.twosixlabs.dart.status.config.DartConfigDI

import java.io.InputStream
import java.nio.file.{Files, Path, Paths}
import scala.util.Try

object Main {

    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    def main( args : Array[ String ] ) : Unit = {
        val port = DartConfigDI.config.getInt( "status.http.port" )
        val server = new Server( port )
        val context = new WebAppContext()

        context.setContextPath( "/" )
        context.setResourceBase( "src/main/webapp" )
        context.setInitParameter( ScalatraListener.LifeCycleKey, "com.twosixlabs.dart.status.ScalatraInit" ) // scalatra uses some magic defaults I don't like
        context.addEventListener( new ScalatraListener )

        server.setHandler( context )
        server.start()
        server.join()
    }
}
