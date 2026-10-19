package ir.daneshnet.price

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

object PriceEngine {
    fun snapshot(part: NormalizedPart, observations: List<PriceObservation>, nowMs: Long, marginPct: Double = 10.0): MarketSnapshot {
        val candidates = observations.filter { isCandidate(it, nowMs) }
        val clean = removeOutliers(candidates)
        val weighted = clean.map { it.priceRial to weight(it, nowMs) }
        val median = weightedMedian(weighted)
        val prices = clean.map { it.priceRial }.sorted()
        val low = prices.firstOrNull()
        val high = prices.lastOrNull()
        val suggested = median?.let { round(it * (1.0 + marginPct.coerceIn(0.0, 100.0) / 100.0)).toLong() }
        val confidence = confidence(clean)
        return MarketSnapshot(part, prices, low, median, high, suggested,
            clean.map { it.sourceId }.distinct().size, confidence, quality(confidence, clean.size),
            clean.maxOfOrNull { it.observedAtEpochMs } ?: 0L)
    }

    private fun isCandidate(o: PriceObservation, now: Long) =
        o.priceRial > 0 && o.matchScore >= .80 && o.availability != Availability.OUT_OF_STOCK &&
        o.observedAtEpochMs <= now && freshness(o.observedAtEpochMs, now) > .05

    private fun freshness(observed: Long, now: Long): Double {
        val ageDays = max(0.0, (now-observed).toDouble()/86_400_000.0)
        return exp(-ageDays/7.0)
    }

    private fun weight(o: PriceObservation, now: Long): Double {
        val trust = when(o.sourceType) {
            SourceType.OFFICIAL -> 1.00
            SourceType.DISTRIBUTOR -> .92
            SourceType.RETAILER -> .84
            SourceType.USER_SUPPLIER -> .80
            SourceType.PUBLIC_MESSAGE -> .55
        }
        return trust*freshness(o.observedAtEpochMs,now)*o.matchScore*o.sellerScore.coerceIn(0.0,1.0)
    }

    private fun removeOutliers(items: List<PriceObservation>): List<PriceObservation> {
        if(items.size<4) return items
        val v=items.map{it.priceRial}.sorted(); val q1=percentile(v,.25); val q3=percentile(v,.75); val iqr=q3-q1
        val lo=q1-1.5*iqr; val hi=q3+1.5*iqr
        return items.filter{it.priceRial.toDouble() in lo..hi}
    }

    private fun percentile(v: List<Long>, p: Double): Double {
        val pos=(v.size-1)*p; val lo=pos.toInt(); val hi=min(lo+1,v.lastIndex)
        return v[lo]+(v[hi]-v[lo])*(pos-lo)
    }

    private fun weightedMedian(v: List<Pair<Long,Double>>): Long? {
        if(v.isEmpty()) return null
        val s=v.sortedBy{it.first}; val total=s.sumOf{it.second}; var c=0.0
        for((price,w) in s){c+=w;if(c>=total/2)return price};return s.last().first
    }

    private fun confidence(v: List<PriceObservation>): Int {
        if(v.isEmpty()) return 0
        val sources=v.map{it.sourceId}.distinct().size
        val avgMatch=v.map{it.matchScore}.average()
        val coverage=min(1.0,v.size/8.0)
        val sourceFactor=min(1.0,sources/6.0)
        val med=v.map{it.priceRial}.sorted()[v.size/2].toDouble()
        val stability=1.0-min(1.0,v.map{abs(it.priceRial-med)/max(1.0,med)}.average())
        return (100*(.30*sourceFactor+.25*coverage+.30*avgMatch+.15*stability)).toInt().coerceIn(0,100)
    }

    private fun quality(c:Int,n:Int)=when{n<2||c<30->DataQuality.INSUFFICIENT;c<55->DataQuality.WEAK;c<75->DataQuality.MODERATE;else->DataQuality.STRONG}
}
