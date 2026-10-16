package ir.daneshnet.price

import kotlin.test.Test
import kotlin.test.assertEquals

class MatchDecisionTest {
    @Test fun ambiguousMatchNeedsReview() {
        val a=PartRecord("a","پژو","۲۰۶","تیپ ۵",null,null,"لنت جلو","جلو",null,"A")
        val b=PartRecord("b","پژو","۲۰۶","تیپ ۵",null,null,"لنت جلو","جلو",null,"B")
        val r=MatchDecisionEngine.decide(listOf(PartMatch(a,.88,listOf("نام و خودرو")),PartMatch(b,.86,listOf("نام و خودرو"))))
        assertEquals(MatchDecision.REVIEW,r.decision)
    }
}
