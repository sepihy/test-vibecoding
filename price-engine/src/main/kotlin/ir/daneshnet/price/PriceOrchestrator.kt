package ir.daneshnet.price
data class PriceSearchResponse(val match:MatchResult,val snapshot:MarketSnapshot?,val accuracy:AccuracyReport?,val status:String)
class PriceOrchestrator(private val catalog:PartCatalogFacade,private val adapters:AdapterRegistry,private val repository:PriceRepository){
 suspend fun search(q:PartQuery,now:Long,margin:Double=10.0):PriceSearchResponse{
  val match=MatchDecisionEngine.decide(catalog.find(q));val r=match.record?:return PriceSearchResponse(match,null,null,"قطعه پیدا نشد")
  if(match.decision==MatchDecision.REJECT||match.decision==MatchDecision.REVIEW)return PriceSearchResponse(match,null,null,"تطبیق قطعه نیازمند بررسی است")
  val part=r.toNormalized();val obs=adapters.enabled().flatMap{runCatching{it.search(q)}.getOrElse{emptyList()}}.filter{it.partKey==r.canonicalKey}
  obs.forEach{repository.save(PricePoint(it.partKey,it.sourceId,it.priceRial,it.observedAtEpochMs,it.availability))}
  val snap=PriceEngine.snapshot(part,obs,now,margin);val result=AccuratePriceResultBuilder.build(snap,obs)
  return PriceSearchResponse(match,snap,result.accuracy,if(result.accuracy.canShowPrice)"قیمت قابل نمایش است" else "داده کافی برای قیمت قطعی نیست")
 }
}
interface PartCatalogFacade{fun find(query:PartQuery):List<PartMatch>}
fun PartRecord.toNormalized()=NormalizedPart(canonicalKey,"$make $model $partName",aliases,make,model,trim,partName,partNumber,brand,position)
class DefaultPartCatalogFacade:PartCatalogFacade{override fun find(q:PartQuery)=PartCatalog.find(q)}