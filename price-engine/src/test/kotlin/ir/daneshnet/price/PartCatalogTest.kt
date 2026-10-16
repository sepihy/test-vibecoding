package ir.daneshnet.price

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PartCatalogTest {
    @Test fun exactPartNumberWins() {
        PartCatalog.upsert(PartRecord("206-front-pad-a","پژو","۲۰۶","تیپ ۵",null,null,"لنت جلو","جلو","ایساکو","ABC-206-123",aliases=setOf("لنت ترمز جلو")))
        PartCatalog.upsert(PartRecord("206-rear-pad-b","پژو","۲۰۶","تیپ ۵",null,null,"لنت عقب","عقب","ایساکو","XYZ-206-999"))
        val r=PartCatalog.find(PartQuery(make="پژو",model="۲۰۶",trim="تیپ ۵",partName="لنت جلو",partNumber="ABC-206-123"))
        assertTrue(r.isNotEmpty())
        assertEquals("206-front-pad-a",r.first().record.canonicalKey)
    }
}
