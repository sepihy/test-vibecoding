package ir.daneshnet.price

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccuracyPolicyTest {
    private fun part() = NormalizedPart("p","لنت ۲۰۶",emptySet(),"پژو","۲۰۶",null,"لنت",null,null)
    private fun obs(id:String, price:Long, score:Double=.98) = PriceObservation(id,id,SourceType.RETAILER,price,1_000_000,score,1.0,partKey="p")
    @Test fun refusesSingleSource() {
        val s=PriceEngine.snapshot(part(),listOf(obs("a",3_000_000)),1_000_000)
        assertFalse(AccuracyPolicyEngine.evaluate(s,listOf(obs("a",3_000_000))).canShowPrice)
    }
    @Test fun acceptsSeveralStableSources() {
        val os=listOf(obs("a",3_000_000),obs("b",3_050_000),obs("c",3_100_000))
        val s=PriceEngine.snapshot(part(),os,1_000_000)
        assertTrue(AccuracyPolicyEngine.evaluate(s,os).canShowPrice)
    }
}
