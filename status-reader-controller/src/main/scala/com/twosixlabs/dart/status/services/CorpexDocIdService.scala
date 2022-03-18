package com.twosixlabs.dart.status.services

import com.twosixlabs.dart.corpex.api.enums.BoolType
import com.twosixlabs.dart.corpex.api.models.queries.CorpexTermQuery
import com.twosixlabs.dart.corpex.api.models.{ CorpexSearchRequest, CorpexSearchResults, CorpexSingleResult }
import com.twosixlabs.dart.corpex.api.tools.Mapper

import java.io.IOException
import com.twosixlabs.dart.exceptions.{ Exceptions, ServiceUnreachableException }
import okhttp3.{ MediaType, OkHttpClient, Request, RequestBody, Response }
import org.slf4j.{ Logger, LoggerFactory }

trait CorpexDocIdServiceDependencies {
    val corpexHost : String
    val corpexPort : Int
    val corpexSearchPath : String
}

class CorpexDocIdService( dependencies : CorpexDocIdServiceDependencies ) extends DocIdsService  {
    private val searchUrl = s"http://${dependencies.corpexHost}:${dependencies.corpexPort}${dependencies.corpexSearchPath}"

    private val LOG : Logger = LoggerFactory.getLogger( getClass )
    private val JSON : MediaType = MediaType.get( "application/json; charset=utf-8" )
    lazy val okClient : OkHttpClient = new OkHttpClient()

    override def docIdFromSourceUri( sourceUri : String ) : Option[ String ] = {
        val corpexRequest = CorpexSearchRequest( fields = Some( List( "cdr.document_id" ) ),
                                                 queries = Some( List( CorpexTermQuery( BoolType.FILTER,
                                                                                        queriedField = "cdr.source_uri",
                                                                                        termValues = List( sourceUri ),
                                                                                        valuesBoolType = None ) ) ) )

        try {
            val requestJson = Mapper.marshal( corpexRequest )

            val body = RequestBody.create( requestJson, JSON )
            val request : Request = new Request.Builder().url( searchUrl ).post( body ).build()
            val response : Response = okClient.newCall( request ).execute()

            response.code() match {
                // deserialize json (response.body.string()) into DuplicateProbability object using Jackson mapper
                case 200 =>
                    val resJson = response.body.string()
                    val corpexResponse = Mapper.unmarshal( resJson, classOf[ CorpexSearchResults ] )
                    corpexResponse.results flatMap { results : List[ CorpexSingleResult ] =>
                        results.headOption
                    } flatMap  { result : CorpexSingleResult =>
                        Option( result.cdr.documentId )
                    }

                case _ =>
                    LOG.error( s"Error executing remote REST request : ${response.code} : ${response.body.string}" )
                    throw new ServiceUnreachableException( "search datastore" )
            }
        } catch {
            case e : IOException =>
                LOG.error( s"Exception caught trying to communicate with service: ${e.getMessage} : ${e.getCause}" )
                LOG.error( Exceptions.getStackTraceText( e ) )
                throw new ServiceUnreachableException( "search datastore" )

        }
    }

    override def multiDocIdsFromSourceUris( sourceUris : List[ String ] ) : Map[ String, String ] = {

        if ( sourceUris.isEmpty ) Map()
        else {
            val corpexRequest = CorpexSearchRequest( pageSize = Some( 1000 ),
                                                     fields = Some( List( "cdr.document_id", "cdr.source_uri" ) ),
                                                     queries = Some( List( CorpexTermQuery( BoolType.FILTER,
                                                                                            queriedField = "cdr.source_uri",
                                                                                            termValues = sourceUris,
                                                                                            valuesBoolType = Some( BoolType.SHOULD ) ) ) ) )

            try {
                val requestJson = Mapper.marshal( corpexRequest )

                val body = RequestBody.create( requestJson, JSON )
                val request : Request = new Request.Builder().url( searchUrl ).post( body ).build()
                val response : Response = okClient.newCall( request ).execute()

                response.code() match {
                    // deserialize json (response.body.string()) into DuplicateProbability object using Jackson mapper
                    case 200 =>
                        val resJson = response.body.string()
                        val corpexResponse = Mapper.unmarshal( resJson, classOf[ CorpexSearchResults ] )
                        corpexResponse.results match {
                            case None => Map()
                            case Some( results : List[ CorpexSingleResult ] ) =>
                                results flatMap { result =>
                                    val docIdOpt = Option( result.cdr.documentId )
                                    val sourceUriOpt = Option( result.cdr.sourceUri )
                                    (for {
                                        docId <- docIdOpt
                                        sourceUri <- sourceUriOpt
                                    } yield (sourceUri -> docId)).toList
                                } toMap
                        }

                    case _ =>
                        LOG.error( s"Error executing remote REST request : ${response.code} : ${response.body.string}" )
                        throw new ServiceUnreachableException( "search datastore" )
                }
            } catch {
                case e : IOException =>
                    LOG.error( s"Exception caught trying to communicate with service: ${e.getMessage} : ${e.getCause}" )
                    LOG.error( Exceptions.getStackTraceText( e ) )
                    throw new ServiceUnreachableException( "search datastore" )

            }
        }
    }

    override def sourceUrisFromDocIds( docIds : List[ String ] ) : Map[ String, String ] = {
        if ( docIds.isEmpty ) Map()
        else {
            val corpexRequest = CorpexSearchRequest( pageSize = Some( 1000 ),
                                                     fields = Some( List( "cdr.document_id", "cdr.source_uri" ) ),
                                                     queries = Some( List( CorpexTermQuery( BoolType.FILTER,
                                                                                            queriedField = "cdr.document_id",
                                                                                            termValues = docIds,
                                                                                            valuesBoolType = Some( BoolType.SHOULD ) ) ) ) )

            try {
                val requestJson = Mapper.marshal( corpexRequest )

                val body = RequestBody.create( requestJson, JSON )
                val request : Request = new Request.Builder().url( searchUrl ).post( body ).build()
                val response : Response = okClient.newCall( request ).execute()

                response.code() match {
                    // deserialize json (response.body.string()) into DuplicateProbability object using Jackson mapper
                    case 200 =>
                        val resJson = response.body.string()
                        val corpexResponse = Mapper.unmarshal( resJson, classOf[ CorpexSearchResults ] )
                        corpexResponse.results match {
                            case None => Map()
                            case Some( results : List[ CorpexSingleResult ] ) =>
                                results flatMap { result =>
                                    val docIdOpt = Option( result.cdr.documentId )
                                    val sourceUriOpt = Option( result.cdr.sourceUri )
                                    (for {
                                        docId <- docIdOpt
                                        sourceUri <- sourceUriOpt
                                    } yield (docId -> sourceUri)).toList
                                } toMap
                        }

                    case _ =>
                        LOG.error( s"Error executing remote REST request : ${response.code} : ${response.body.string}" )
                        throw new ServiceUnreachableException( "search datastore" )
                }
            } catch {
                case e : IOException =>
                    LOG.error( s"Exception caught trying to communicate with service: ${e.getMessage} : ${e.getCause}" )
                    LOG.error( Exceptions.getStackTraceText( e ) )
                    throw new ServiceUnreachableException( "search datastore" )

            }
        }
    }
}
