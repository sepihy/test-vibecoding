package ir.daneshnet.price

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

object PriceEngine {
    fun snapshot(part: NormalizedPart, observations: List<PriceObservation>, nowMs: Long, marginPct: Double = 10.0): MarketSnapshot {
        val candidates = observations.filter {
            it.priceRial > 0 && it.matchScore >= 0.80 && freshness(it.observedAtEpochMs, nowMs) > 0.05
        }
        val clean = removeOutliers(candidates)
        val weighted = clean.map { it.priceRial to weight(it, nowMs) }
        val median = weightedMedian(weighted)
        val prices = clean.map { it.priceRial }.sorted()
        val low = prices.firstOrNull()
        val high = prices.lastOrNull()
        val suggested = median?.let { kotlin.math.round(it * (1.0 + marginPct / 100.0)).toLong() }
        val confidence = confidence(clean, candidates.size)
        return MarketSnapshot(part, prices, low, median, high, suggested, clean.map { it.sourceId }.distinct().size, confidence, clean.maxOfOrNull { it.observedAtEpochMs } ?: 0L)
    }

    private fun freshness(observed: Long, now: Long): Double {
        val ageDays = max(0.0, (now - observed).toDouble() / 86_400_000.0)
        return exp(-ageDays / 7.0)
    }

    private fun weight(o: PriceObservation, now: Long): Double {
        val sourceTrust = when (o.sourceType) {
            SourceType.OFFICIAL -> 1.00
            SourceType.DISTRIBUTOR -> 0.92
            SourceType.RETAILER -> 0.84
            SourceType.USER_SUPPLIER -> 0.80
            SourceType.PUBLIC_MESSAGE -> 0.55
        }
        return sourceTrust * freshness(o.observedAtEpochMs, now) * o.matchScore * o.sellerScore
    }

    private fun removeOutliers(items: List<PriceObservation>): List<PriceObservation> {
        if (items.size < 4) return items
        val values = items.map { it.priceRial }.sorted()
        val q1 = percentile(values, .25)
        val q3 = percentile(values, .75)
        val iqr = q3 - q1
        val lo = q1 - 1.5 * iqr
        val hi = q3 + 1.5 * iqr
        return items.filter { it.priceRial.toDouble() in lo..hi }
    }

    private fun percentile(v: List<Long>, p: Double): Double {
        val pos = (v.size - 1) * p
        val lo = pos.toInt()
        val hi = min(lo + 1, v.lastIndex)
        return v[lo] + (v[hi] - v[lo]) * (pos - lo)
    }

    private fun weightedMedian(values: List<Pair<Long, Double>>): Long? {
        if (values.isEmpty()) return null
        val sorted = values.sortedBy { it.first }
        val total = sorted.sumOf { it.second }
        var cumulative = 0.0
        for ((price, weight) in sorted) {
            cumulative += weight
            if (cumulative >= total / 2.0) return price
        }
        return sorted.last().first
    }

    private fun confidence(clean: List<PriceObservation>, candidateCount: Int): Int {
        if (clean.isEmpty()) return 0
        val sources = clean.map { it.sourceId }.distinct().size
        val avgMatch = clean.map { it.matchScore }.average()
        val sourceFactor = min(1.0, sources / 6.0)
        val coverage = min(1.0, clean.size / 8.0)
        val stability = if (clean.size < 2) .45 else {
            val med = clean.map { it.priceRial }.sorted()[clean.size / 2].toDouble()
            1.0 - min(1.0, clean.map { abs(it.priceRial - med) / max(1.0, med) }.average())
        }
        val raw = 100.0 * (.30 * sourceFactor + .25 * coverage + .30 * avgMatch + .15 * stability)
        return raw.toInt().coerceIn(0, 100)
    }
}
