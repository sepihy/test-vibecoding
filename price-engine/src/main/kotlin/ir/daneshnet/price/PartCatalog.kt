package ir.daneshnet.price

enum class Condition { NEW, USED, UNKNOWN }

data class PartRecord(
    val canonicalKey: String,
    val make: String,
    val model: String,
    val trim: String?,
    val yearFrom: Int?,
    val yearTo: Int?,
    val partName: String,
    val position: String?,
    val brand: String?,
    val partNumber: String?,
    val condition: Condition = Condition.NEW,
    val aliases: Set<String> = emptySet()
)

data class PartMatch(
    val record: PartRecord,
    val score: Double,
    val reasons: List<String>
)

object PartCatalog {
    private val records = mutableListOf<PartRecord>()

    @Synchronized fun upsert(record: PartRecord) {
        records.removeAll { it.canonicalKey == record.canonicalKey }
        records += record
    }

    @Synchronized fun all(): List<PartRecord> = records.toList()

    fun find(query: PartQuery): List<PartMatch> =
        records.mapNotNull { score(it, query) }.sortedByDescending { it.score }

    private fun score(r: PartRecord, q: PartQuery): PartMatch? {
        val reasons = mutableListOf<String>()
        var score = 0.0
        val pn = norm(q.partNumber)
        if (pn.isNotBlank()) {
            if (norm(r.partNumber) == pn) { score += .65; reasons += "شماره فنی دقیق" }
            else if (norm(r.partNumber).contains(pn) || pn.contains(norm(r.partNumber))) score += .25
        }
        if (norm(r.make) == norm(q.make)) { score += .08; reasons += "برند خودرو" }
        if (norm(r.model) == norm(q.model)) { score += .10; reasons += "مدل خودرو" }
        if (!q.trim.isNullOrBlank() && norm(r.trim) == norm(q.trim)) { score += .06; reasons += "تیپ" }
        if (norm(r.partName) == norm(q.partName) || r.aliases.any { norm(it) == norm(q.partName) }) { score += .08; reasons += "نام قطعه" }
        if (!q.position.isNullOrBlank() && norm(r.position) == norm(q.position)) { score += .03; reasons += "موقعیت قطعه" }
        return if (score >= .35) PartMatch(r, score.coerceAtMost(1.0), reasons) else null
    }

    private fun norm(s: String?) = s.orEmpty().lowercase()
        .replace("ي","ی").replace("ى","ی").replace("ك","ک")
        .replace(Regex("[\s\-_/]+"), "")
}
