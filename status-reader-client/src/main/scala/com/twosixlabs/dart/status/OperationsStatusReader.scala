package com.twosixlabs.dart.status

import com.twosixlabs.dart.operations.status.PipelineStatus
import com.twosixlabs.dart.operations.status.client.PipelineStatusQueryClient
import com.twosixlabs.dart.status.readers.DartStatusReader

trait OperationsStatusReaderDependencies {
    val operationsClient : PipelineStatusQueryClient
    val dartReader : DartStatusReader
}

class OperationsStatusReader( dependencies : OperationsStatusReaderDependencies ) extends StatusReader {
    private lazy val dartReader = dependencies.dartReader
    private lazy val operationsClient = dependencies.operationsClient

    override lazy val readers : Set[ SingleStatusReader ] = Set( dartReader )

    override def retrieveHistory( docId : String ) : List[ PipelineStatus ] = {
        operationsClient.historyForDoc( docId ).sortBy( _.getEndTime ).toList
    }
}
