package com.twosixlabs.dart.status.models

case class DocId( docId : String )

case class ReaderId(  id : String )

object ReaderId {
    def apply( id : String ) : ReaderId = new ReaderId( id )
    def unnapply( readerId : ReaderId ) : Option[ String ] = Some( readerId.id )
}

class DocStatus( val msg : String )

object DocStatus {
    def apply( msg : String ) : DocStatus = new DocStatus( msg )
    def unapply( status : DocStatus ) : Option[ String ] = Some( status.msg )
}

case object NoStatus extends DocStatus( "No Status" )
