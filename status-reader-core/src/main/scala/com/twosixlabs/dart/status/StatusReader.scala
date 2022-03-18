package com.twosixlabs.dart.status

import com.twosixlabs.dart.operations.status.PipelineStatus
import com.twosixlabs.dart.status.models.{ DocId, DocStatus, NoStatus, ReaderId }

import scala.language.postfixOps


trait StatusReader {
    val readers : Set[ SingleStatusReader ]

    def getDocStatus( docId : String ) : Map[ ReaderId, DocStatus ] = {
        interpretHistory( retrieveHistory( docId ).sortBy( _.getEndTime ) )
    }

    def getDocStatus( docIds : Seq[ String ] ) : Map[ DocId, Map[ ReaderId, DocStatus ] ] = {
        docIds.distinct.map( ( docId : String ) => DocId( docId ) -> getDocStatus( docId ) ).toMap
    }

    def retrieveHistory( docId : String ) : List[ PipelineStatus ]

    private def interpretHistory( history : List[ PipelineStatus ] ) : Map[ ReaderId, DocStatus ] = {
        val initReaderMap : Set[ (SingleStatusReader, DocStatus) ] =
            readers.map( rdr => (rdr, NoStatus ) )

        history.foldLeft( initReaderMap ) { ( readerMap, historyLine : PipelineStatus ) =>
            readerMap map { tup =>
                val (reader, oldStatus) = tup
                reader -> reader.getStatus( historyLine, oldStatus )
            }
        } map( ( rdrMap : (SingleStatusReader, DocStatus) ) => {
            val (rdr, sts) = rdrMap
            rdr.id -> sts
        } ) toMap
    }
}
