package com.twosixlabs.dart.status.readers

import com.twosixlabs.dart.operations.status.PipelineStatus
import com.twosixlabs.dart.operations.status.PipelineStatus.{ProcessorType, Status}
import com.twosixlabs.dart.status.SingleStatusReader
import com.twosixlabs.dart.status.models.{DocStatus, NoStatus, ReaderId}

trait DartStatusReaderDependencies {
    val numAnnotators : Int
}

object DartStatusReader {
    def apply( dependencies : DartStatusReaderDependencies ) = new DartStatusReader( dependencies )
    def unapply( status : DartDocStatus ) : Option[ (Int, String) ] = Some( status.rank, status.msg )

    sealed class DartDocStatus( val rank : Int, msg : String ) extends DocStatus( msg ) with Ordered[ DartDocStatus ] {
        override def compare( that : DartDocStatus ) : Int = this.rank - that.rank
    }

    case object Staged extends DartDocStatus( 0, "Staged" )

    case object Processing extends DartDocStatus( 1, "Processing" )

    case object Duplicate extends DartDocStatus( 2, "Duplicate" )

    case object BadDocument extends DartDocStatus( 3, "Bad Document" )

    case class Annotating( annotations : Set[ String ], total : Int )
      extends DartDocStatus( 4, s"Annotating (${annotations.size} of $total complete)" )

    case object Completed extends DartDocStatus( 5, "Completed" )
}

class DartStatusReader( dependencies : DartStatusReaderDependencies ) extends SingleStatusReader {
    import DartStatusReader._

    val numAnnotators : Int = dependencies.numAnnotators

    override val id : ReaderId = ReaderId( "dart" )

    override def getStatus( historyLine : PipelineStatus,
                            prevStatus : DocStatus ) : DocStatus = {
        val status = historyLine.getStatus
        val msg = historyLine.getMessage
        val appId = historyLine.getApplicationId
        historyLine.getProcessorType match {
            case ProcessorType.CORE =>
                if ( status == Status.FAILURE ) {
                    if ( msg == "DUPLICATE" ) {
                        Duplicate
                    } else if ( appId == "DocumentProcessingError" ) {
                        BadDocument
                    } else prevStatus match {
                        case dartStatus : DartDocStatus =>
                            if ( dartStatus < Processing ) Processing
                            else dartStatus
                        case _ => Processing
                    }
                } else if ( appId == "forklift" ) {
                    prevStatus match {
                        case dartStatus : DartDocStatus if ( dartStatus > Staged ) => dartStatus
                        case _ => Staged
                    }
                } else prevStatus match {
                    case dartStatus : DartDocStatus =>
                        if ( dartStatus < Processing ) Processing
                        else dartStatus
                    case _ => Processing
                }

            case ProcessorType.ANNOTATOR =>
                prevStatus match {
                    case Completed => Completed
                    case Annotating( prevAnnotations, _ ) =>
                        if ( prevAnnotations.contains( appId ) ) prevStatus
                        else if ( prevAnnotations.size + 1 >= numAnnotators ) Completed
                        else Annotating( prevAnnotations + appId, numAnnotators )
                    case _ => Annotating( Set( appId ), numAnnotators )
                }

            case _ => prevStatus
        }
    }
}
