package ir.daneshnet.price

data class AccuratePriceResult(
    val snapshot: MarketSnapshot,
    val accuracy: AccuracyReport,
    val sources: List<PriceObservation>
)

object AccuratePriceResultBuilder {
    fun build(snapshot: MarketSnapshot, observations: List<PriceObservation>, policy: AccuracyPolicy = AccuracyPolicy()) =
        AccuratePriceResult(snapshot, AccuracyPolicyEngine.evaluate(snapshot, observations, policy), observations)
}
