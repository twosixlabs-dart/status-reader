package com.twosixlabs.dart.status.models

import com.github.tminglei.slickpg._
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities


trait PgStatusProfile extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support {
    def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

    // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
    override protected def computeCapabilities : Set[ Capability ] =
        super.computeCapabilities + JdbcCapabilities.insertOrUpdate

    override val api : API = PgCorpexApi

    object PgCorpexApi extends API
      with ArrayImplicits
      with DateTimeImplicits

}

object PgStatusProfile extends PgStatusProfile