package com.twosixlabs.dart.status

import com.twosixlabs.dart.operations.status.PipelineStatus
import com.twosixlabs.dart.operations.status.PipelineStatus.ProcessorType
import com.twosixlabs.dart.status.models.{DocStatus, NoStatus, ReaderId}
import com.twosixlabs.dart.status.services.DocIdsService
import com.twosixlabs.dart.test.base.StandardTestBase3x
import org.scalamock.scalatest.MockFactory
import org.scalatra.test.scalatest.ScalatraSuite
import org.slf4j.{Logger, LoggerFactory}

class StatusReaderControllerTest extends StandardTestBase3x with ScalatraSuite with MockFactory {

    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    val docList = List( "fbe14fc8728f56a864bc83efb6611ecf", "cf7beea350d28a746b1c14c4338163ab", "a99caedfc078a7b02a5c91e09cb25be5", "3ec9d600b2d3d597f1158fda40ead87c" )
    val sourcUriList = List( "test-file-name-1", "test-file-name-2", "test-file-name-3", "test-file-name-4" )

    val testPlStatus1 = new PipelineStatus( docList.head, "test-app-id-1", ProcessorType.CORE, PipelineStatus.Status.SUCCESS, "", 100, 100, "test-status-1" )
    val testPlStatus2 = new PipelineStatus( docList( 1 ), "test-app-id-1", ProcessorType.CORE, PipelineStatus.Status.SUCCESS, "", 150, 150, "test-status-2" )
    val testPlStatus3 = new PipelineStatus( docList( 2 ), "test-app-id-1", ProcessorType.CORE, PipelineStatus.Status.SUCCESS, "", 200, 200, "test-status-3" )
    val testPlStatus4 = new PipelineStatus( docList( 3 ), "test-app-id-1", ProcessorType.CORE, PipelineStatus.Status.SUCCESS, "", 250, 250, "test-status-4" )
    val testPlStatus5 = new PipelineStatus( docList.head, "test-app-id-2", ProcessorType.ANNOTATOR, PipelineStatus.Status.FAILURE, "", 300, 300, "test-status-5" )
    val testPlStatus6 = new PipelineStatus( docList( 1 ), "test-app-id-2", ProcessorType.ANNOTATOR, PipelineStatus.Status.FAILURE, "", 350, 350, "test-status-6" )
    val testPlStatus7 = new PipelineStatus( docList( 2 ), "test-app-id-2", ProcessorType.ANNOTATOR, PipelineStatus.Status.FAILURE, "", 400, 400, "test-status-7" )
    val testPlStatus8 = new PipelineStatus( docList( 3 ), "test-app-id-2", ProcessorType.ANNOTATOR, PipelineStatus.Status.FAILURE, "", 450, 450, "test-status-8" )
    val testPlStatus9 = new PipelineStatus( docList.head, "test-app-id-3", ProcessorType.READER, PipelineStatus.Status.SUCCESS, "", 500, 500, "test-status-9" )
    val testPlStatus10 = new PipelineStatus( docList( 1 ), "test-app-id-3", ProcessorType.READER, PipelineStatus.Status.SUCCESS, "", 550, 550, "test-status-10" )
    val testPlStatus11 = new PipelineStatus( docList( 2 ), "test-app-id-3", ProcessorType.READER, PipelineStatus.Status.SUCCESS, "", 600, 600, "test-status-11" )
    val testPlStatus12 = new PipelineStatus( docList( 3 ), "test-app-id-3", ProcessorType.READER, PipelineStatus.Status.SUCCESS, "", 650, 650, "test-status-12" )

    val fullPlHistory = List( testPlStatus1, testPlStatus2, testPlStatus3, testPlStatus4, testPlStatus5, testPlStatus6, testPlStatus7, testPlStatus8, testPlStatus9, testPlStatus10, testPlStatus11, testPlStatus12 )

    object Fake1Status1 extends DocStatus( "fake-status-1" )
    object Fake1Status2 extends DocStatus( "fake-status-2" )

    val mockedGetStatus1 = mockFunction[ PipelineStatus, DocStatus, DocStatus ]
    val mockedReader1 = new SingleStatusReader {
        override val id : ReaderId = ReaderId( "test-1" )

        override def getStatus( historyLine : PipelineStatus,
                                prevStatus : DocStatus ) : DocStatus = mockedGetStatus1( historyLine, prevStatus )
    }

    object Fake2Status1 extends DocStatus( "fake-status-1" )
    object Fake2Status2 extends DocStatus( "fake-status-2" )

    val mockedGetStatus2 = mockFunction[ PipelineStatus, DocStatus, DocStatus ]
    val mockedReader2 = new SingleStatusReader {
        override val id : ReaderId = ReaderId( "test-2" )

        override def getStatus( historyLine : PipelineStatus,
                                prevStatus : DocStatus ) : DocStatus = mockedGetStatus2( historyLine, prevStatus )
    }

    val mockHistoryGenerator = mockFunction[ String, List[ PipelineStatus ] ]
    val mockdocIdsFromSourceUris = mockFunction[ List[ String ], Map[ String, String ] ]
    val dependencies : StatusReaderControllerDependencies = new StatusReaderControllerDependencies {
        override val statusReader : StatusReader = new StatusReader {
            override val readers : Set[ SingleStatusReader ] = Set( mockedReader1, mockedReader2 )

            override def retrieveHistory( docId : String ) : List[ PipelineStatus ] = mockHistoryGenerator( docId )
        }
        override val docIdsService : DocIdsService = new DocIdsService {
            override def docIdFromSourceUri( sourceUri : String ) : Option[ String ] = ???
            override def multiDocIdsFromSourceUris( sourceUris : List[ String ] ) : Map[ String, String ] = mockdocIdsFromSourceUris( sourceUris )
            override def sourceUrisFromDocIds( docIds : List[ String ] ) : Map[ String, String ] = ???
        }
    }

    addServlet( new StatusReaderController( dependencies ), "/*" )

    it should "return map with No Status when there is no history for doc ids" in {
        mockHistoryGenerator.expects( * ).returning( Nil ).anyNumberOfTimes()

        get( "/?docIds=fbe14fc8728f56a864bc83efb6611ecf,cf7beea350d28a746b1c14c4338163ab,a99caedfc078a7b02a5c91e09cb25be5,3ec9d600b2d3d597f1158fda40ead87c" ) {
            status shouldBe 200
            body shouldBe """{"fbe14fc8728f56a864bc83efb6611ecf":{"test-1":"No Status","test-2":"No Status"},"cf7beea350d28a746b1c14c4338163ab":{"test-1":"No Status","test-2":"No Status"},"a99caedfc078a7b02a5c91e09cb25be5":{"test-1":"No Status","test-2":"No Status"},"3ec9d600b2d3d597f1158fda40ead87c":{"test-1":"No Status","test-2":"No Status"}}"""
        }
    }

    it should "return an appropriate 400 error response is docIds parameter is missing" in {
        get( "/" ) {
            status shouldBe 400
            body should (include( "must provide docIds, filenames, or both") and include( "Bad request: invalid query: parameters docIds, filenames" ) )
        }
    }

    it should "return an appropriate 400 error response if a docId is not 32 characters" in {
        get( "/?docIds=fbe14fc8728f56a864bc83efb6611ecf,cf7beea350d28a746b1c14c4338163ab,a99caedfc078a7b02a5c91e09cb25be5abcdefg,3ec9d600b2d3d597f1158fda40ead87c" ) {
            status shouldBe 400
            body should (include( "a99caedfc078a7b02a5c91e09cb25be5abcdefg is not a valid document id") and include( "Bad request: invalid query: parameter docIds" ) )
        }
    }

    it should "return a json array of doc status objects if docIds are valid" in {
        mockHistoryGenerator
          .expects( * )
          .onCall( ( docId : String ) => fullPlHistory.filter( _.getDocumentId == docId ) )
          .anyNumberOfTimes()

        mockedGetStatus1
          .expects( *, * )
          .onCall( ( plStatus : PipelineStatus, docStatus : DocStatus ) => docStatus match {
              case NoStatus => Fake1Status1
              case Fake1Status1 => Fake1Status2
              case Fake1Status2 => Fake1Status2
              case otherStatus => otherStatus
          }).anyNumberOfTimes()

        mockedGetStatus2
          .expects( *, * )
          .onCall( ( plStatus : PipelineStatus, docStatus : DocStatus ) => docStatus match {
              case NoStatus => Fake2Status1
              case Fake2Status1 => Fake2Status2
              case Fake2Status2 => Fake2Status2
              case otherStatus => otherStatus
          }).anyNumberOfTimes()

        get( "/?docIds=fbe14fc8728f56a864bc83efb6611ecf,cf7beea350d28a746b1c14c4338163ab,a99caedfc078a7b02a5c91e09cb25be5,3ec9d600b2d3d597f1158fda40ead87c" ) {
            status shouldBe 200
            body shouldBe """{"fbe14fc8728f56a864bc83efb6611ecf":{"test-1":"fake-status-2","test-2":"fake-status-2"},"cf7beea350d28a746b1c14c4338163ab":{"test-1":"fake-status-2","test-2":"fake-status-2"},"a99caedfc078a7b02a5c91e09cb25be5":{"test-1":"fake-status-2","test-2":"fake-status-2"},"3ec9d600b2d3d597f1158fda40ead87c":{"test-1":"fake-status-2","test-2":"fake-status-2"}}"""
        }
    }

    it should "return map with No Status when there is no history for source uris" in {
        mockdocIdsFromSourceUris
          .expects( * )
          .onCall( (filenames : List[ String ]) => filenames.zip( docList ).toMap )

        mockHistoryGenerator.expects( * ).returning( Nil ).anyNumberOfTimes()

        get( s"/?filenames=${sourcUriList.mkString(",")}" ) {
            status shouldBe 200
            body shouldBe """{"fbe14fc8728f56a864bc83efb6611ecf":{"filename":"test-file-name-1","test-1":"No Status","test-2":"No Status"},"cf7beea350d28a746b1c14c4338163ab":{"filename":"test-file-name-2","test-1":"No Status","test-2":"No Status"},"a99caedfc078a7b02a5c91e09cb25be5":{"filename":"test-file-name-3","test-1":"No Status","test-2":"No Status"},"3ec9d600b2d3d597f1158fda40ead87c":{"filename":"test-file-name-4","test-1":"No Status","test-2":"No Status"}}"""
        }
    }

    it should "return a json array of doc status objects if filenames are valid" in {
        mockdocIdsFromSourceUris
          .expects( * )
          .onCall( (filenames : List[ String ]) => filenames.zip( docList ).toMap )

        mockHistoryGenerator
          .expects( * )
          .onCall( ( docId : String ) => fullPlHistory.filter( _.getDocumentId == docId ) )
          .anyNumberOfTimes()

        mockedGetStatus1
          .expects( *, * )
          .onCall( ( plStatus : PipelineStatus, docStatus : DocStatus ) => docStatus match {
              case NoStatus => Fake1Status1
              case Fake1Status1 => Fake1Status2
              case Fake1Status2 => Fake1Status2
              case otherStatus => otherStatus
          }).anyNumberOfTimes()

        mockedGetStatus2
          .expects( *, * )
          .onCall( ( plStatus : PipelineStatus, docStatus : DocStatus ) => docStatus match {
              case NoStatus => Fake2Status1
              case Fake2Status1 => Fake2Status2
              case Fake2Status2 => Fake2Status2
              case otherStatus => otherStatus
          }).anyNumberOfTimes()

        get( s"/?filenames=${sourcUriList.mkString(",")}" ) {
            status shouldBe 200
            body shouldBe """{"fbe14fc8728f56a864bc83efb6611ecf":{"filename":"test-file-name-1","test-1":"fake-status-2","test-2":"fake-status-2"},"cf7beea350d28a746b1c14c4338163ab":{"filename":"test-file-name-2","test-1":"fake-status-2","test-2":"fake-status-2"},"a99caedfc078a7b02a5c91e09cb25be5":{"filename":"test-file-name-3","test-1":"fake-status-2","test-2":"fake-status-2"},"3ec9d600b2d3d597f1158fda40ead87c":{"filename":"test-file-name-4","test-1":"fake-status-2","test-2":"fake-status-2"}}"""
        }
    }

    it should "return nothing when no source uris exist" in {
        mockdocIdsFromSourceUris
          .expects( * )
          .onCall( (filenames : List[ String ]) => Map[ String, String ]() )

        get( s"/?filenames=non-existent-filename-1,non-existent-filename-2" ) {
            status shouldBe 200
            body shouldBe """{}"""
        }
    }

    it should "return appropriate results when both docIds and filenames are included (non-existent docIds return no status, non-existing filenames don't return anything, and only 'filename' keyed docs include a 'filename' field in results" in {
        val allDocs : List[ (String, String) ] = sourcUriList.zip( docList ).take( 3 )
        val filenameDocs : List[ (String, String) ] = allDocs.take( 2 )
        val plHistory = fullPlHistory.filter( ps => allDocs.map( _._2 ).contains( ps.getDocumentId ) )

        mockdocIdsFromSourceUris
          .expects( * )
          .onCall( (filenames : List[ String ]) => filenameDocs.toMap )

        mockHistoryGenerator
          .expects( * )
          .onCall( ( docId : String ) => plHistory.filter( _.getDocumentId == docId ) )
          .anyNumberOfTimes()

        mockedGetStatus1
          .expects( *, * )
          .onCall( ( plStatus : PipelineStatus, docStatus : DocStatus ) => docStatus match {
              case NoStatus => Fake1Status1
              case Fake1Status1 => Fake1Status2
              case Fake1Status2 => Fake1Status2
              case otherStatus => otherStatus
          }).anyNumberOfTimes()

        mockedGetStatus2
          .expects( *, * )
          .onCall( ( plStatus : PipelineStatus, docStatus : DocStatus ) => docStatus match {
              case NoStatus => Fake2Status1
              case Fake2Status1 => Fake2Status2
              case Fake2Status2 => Fake2Status2
              case otherStatus => otherStatus
          }).anyNumberOfTimes()

        get( s"/?filenames=${filenameDocs.map( _._1 ).mkString(",")}&docIds=${docList.drop(2).mkString(",")}" ) {
            status shouldBe 200
            LOG.info( body )
            body shouldBe """{"a99caedfc078a7b02a5c91e09cb25be5":{"test-1":"fake-status-2","test-2":"fake-status-2"},"3ec9d600b2d3d597f1158fda40ead87c":{"test-1":"No Status","test-2":"No Status"},"fbe14fc8728f56a864bc83efb6611ecf":{"filename":"test-file-name-1","test-1":"fake-status-2","test-2":"fake-status-2"},"cf7beea350d28a746b1c14c4338163ab":{"filename":"test-file-name-2","test-1":"fake-status-2","test-2":"fake-status-2"}}"""
        }
    }

}
