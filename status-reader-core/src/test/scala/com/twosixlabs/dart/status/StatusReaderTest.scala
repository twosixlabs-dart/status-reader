package com.twosixlabs.dart.status

import com.twosixlabs.dart.operations.status.PipelineStatus
import com.twosixlabs.dart.operations.status.PipelineStatus.ProcessorType
import com.twosixlabs.dart.status.models.{DocStatus, NoStatus, ReaderId}
import com.twosixlabs.dart.test.base.StandardTestBase3x
import com.twosixlabs.dart.test.tags.WipTest
import org.scalamock.scalatest.MockFactory

class StatusReaderTest extends StandardTestBase3x with MockFactory {

    val docList = List( "fbe14fc8728f56a864bc83efb6611ecf", "cf7beea350d28a746b1c14c4338163ab", "a99caedfc078a7b02a5c91e09cb25be5", "3ec9d600b2d3d597f1158fda40ead87c" )

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

    private val mockRetrieveHistory = mockFunction[ String, List[ PipelineStatus ] ]

    behavior of "StatusReader.getDocStatus( docId : String )"

    it should "return an empty map if there are no readers" in {
        val statusReader = new StatusReader {
            override val readers : Set[ SingleStatusReader ] = Set[ SingleStatusReader ]()
            override def retrieveHistory( docId : String ) : List[ PipelineStatus ] = mockRetrieveHistory( docId )
        }

        mockRetrieveHistory
          .expects( * )
          .onCall( ( docId : String ) => fullPlHistory.filter( _.getDocumentId == docId ) )

        val statuses = statusReader.getDocStatus( docList.head )
        statuses shouldBe Map[ ReaderId, DocStatus ]()
    }

    it should "return No Status if there is a reader with no history" in {
        val statusReader = new StatusReader {
            override val readers : Set[ SingleStatusReader ] = Set[ SingleStatusReader ]( new SingleStatusReader {
                override val id : ReaderId = ReaderId( "test" )
                case object SomeStatus extends DocStatus( "some-msg" )
                override def getStatus( historyLine : PipelineStatus,
                                        prevStatus : DocStatus ) : DocStatus = {
                    SomeStatus
                }
            })
            override def retrieveHistory( docId : String ) : List[ PipelineStatus ] = mockRetrieveHistory( docId )
        }

        mockRetrieveHistory.expects( * ).returning( Nil )

        val statuses = statusReader.getDocStatus( docList.head )
        statuses shouldBe Map( ReaderId( "test" ) -> NoStatus )
    }

    it should "generate the same results no matter the order of the input history" in {
        case object FirstStatus extends DocStatus( "msg-1" )
        case object SecondStatus extends DocStatus( "msg-2" )
        case object ThirdStatus extends DocStatus( "msg-3" )

        val statusReader = new StatusReader {
            override val readers : Set[ SingleStatusReader ] = Set[ SingleStatusReader ]( new SingleStatusReader {
                override val id : ReaderId = ReaderId( "test" )
                override def getStatus( historyLine : PipelineStatus,
                                        prevStatus : DocStatus ) : DocStatus = {
                    historyLine.getApplicationId match {
                        case "test-app-id-1" => FirstStatus
                        case "test-app-id-2" => SecondStatus
                        case "test-app-id-3" => ThirdStatus
                        case sts => prevStatus
                    }
                }
            })

            override def retrieveHistory( docId : String ) : List[ PipelineStatus ] = mockRetrieveHistory( docId )
        }
        mockRetrieveHistory.expects( * ).returning( fullPlHistory.filter( _.getDocumentId == docList.head ).reverse )
        val statuses = statusReader.getDocStatus( docList.head )
        statuses shouldBe Map( ReaderId( "test" ) -> ThirdStatus )
    }

    it should "update complex statuses expectedly" taggedAs( WipTest ) in {
        case class ComplexStatus( appIds : List[ String ] ) extends DocStatus( appIds.mkString(", ") )

        val statusReader = new StatusReader {
            override val readers : Set[ SingleStatusReader ] = Set[ SingleStatusReader ]( new SingleStatusReader {
                override val id : ReaderId = ReaderId( "test" )
                override def getStatus( historyLine : PipelineStatus,
                                        prevStatus : DocStatus ) : DocStatus = {
                    prevStatus match {
                        case NoStatus =>
                            ComplexStatus( List( historyLine.getApplicationId ) )
                        case ComplexStatus( appIdList ) =>
                            ComplexStatus( appIdList :+ historyLine.getApplicationId )
                        case otherState => otherState
                    }
                }
            } )

            override def retrieveHistory( docId : String ) : List[ PipelineStatus ] = mockRetrieveHistory( docId )
        }
        mockRetrieveHistory.expects( * ).returning( fullPlHistory.filter( _.getDocumentId == docList.head ).reverse )
        val statuses = statusReader.getDocStatus( docList.head )
        statuses shouldBe Map( ReaderId( "test" ) -> ComplexStatus( List( "test-app-id-1", "test-app-id-2", "test-app-id-3" ) ) )
    }
}
