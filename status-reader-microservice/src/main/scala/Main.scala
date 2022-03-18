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

object Main extends App {

    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    override def main( args : Array[ String ] ) : Unit = {

        val logbackLevel : String = DartConfigDI.config.getString( "log.level" ).toLowerCase
        val logbackFilePath : Path = Paths get {
            val rawPath = DartConfigDI.config.getString( "log.filepath" ).trim
            if ( rawPath.startsWith("~") ) rawPath.replace( "~", System.getProperty( "user.home" ).stripSuffix( "/" ) )
            else rawPath
        }
        val logbackResourceName = "logback/" + logbackLevel + ".xml"
        val logbackResourceStream : InputStream = Resource.getAsStream( logbackResourceName )
        Try( Files.delete( logbackFilePath ) )
        Files.copy( logbackResourceStream, logbackFilePath )
        logbackResourceStream.close()

        val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[ LoggerContext ]
        Try {
            val configurator = new JoranConfigurator
            configurator.setContext( loggerContext )
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            loggerContext.reset()
            configurator.doConfigure( logbackFilePath.toString )
        } logged

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