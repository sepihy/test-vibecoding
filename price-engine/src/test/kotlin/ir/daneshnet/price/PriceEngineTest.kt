package ir.daneshnet.price

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PriceEngineTest {
    @Test
    fun filtersExtremeOutlierAndUsesWeightedMedian() {
        val now = 1_000_000_000L
        val part = NormalizedPart("206|front pad|x", "206 front pad X", emptySet(), "Peugeot", "206", "front pad", "X", null)
        val observations = listOf(
            obs("official", SourceType.OFFICIAL, 2_900_000, 1.0),
            obs("retail1", SourceType.RETAILER, 2_850_000, 0.95),
            obs("retail2", SourceType.RETAILER, 2_950_000, 0.90),
            obs("retail3", SourceType.RETAILER, 3_000_000, 0.90),
            obs("bad", SourceType.PUBLIC_MESSAGE, 9_900_000, 0.90)
        )
        val s = PriceEngine.snapshot(part, observations, now, 10.0)
        assertTrue(s.medianRial in 2_850_000L..3_000_000L)
        assertTrue(s.highRial!! < 9_900_000L)
        assertEquals(4, s.sourceCount)
    }

    private fun obs(id: String, type: SourceType, price: Long, match: Double) =
        PriceObservation(id, id, type, price, 999_000_000L, match)
}
