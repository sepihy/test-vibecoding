package ir.daneshnet.price

object PriceValidator {
    fun validate(o: PriceObservation, nowMs: Long): Boolean {
        if (o.priceRial <= 0 || o.matchScore < .80) return false
        if (o.observedAtEpochMs > nowMs + 5 * 60_000L) return false
        if (o.sourceId.isBlank() || o.sourceName.isBlank()) return false
        return true
    }

    fun deduplicate(items: List<PriceObservation>): List<PriceObservation> =
        items.groupBy {
            Triple(it.sourceId, it.priceRial, it.partKey ?: "")
        }.values.map { group -> group.maxBy { it.observedAtEpochMs } }
}
