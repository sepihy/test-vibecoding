package ir.daneshnet.price

import kotlin.test.Test
import kotlin.test.assertEquals

class PriceDecisionTest {
    @Test fun noDataWhenNoObservations() {
        val p = NormalizedPart("x","x",emptySet(),null,null,null,null,null,null)
        val s = PriceEngine.snapshot(p, emptyList(), 1_000_000, 10.0)
        val r = LookupResult(PartQuery("x"), null, s)
        assertEquals(Decision.NO_DATA, PriceDecision.decide(r))
    }
}
