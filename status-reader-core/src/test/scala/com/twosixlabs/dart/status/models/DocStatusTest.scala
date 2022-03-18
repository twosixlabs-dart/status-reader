package com.twosixlabs.dart.status.models

import com.twosixlabs.dart.test.base.StandardTestBase3x
import annotations.WipTest

@WipTest
class DocStatusTest extends StandardTestBase3x {
    behavior of "DocStatus.unapply"

    it should "extract a status message" in {
        val status = DocStatus( "some message" )
        status match {
            case DocStatus( msg ) =>
                msg shouldBe "some message"

            case _ => fail( "did not match DocStatus!" )
        }
    }
}
