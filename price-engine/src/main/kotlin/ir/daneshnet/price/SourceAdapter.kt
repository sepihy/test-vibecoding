package ir.daneshnet.price

interface SourceAdapter {
    val sourceId: String
    val displayName: String
    val type: SourceType

    suspend fun search(query: PartQuery): List<PriceObservation>
}

class SourceRegistry(private val adapters: List<SourceAdapter>) {
    suspend fun searchAll(query: PartQuery): List<PriceObservation> =
        adapters.flatMap { adapter -> runCatching { adapter.search(query) }.getOrDefault(emptyList()) }
}
