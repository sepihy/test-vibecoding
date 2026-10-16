package ir.daneshnet.price
interface PriceRepository{suspend fun save(point:PricePoint);suspend fun history(partKey:String,sinceEpochMs:Long):List<PricePoint>;suspend fun latest(partKey:String):List<PricePoint>}
class InMemoryPriceRepository:PriceRepository{
 private val items=mutableListOf<PricePoint>()
 override suspend fun save(p:PricePoint){synchronized(items){items+=p}}
 override suspend fun history(k:String,since:Long)=synchronized(items){items.filter{it.partKey==k&&it.observedAtEpochMs>=since}}
 override suspend fun latest(k:String)=synchronized(items){items.filter{it.partKey==k}.groupBy{it.sourceId}.values.mapNotNull{it.maxByOrNull{p->p.observedAtEpochMs}}}
}