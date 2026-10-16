package ir.daneshnet.price

class PriceService(
    private val registry: SourceRegistry,
    private val catalog: List<NormalizedPart> = emptyList()
) {
    suspend fun lookup(query: PartQuery, nowMs: Long, marginPct: Double = 10.0): LookupResult {
        val match = PartMatcher.match(query, catalog)
        val resolved = match?.part ?: PartNormalizer.fromQuery(query)
        val observations = registry.searchAll(query)
            .filter { PriceValidator.validate(it, nowMs) }
            .let(PriceValidator::deduplicate)

        val snapshot = PriceEngine.snapshot(resolved, observations, nowMs, marginPct)
        return LookupResult(query, match, snapshot)
    }
}

data class LookupResult(
    val query: PartQuery,
    val match: MatchResult?,
    val snapshot: MarketSnapshot
)
