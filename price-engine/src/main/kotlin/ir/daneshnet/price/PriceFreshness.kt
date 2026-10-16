package ir.daneshnet.price

import kotlin.math.exp
import kotlin.math.max

object PriceFreshness {
    fun score(observedAtMs: Long, nowMs: Long, halfLifeDays: Double = 7.0): Double {
        if (observedAtMs > nowMs) return 0.0
        val ageDays = max(0.0, (nowMs - observedAtMs) / 86_400_000.0)
        return exp(-ageDays / halfLifeDays)
    }

    fun isUsable(observedAtMs: Long, nowMs: Long, maxAgeDays: Int = 14): Boolean =
        observedAtMs in 0..nowMs && nowMs - observedAtMs <= maxAgeDays * 86_400_000L
}
