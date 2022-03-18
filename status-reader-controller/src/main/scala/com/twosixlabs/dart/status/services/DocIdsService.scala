package com.twosixlabs.dart.status.services

trait DocIdsService {
    def docIdFromSourceUri( sourceUri : String ) : Option[ String ]
    def multiDocIdsFromSourceUris( sourceUris : List[ String ] ) : Map[ String, String ]
    def sourceUrisFromDocIds( docIds : List[ String ] ) : Map[ String, String ]
}
