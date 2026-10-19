package ir.daneshnet.price

class PricePipeline(
    private val registry: SourceRegistry
) {
    suspend fun run(query: PartQuery, nowMs: Long, marginPct: Double = 10.0): MarketSnapshot {
        val part = PartNormalizer.fromQuery(query)
        val observations = registry.searchAll(query)
        return PriceEngine.snapshot(part, observations, nowMs, marginPct)
    }
}
