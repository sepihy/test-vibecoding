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
    fun build(result: AccuratePriceResult): PriceAudit {
        val accepted = result.sources.map { it.priceRial }.toSet()
        val all = result.sources.map { it.priceRial }.toSet()
        return PriceAudit(
            result.snapshot.part.canonicalKey,
            if (result.accuracy.canShowPrice) result.snapshot.medianRial else null,
            result.accuracy.confidence,
            result.sources.map { it.sourceId }.distinct(),
            all.filter { it !in accepted }.toList(),
            result.accuracy.reasons
        )
    }
}
