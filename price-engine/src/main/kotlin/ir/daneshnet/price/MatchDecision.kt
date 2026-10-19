package ir.daneshnet.price

enum class MatchDecision { EXACT, HIGH_CONFIDENCE, REVIEW, REJECT }

data class MatchResult(
    val decision: MatchDecision,
    val score: Double,
    val record: PartRecord?,
    val reasons: List<String>
)

object MatchDecisionEngine {
    fun decide(matches: List<PartMatch>): MatchResult {
        val best = matches.maxByOrNull { it.score } ?: return MatchResult(MatchDecision.REJECT,0.0,null,listOf("قطعه‌ای با مشخصات کافی پیدا نشد"))
        val second = matches.filter { it != best }.maxOfOrNull { it.score } ?: 0.0
        val gap = best.score - second
        val decision = when {
            best.score >= .95 && gap >= .08 -> MatchDecision.EXACT
            best.score >= .85 && gap >= .05 -> MatchDecision.HIGH_CONFIDENCE
            best.score >= .60 -> MatchDecision.REVIEW
            else -> MatchDecision.REJECT
        }
        val reasons = best.reasons + if (gap < .05) "دو گزینه بسیار نزدیک‌اند" else emptyList()
        return MatchResult(decision,best.score,best.record,reasons)
    }
}
