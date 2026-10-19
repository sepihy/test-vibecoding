package ir.daneshnet.price
import kotlin.test.Test
import kotlin.test.assertTrue
class SourceReliabilityTest{
 @Test fun consistentSourceScoresHigher(){
  val p=(1..8).map{PriceObservation(it.toString(),"s",SourceType.RETAILER,3_000_000L+it*5_000,1000,.98,.95,partKey="p")}
  val r=SourceReliabilityEngine.calculate("s",p)
  assertTrue(r.reliabilityScore>=75)
 }
}