package ir.daneshnet.price

import kotlin.math.max

data class MatchResult(val part: NormalizedPart, val score: Double, val reasons: List<String>)

object PartMatcher {
    fun match(query: PartQuery, catalog: List<NormalizedPart>): MatchResult? {
        if (catalog.isEmpty()) return null
        val nq = PartNormalizer.normalizeText(query.raw)
        return catalog.map { part ->
            val fields = listOfNotNull(part.title, part.partName, part.brand, part.partNumber, part.make, part.model)
                .map(PartNormalizer::normalizeText)
            val tokenScore = tokenSimilarity(nq, fields.flatMap { it.split(" ") }.toSet())
            val exactPartNumber = query.partNumber != null && part.partNumber != null &&
                PartNormalizer.normalizeText(query.partNumber) == PartNormalizer.normalizeText(part.partNumber)
            val score = if (exactPartNumber) 1.0 else tokenScore
            MatchResult(part, score, buildList {
                if (exactPartNumber) add("شماره فنی دقیق")
                if (tokenScore >= .8) add("شباهت نام و مشخصات")
            })
        }.maxByOrNull { it.score }
    }

    private fun tokenSimilarity(query: String, tokens: Set<String>): Double {
        val q = query.split(" ").filter { it.length > 1 }.toSet()
        if (q.isEmpty()) return 0.0
        return q.count { token -> tokens.any { t -> t == token || t.contains(token) || token.contains(t) } }.toDouble() / q.size
    }
}
