package com.twosixlabs.dart.status

import com.twosixlabs.dart.operations.status.PipelineStatus
import com.twosixlabs.dart.status.models.{DocStatus, ReaderId}

trait SingleStatusReader {
    val id : ReaderId

    def getStatus( historyLine : PipelineStatus, prevStatus : DocStatus) : DocStatus
}
