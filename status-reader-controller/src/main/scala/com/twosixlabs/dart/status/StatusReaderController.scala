package com.twosixlabs.dart.status

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonInclude, JsonProperty}
import com.twosixlabs.dart.exceptions.BadQueryParameterException
import com.twosixlabs.dart.rest.scalatra.DartScalatraServlet
import com.twosixlabs.dart.status.models.{DocId, DocStatus, ReaderId}
import com.twosixlabs.dart.status.services.DocIdsService
import org.scalatra.CorsSupport
import org.slf4j.{Logger, LoggerFactory}

import scala.beans.BeanProperty

@JsonInclude( Include.NON_EMPTY )
@JsonIgnoreProperties( ignoreUnknown = true )
case class StatusResult( @BeanProperty @JsonProperty( "document_id" ) document_id : Option[ String ] = None,
                         @BeanProperty @JsonProperty( "state" ) state : Option[ String ] = None,
                         @BeanProperty @JsonProperty( "source_uri" ) source_uri : Option[ String ] = None )

trait StatusReaderControllerDependencies {
    val statusReader : StatusReader
    val docIdsService : DocIdsService
}

class StatusReaderController( dependencies : StatusReaderControllerDependencies )
  extends DartScalatraServlet with CorsSupport {
    private val statusReader = dependencies.statusReader
    private val docIdsService = dependencies.docIdsService

    override val LOG : Logger = LoggerFactory.getLogger( getClass )

    setStandardConfig()

    get( "/" )( handleOutput {
        val docIdsParam = params.get( "docIds" )
        val filenamesParam = params.get( "filenames" )

        if (docIdsParam.isEmpty && filenamesParam.isEmpty)
            throw new BadQueryParameterException( List( "docIds", "filenames" ), Some( "must provide docIds, filenames, or both" ) )

        val docIdStatusMap : Map[ String, Map[String, String ] ] = docIdsParam match {
            case Some( docIdsString : String ) =>
                val docIds = docIdsString.split(',').map( _.trim ).toList
                LOG.info( "DOCIDS:")
                LOG.info( docIds.toString )

                docIds.foreach( d => if (d.length != 32) throw new BadQueryParameterException( "docIds", None, s"$d is not a valid document id" ))

                val statuses : Map[ DocId, Map[ ReaderId, DocStatus ] ] = statusReader.getDocStatus( docIds )

                statuses map { docIdTuple : (DocId, Map[ReaderId, DocStatus ]) =>
                    val (docId, readerMap) = docIdTuple
                    docId.docId -> ( readerMap map { readerTuple : (ReaderId, DocStatus) =>
                        val (readerId, docStatus) = readerTuple
                        readerId.id -> docStatus.msg
                    } )
                }
            case _ => Map[ String, Map[ String, String ] ]()
        }

        val filenameStatusMap = filenamesParam match {
            case Some( filenameString : String ) =>
                val filenames = filenameString.split(',').map( _.trim ).toList

                val sourceUriToDocIdMap : Map[ String, String ] = docIdsService.multiDocIdsFromSourceUris( filenames )
                val docIdToSourceUriMap = for { (k, v) <- sourceUriToDocIdMap } yield (v -> k)

                val statuses : Map[ DocId, Map[ ReaderId, DocStatus ] ] = statusReader.getDocStatus( sourceUriToDocIdMap.values.toSeq )

                statuses
                  .filterKeys( docId => docIdToSourceUriMap contains docId.docId )
                  .map { docIdTuple : (DocId, Map[ReaderId, DocStatus ]) =>
                      val (docId : DocId, readerMap : Map[ ReaderId, DocStatus ]) = docIdTuple
                      val statusMap = {
                          Map( "filename" -> docIdToSourceUriMap( docId.docId ) ) ++
                          readerMap.map( ( readerIdTuple : (ReaderId, DocStatus) ) => {
                              val (readerId, docStatus) = readerIdTuple
                              readerId.id -> docStatus.msg
                          } )
                      }
                      docId.docId -> statusMap
                  }
            case _ => Map[ String, Map[ String, String ] ]()
        }

        docIdStatusMap ++ filenameStatusMap
    } )
}
