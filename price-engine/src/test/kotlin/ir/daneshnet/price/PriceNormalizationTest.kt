package ir.daneshnet.price

import kotlin.test.Test
import kotlin.test.assertEquals

class PriceNormalizationTest {
    @Test fun PersianTomanIsConvertedToRial() {
        assertEquals(28_000_000L, PriceNormalization.toRial("۲,۸۰۰,۰۰۰ تومان"))
    }
    @Test fun RialStaysRial() {
        assertEquals(28_000_000L, PriceNormalization.toRial("۲۸,۰۰۰,۰۰۰ ریال"))
    }
}
