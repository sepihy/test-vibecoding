package ir.daneshnet.price
interface SourceAdapter{val config:SourceConfig;suspend fun search(part:PartQuery):List<PriceObservation>}
class AdapterRegistry{private val adapters=mutableMapOf<String,SourceAdapter>();fun register(a:SourceAdapter){adapters[a.config.id]=a};fun enabled()=adapters.values.filter{it.config.enabled}}