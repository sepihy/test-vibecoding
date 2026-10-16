package ir.daneshnet.price

class PriceService(private val registry: SourceRegistry, private val catalog: List<NormalizedPart> = emptyList()) {
    suspend fun lookup(query: PartQuery, nowMs: Long, marginPct: Double = 10.0): LookupResult {
        val parsed = if (query.make == null || query.model == null) QueryParser.parse(query.raw) else null
        val effective = parsed?.query?.copy(
            partName = query.partName,
            brand = query.brand,
            year = query.year,
            partNumber = query.partNumber ?: parsed.query.partNumber
        ) ?: query
        val match = PartMatcher.match(effective, catalog)
        val resolved = match?.part ?: PartNormalizer.fromQuery(effective)
        val observations = registry.searchAll(effective)
            .filter { PriceValidator.validate(it, nowMs) }
            .let(PriceValidator::deduplicate)
        return LookupResult(effective, match, PriceEngine.snapshot(resolved, observations, nowMs, marginPct))
    }
}

data class LookupResult(val query: PartQuery, val match: MatchResult?, val snapshot: MarketSnapshot)
