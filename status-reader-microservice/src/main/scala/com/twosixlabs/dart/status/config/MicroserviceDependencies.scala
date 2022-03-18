package com.twosixlabs.dart.status.config

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.twosixlabs.dart.operations.status.client.{PipelineStatusQueryClient, SqlPipelineStatusQueryClient}
import com.twosixlabs.dart.sql.SqlClient
import com.twosixlabs.dart.status.readers.{DartStatusReader, DartStatusReaderDependencies}
import com.twosixlabs.dart.status.services.{CorpexDocIdService, CorpexDocIdServiceDependencies, DocIdsService}
import com.twosixlabs.dart.status.{OperationsStatusReader, OperationsStatusReaderDependencies, StatusReader, StatusReaderControllerDependencies}

trait DartStatusReaderDI extends DartStatusReaderDependencies with DartConfigDI {
    override val numAnnotators : Int = config.getInt( "number.annotators" )
}

object DartStatusReaderDI extends DartStatusReaderDI

trait OperationsStatusReaderDI extends OperationsStatusReaderDependencies with DartConfigDI {
    private val statusHost = config.getString( "status.pg.host" )
    private val statusPort = config.getInt( "status.pg.port" )
    private val statusDb = config.getString( "status.pg.db" )
    private val statusUser = config.getString( "status.pg.user" )
    private val statusPassword = config.getString( "status.pg.password" )
    private val statusTable = config.getString( "status.pg.table" )

    private val ds = new ComboPooledDataSource()
    ds.setDriverClass( "org.postgresql.Driver" )
    ds.setJdbcUrl( s"jdbc:postgresql://$statusHost:$statusPort/$statusDb" )
    ds.setUser( statusUser )
    ds.setPassword( statusPassword )
    ds.setMinPoolSize( 1 )
    ds.setAcquireIncrement( 1 )
    ds.setMaxPoolSize( 50 )

    override val operationsClient : PipelineStatusQueryClient = new SqlPipelineStatusQueryClient(
        new SqlClient( ds ),
        statusTable
    )
    override val dartReader : DartStatusReader = new DartStatusReader( DartStatusReaderDI )
}

object OperationsStatusReaderDI extends OperationsStatusReaderDI

trait CorpexDocIdServiceDI extends CorpexDocIdServiceDependencies with DartConfigDI {
    override val corpexHost : String = config.getString( "corpex.host" )
    override val corpexPort : Int = config.getInt( "corpex.port" )
    override val corpexSearchPath : String = config.getString( "corpex.search.path" )
}

object CorpexDocIdServiceDI extends CorpexDocIdServiceDI

trait StatusReaderControllerDI extends StatusReaderControllerDependencies {
    override val statusReader : StatusReader = new OperationsStatusReader( OperationsStatusReaderDI )
    override val docIdsService : DocIdsService = new CorpexDocIdService( CorpexDocIdServiceDI )
}

object StatusReaderControllerDI extends StatusReaderControllerDI
