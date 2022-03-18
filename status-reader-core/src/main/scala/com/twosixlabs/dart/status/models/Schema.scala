package com.twosixlabs.dart.status

import java.time.OffsetDateTime
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty}
import com.twosixlabs.dart.status.models.PgStatusProfile.api._
import slick.lifted.ProvenShape

import scala.beans.BeanProperty

package object models {

    trait Selector {
        def sortFields : Map[ String, Rep[ _ ] ]
    }

    /**
     * id SERIAL PRIMARY KEY,
     * document_id TEXT NOT NULL,
     * application_id TEXT NOT NULL,
     * processor_type TEXT NOT NULL,
     * status TEXT NOT NULL,
     * scope TEXT NOT NULL,
     * start_time BIGINT NOT NULL,
     * end_Time BIGINT NOT NULL,
     * message TEXT
     */

    @JsonInclude( Include.NON_EMPTY )
    case class OperationsRow( @BeanProperty @JsonProperty( "id" ) id : Option[ Int ],
                              @BeanProperty @JsonProperty( "document_id" ) documentId : String,
                              @BeanProperty @JsonProperty( "application_id" ) applicationId : String,
                              @BeanProperty @JsonProperty( "processor_type" ) processorType : String,
                              @BeanProperty @JsonProperty( "status" ) status : String,
                              @BeanProperty @JsonProperty( "scope" ) scope : String,
                              @BeanProperty @JsonProperty( "start_time" ) startTime : Long,
                              @BeanProperty @JsonProperty( "end_time" ) endTime : Long,
                              @BeanProperty @JsonProperty( "message" ) message : String )

    class OperationsTable( tag : Tag ) extends Table[ OperationsRow ]( tag : Tag,"pipeline_status" ) with Selector {

        def id : Rep[ Option[ Int ] ] = column[ Option[ Int ] ]( "id", O.PrimaryKey, O.AutoInc )
        def documentId : Rep[ String ] = column[ String ]( "document_id" )
        def applicationId : Rep[ String ] = column[ String ]( "application_id" )
        def processorType : Rep[ String ] = column[ String ]( "processor_type" )
        def status : Rep[ String ] = column[ String ]( "status" )
        def scope : Rep[ String ] = column[ String ]( "scope" )
        def startTime : Rep[ Long ] = column[ Long ]( "start_time" )
        def endTime : Rep[ Long ] = column[ Long ]( "end_time" )
        def message : Rep[ String ] = column[ String ]( "message" )

        override def * : ProvenShape[ OperationsRow ] =
            ( id, documentId, applicationId, processorType, status, scope, startTime, endTime, message ) <>
            (OperationsRow.tupled, OperationsRow.unapply)

        val sortFields : Map[ String, Rep[ _ ] ] =
            Map( "startTime" -> this.startTime,
                 "endTime" -> this.endTime )
    }

    val operationsQuery = TableQuery[ OperationsTable ]
}
