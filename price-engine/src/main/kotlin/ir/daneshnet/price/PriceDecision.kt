package ir.daneshnet.price

data class PriceDecision(
 val status:Status,
 val marketPriceRial:Long?,
 val suggestedSaleRial:Long?,
 val confidence:Int,
 val explanation:List<String>
){enum class Status{READY,REVIEW,INSUFFICIENT}}

object PriceDecisionEngine{
 fun decide(snapshot:MarketSnapshot,accuracy:AccuracyReport,reliability:List<SourceReliability>,marginPct:Double=10.0):PriceDecision{
  val reasons=mutableListOf<String>()
  if(!accuracy.canShowPrice)reasons+="شرایط دقت برای قیمت قطعی کامل نیست"
  val strong=reliability.count{it.reliabilityScore>=75}
  if(strong<2)reasons+="حداقل دو منبع قابل‌اعتماد نداریم"
  val ready=accuracy.canShowPrice&&strong>=2&&snapshot.medianRial!=null
  val sale=snapshot.medianRial?.let{(it*(1+marginPct/100)).toLong()}
  return PriceDecision(if(ready)Status.READY else if(snapshot.medianRial!=null)Status.REVIEW else Status.INSUFFICIENT,if(ready)snapshot.medianRial else null,if(ready)sale else null,snapshot.confidence,reasons)
 }
}