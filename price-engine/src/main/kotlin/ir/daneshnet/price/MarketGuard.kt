package ir.daneshnet.price

object MarketGuard {
    fun allow(snapshot: MarketSnapshot, policy: AccuracyPolicy = AccuracyPolicy()): Boolean =
        snapshot.medianRial != null &&
        snapshot.sourceCount >= policy.minIndependentSources &&
        snapshot.confidence >= policy.minConfidenceToShow &&
        snapshot.dataQuality >= DataQuality.MODERATE
}
