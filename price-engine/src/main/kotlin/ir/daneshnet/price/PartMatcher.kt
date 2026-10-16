package ir.daneshnet.price

data class MatchResult(val part: NormalizedPart, val score: Double, val reasons: List<String>, val needsConfirmation: Boolean)

object PartMatcher {
    fun match(query: PartQuery, catalog: List<NormalizedPart>): MatchResult? {
        if (catalog.isEmpty()) return null
        val q = PartNormalizer.normalizeText(query.raw)
        return catalog.map { p ->
            var score = tokenScore(q, p)
            val reasons = mutableListOf<String>()
            if (!query.partNumber.isNullOrBlank() && query.partNumber == p.partNumber) { score = 1.0; reasons += "شماره فنی دقیق" }
            if (!query.brand.isNullOrBlank() && query.brand.equals(p.brand, true)) { score += .12; reasons += "برند منطبق" }
            if (!query.model.isNullOrBlank() && PartNormalizer.normalizeText(query.model) == PartNormalizer.normalizeText(p.model ?: "")) { score += .10; reasons += "مدل خودرو منطبق" }
            score = score.coerceIn(0.0, 1.0)
            MatchResult(p, score, reasons, score < .90)
        }.maxByOrNull { it.score }
    }

    private fun tokenScore(q: String, p: NormalizedPart): Double {
        val qt = q.split(" ").filter { it.length > 1 }.toSet()
        val pt = (p.aliases + listOfNotNull(p.title,p.make,p.model,p.partName,p.brand,p.partNumber)).flatMap { PartNormalizer.normalizeText(it).split(" ") }.toSet()
        if (qt.isEmpty()) return 0.0
        return qt.count { t -> pt.any { x -> x == t || x.contains(t) || t.contains(x) } }.toDouble() / qt.size
    }
}
