package ir.daneshnet.price

data class PriceAudit(
    val partKey: String,
    val displayedPriceRial: Long?,
    val confidence: Int,
    val sourceIds: List<String>,
    val excludedPricesRial: List<Long>,
    val reasons: List<String>
)

object PriceAuditBuilder {
    fun build(result: AccuratePriceResult, allObservedPricesRial: List<Long> = result.sources.map { it.priceRial }): PriceAudit {
        val accepted = result.snapshot.validPricesRial.toSet()
        return PriceAudit(
            result.snapshot.part.canonicalKey,
            if (result.accuracy.canShowPrice) result.snapshot.medianRial else null,
            result.accuracy.confidence,
            result.sources.map { it.sourceId }.distinct(),
            allObservedPricesRial.filter { it !in accepted }.distinct(),
            result.accuracy.reasons
        )
    }
}
