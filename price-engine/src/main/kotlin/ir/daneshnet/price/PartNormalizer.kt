package ir.daneshnet.price

object PartNormalizer {
    private val digitMap = mapOf('۰' to '0','۱' to '1','۲' to '2','۳' to '3','۴' to '4','۵' to '5','۶' to '6','۷' to '7','۸' to '8','۹' to '9')
    private val synonyms = mapOf(
        "لنت ترمز" to "لنت",
        "صفحه کلاچ" to "دیسک کلاچ",
        "تسمه تایم" to "تسمه تایم",
        "206" to "۲۰۶",
        "peugeot" to "پژو"
    )

    fun normalizeText(raw: String): String {
        var s = raw.trim().lowercase().map { digitMap[it] ?: it }.joinToString("")
        synonyms.forEach { (a,b) -> s = s.replace(a, b) }
        return s.replace('ي','ی').replace('ك','ک')
            .replace(Regex("[^\p{L}\p{N}]+"), " ")
            .replace(Regex("\s+"), " ").trim()
    }

    fun buildKey(query: PartQuery): String =
        listOf(query.make, query.model, query.year?.toString(), query.partName, query.brand, query.partNumber)
            .filterNotNull().filter { it.isNotBlank() }.joinToString("|") { normalizeText(it) }

    fun fromQuery(query: PartQuery): NormalizedPart {
        val key = buildKey(query)
        return NormalizedPart(
            canonicalKey = key,
            title = listOfNotNull(query.make, query.model, query.partName, query.brand, query.partNumber)
                .joinToString(" ").ifBlank { normalizeText(query.raw) },
            aliases = setOf(normalizeText(query.raw)),
            make = query.make, model = query.model, year = query.year,
            partName = query.partName, brand = query.brand, partNumber = query.partNumber
        )
    }
}
