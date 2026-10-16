package ir.daneshnet.price

data class SmartLookupResult(
    val decision: Decision,
    val result: LookupResult,
    val explanation: String
)

object SmartLookup {
    suspend fun run(service: PriceService, raw: String, nowMs: Long, marginPct: Double = 10.0): SmartLookupResult {
        val parsed = QueryParser.parse(raw)
        val result = service.lookup(parsed.query, nowMs, marginPct)
        val decision = PriceDecision.decide(result)
        val explanation = when (decision) {
            Decision.SHOW_PRICE -> "قیمت بر اساس داده‌های معتبر جمع‌آوری‌شده محاسبه شد."
            Decision.ASK_CLARIFICATION -> "مشخصات قطعه برای تطبیق دقیق کافی نیست؛ تأیید کاربر لازم است."
            Decision.NO_DATA -> "برای این قطعه داده معتبر کافی پیدا نشد؛ قیمت حدس زده نمی‌شود."
        }
        return SmartLookupResult(decision, result, explanation)
    }
}
