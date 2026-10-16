package ir.daneshnet.price

data class AccuracyPolicy(
    val minMatchScore: Double = .90,
    val minIndependentSources: Int = 3,
    val minConfidenceToShow: Int = 75,
    val strongConfidence: Int = 90,
    val maxAgeDays: Int = 14
)

data class AccuracyReport(
    val confidence: Int,
    val quality: DataQuality,
    val canShowPrice: Boolean,
    val reasons: List<String>
)

object AccuracyPolicyEngine {
    fun evaluate(snapshot: MarketSnapshot, observations: List<PriceObservation>, policy: AccuracyPolicy = AccuracyPolicy()): AccuracyReport {
        val reasons = mutableListOf<String>()
        val independent = observations.map { it.sourceId }.distinct().size
        if (independent < policy.minIndependentSources) reasons += "منابع مستقل کافی نیستند"
        if (snapshot.confidence < policy.minConfidenceToShow) reasons += "اطمینان کمتر از حد مجاز است"
        if (snapshot.medianRial == null) reasons += "قیمت معتبر پیدا نشد"
        if (observations.any { it.matchScore < policy.minMatchScore }) reasons += "برخی تطبیق‌ها نیازمند بررسی هستند"
        val canShow = snapshot.medianRial != null &&
            snapshot.confidence >= policy.minConfidenceToShow &&
            independent >= policy.minIndependentSources
        return AccuracyReport(snapshot.confidence, snapshot.dataQuality, canShow, reasons)
    }
}
