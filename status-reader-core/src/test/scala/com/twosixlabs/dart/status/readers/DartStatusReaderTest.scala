package com.twosixlabs.dart.status.readers

import com.twosixlabs.dart.operations.status.PipelineStatus
import com.twosixlabs.dart.operations.status.PipelineStatus.{ProcessorType, Status}
import com.twosixlabs.dart.status.models.NoStatus
import com.twosixlabs.dart.status.readers.DartStatusReader.{Annotating, BadDocument, Completed, Duplicate, Processing, Staged}
import com.twosixlabs.dart.test.base.StandardTestBase3x

class DartStatusReaderTest extends StandardTestBase3x {

    var testNumAnnotators = 3

    val statusReader = new DartStatusReader( new DartStatusReaderDependencies {
        override val numAnnotators : Int = testNumAnnotators
    })

    val testHistoryLine = new PipelineStatus(
        "fake-doc-id",
        "fake-application-id",
        PipelineStatus.ProcessorType.CORE,
        PipelineStatus.Status.SUCCESS,
        "fake-scope",
        100,
        100,
        "fake-message",
    )

    implicit class CopyAblePipelineStatus( ps : PipelineStatus ) {
        def copy(
            documentId : String = ps.getDocumentId,
            applicationId : String = ps.getApplicationId,
            processorType: ProcessorType = ps.getProcessorType,
            status : Status = ps.getStatus,
            scope : String = ps.getScope,
            start : Long = ps.getStartTime,
            stop : Long = ps.getEndTime,
            message : String = ps.getMessage,
        ) : PipelineStatus = {
            new PipelineStatus( documentId, applicationId, processorType, status, scope, start, stop, message )
        }
    }

    behavior of "DartStatusReader.getStatus"

    it should "Return Staged from No Status when historyline is successful forklift core processor" in {
        val nextStatus = statusReader.getStatus( testHistoryLine.copy( applicationId = "forklift" ), NoStatus )
        nextStatus shouldBe Staged
    }

    it should "Return Processing from No Status when historyLine is successful core processor" in {
        val nextStatus = statusReader.getStatus( testHistoryLine, NoStatus )
        nextStatus shouldBe Processing
    }

    it should "Return Bad Document from No Status when status is FAILURE and application id is DocumentProcessingError " in {
        val historyLine = testHistoryLine.copy( applicationId = "DocumentProcessingError" )
        val nextStatus = statusReader.getStatus( historyLine, NoStatus )
        nextStatus.msg should not be BadDocument // historyLine is not FAILURE

        val historyLine1 = testHistoryLine.copy( status = Status.FAILURE )
        val nextStatus1 = statusReader.getStatus( historyLine1, NoStatus )
        nextStatus1.msg should not be BadDocument // historyLine FAILURE but not DocumentProcessingError

        val historyLine2 = testHistoryLine.copy( status = Status.FAILURE, applicationId = "DocumentProcessingError" )
        val nextStatus2 = statusReader.getStatus( historyLine2, NoStatus )
        nextStatus2 shouldBe BadDocument
    }

    it should "Return Duplicate from No Status when status is FAILURE and message is DUPLICATE" in {
        val historyLine = testHistoryLine.copy( message = "DUPLICATE" )
        val nextStatus = statusReader.getStatus( historyLine, NoStatus )
        nextStatus should not be Duplicate // historyLine is not FAILURE

        val historyLine1 = testHistoryLine.copy( status = Status.FAILURE )
        val nextStatus1 = statusReader.getStatus( historyLine1, NoStatus )
        nextStatus1 should not be Duplicate // historyLine FAILURE but message is not DUPLICATE

        val historyLine2 = testHistoryLine.copy( status = Status.FAILURE, message = "DUPLICATE" )
        val nextStatus2 = statusReader.getStatus( historyLine2, NoStatus )
        nextStatus2 shouldBe Duplicate
    }

    it should "not return Processing from any other status than No Status or Uploaded" in {
        val nextStatus = statusReader.getStatus( testHistoryLine, Completed )
        nextStatus should not be Processing

        val nextStatus1 = statusReader.getStatus( testHistoryLine, Annotating( Set( "test-annotator" ), 3 ) )
        nextStatus1 should not be Processing

        val nextStatus2 = statusReader.getStatus( testHistoryLine, BadDocument )
        nextStatus2 should not be Processing

        val nextStatus3 = statusReader.getStatus( testHistoryLine, Duplicate )
        nextStatus3 should not be Processing

        val nextStatus4 = statusReader.getStatus( testHistoryLine, Staged )
        nextStatus4 shouldBe Processing
    }

    it should "update Annotating status, ignoring repeated annotations and other core processes; last annotation should return Completed; no new annotations should change Completed" in {
        val annotatorIds = Array( "annotator-1", "annotator-2", "annotator-3", "annotator-4" )

        val annotatorHistoryLine = testHistoryLine.copy( processorType = ProcessorType.ANNOTATOR )

        val nextHistoryLine = annotatorHistoryLine.copy( applicationId = annotatorIds( 0 ) )
        val nextStatus = statusReader.getStatus( nextHistoryLine, Staged )
        nextStatus shouldBe Annotating( Set( annotatorIds( 0 ) ), 3 )

        val nextHistoryLine1 = annotatorHistoryLine.copy( applicationId = annotatorIds( 1 ) )
        val nextStatus1 = statusReader.getStatus( nextHistoryLine1, nextStatus )
        nextStatus1 shouldBe Annotating( Set( annotatorIds( 0 ), annotatorIds( 1  ) ), 3 )

        // Core processor
        val nextHistoryLine2 = testHistoryLine
        val nextStatus2 = statusReader.getStatus( nextHistoryLine2, nextStatus1 )
        nextStatus2 shouldBe nextStatus1

        // Repeat annotation
        val nextHistoryLine3 = annotatorHistoryLine.copy( applicationId = annotatorIds( 1 ) )
        val nextStatus3 = statusReader.getStatus( nextHistoryLine3, nextStatus2 )
        nextStatus3 shouldBe nextStatus1

        // Last annotations should make completed
        val nextHistoryLine4 = annotatorHistoryLine.copy( applicationId = annotatorIds( 2 ) )
        val nextStatus4 = statusReader.getStatus( nextHistoryLine4, nextStatus3 )
        nextStatus4 shouldBe Completed

        // New annotations should make no difference
        val nextHistoryLine5 = annotatorHistoryLine.copy( applicationId = annotatorIds( 3 ) )
        val nextStatus5 = statusReader.getStatus( nextHistoryLine5, nextStatus4 )
        nextStatus5 shouldBe Completed
    }

    it should "replace Annotating and Complete with BadDocument and Duplicate" in {
        val initStatus = Annotating( Set( "annotator-1", "annotator-2" ), 3 )
        val historyLine = testHistoryLine.copy( status = Status.FAILURE, message = "DUPLICATE" )
        val nextStatus = statusReader.getStatus( historyLine, initStatus )
        nextStatus shouldBe Duplicate

        val initStatus1 = Completed
        val historyLine1 = testHistoryLine.copy( status = Status.FAILURE, message = "DUPLICATE" )
        val nextStatus1 = statusReader.getStatus( historyLine1, initStatus1 )
        nextStatus1 shouldBe Duplicate

        val initStatus2 = Annotating( Set( "annotator-1", "annotator-2" ), 3 )
        val historyLine2 = testHistoryLine.copy( status = Status.FAILURE, applicationId = "DocumentProcessingError" )
        val nextStatus2 = statusReader.getStatus( historyLine2, initStatus2 )
        nextStatus2 shouldBe BadDocument

        val initStatus3 = Completed
        val historyLine3 = testHistoryLine.copy( status = Status.FAILURE, applicationId = "DocumentProcessingError" )
        val nextStatus3 = statusReader.getStatus( historyLine3, initStatus3 )
        nextStatus3 shouldBe BadDocument
    }


}
