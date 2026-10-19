package ir.daneshnet.price

data class SourceReliability(
    val sourceId:String,
    val observations:Int,
    val validMatches:Int,
    val stale:Int,
    val outliers:Int,
    val consistency:Double,
    val reliabilityScore:Int
)
object SourceReliabilityEngine {
 fun calculate(sourceId:String, observations:List<PriceObservation>):SourceReliability{
  if(observations.isEmpty())return SourceReliability(sourceId,0,0,0,0,0.0,0)
  val valid=observations.count{it.matchScore>=.9 && it.priceRial>0}
  val stale=observations.count{it.observedAtEpochMs<=0}
  val sorted=observations.map{it.priceRial}.sorted()
  val med=if(sorted.size%2==1)sorted[sorted.size/2] else (sorted[sorted.size/2-1]+sorted[sorted.size/2])/2
  val outliers=observations.count{med>0 && kotlin.math.abs(it.priceRial-med).toDouble()/med>.35}
  val consistency=(1.0-outliers.toDouble()/observations.size).coerceIn(0.0,1.0)
  val score=(100*(.45*valid/observations.size+.40*consistency+.15*(1.0-stale.toDouble()/observations.size))).toInt().coerceIn(0,100)
  return SourceReliability(sourceId,observations.size,valid,stale,outliers,consistency,score)
 }
}