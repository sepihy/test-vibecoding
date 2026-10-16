package ir.daneshnet.price

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DocumentOfferParserTest {
    @Test fun parsesPersianPriceLine() {
        val r = DocumentOfferParser.parse("لنت جلو پژو ۲۰۶ تیپ ۵ ایساکو ۲,۸۰۰,۰۰۰ تومان")
        assertEquals(1, r.size)
        assertEquals(28_000_000L, r.first().priceRial)
        assertNotNull(r.first().vehicle)
        assertTrue(r.first().parseConfidence >= .7)
    }
}
