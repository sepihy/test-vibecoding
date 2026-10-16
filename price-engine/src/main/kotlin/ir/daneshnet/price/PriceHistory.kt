package ir.daneshnet.price

data class PricePoint(val partKey:String,val sourceId:String,val priceRial:Long,val observedAtEpochMs:Long,val availability:Availability)
data class PriceChange(val partKey:String,val previousMedianRial:Long?,val currentMedianRial:Long?,val changePercent:Double?,val direction:Direction){enum class Direction{UP,DOWN,STABLE,UNKNOWN}}
object PriceHistoryAnalyzer{
 fun compare(k:String,p:List<PricePoint>,c:List<PricePoint>):PriceChange{
  val a=median(p.filter{it.availability!=Availability.OUT_OF_STOCK}.map{it.priceRial}); val b=median(c.filter{it.availability!=Availability.OUT_OF_STOCK}.map{it.priceRial})
  if(a==null||b==null)return PriceChange(k,a,b,null,PriceChange.Direction.UNKNOWN)
  val pct=(b-a)*100.0/a; val d=when{pct>1->PriceChange.Direction.UP;pct< -1->PriceChange.Direction.DOWN;else->PriceChange.Direction.STABLE}
  return PriceChange(k,a,b,pct,d)
 }
 private fun median(v:List<Long>):Long?{if(v.isEmpty())return null;val s=v.sorted();return if(s.size%2==1)s[s.size/2] else (s[s.size/2-1]+s[s.size/2])/2}
}