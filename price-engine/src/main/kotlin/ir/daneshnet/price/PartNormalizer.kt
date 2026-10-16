package ir.daneshnet.price

object PartNormalizer {
    private val digitMap = mapOf(
        '۰' to '0','۱' to '1','۲' to '2','۳' to '3','۴' to '4',
        '۵' to '5','۶' to '6','۷' to '7','۸' to '8','۹' to '9'
    )

    fun normalizeText(raw: String): String {
        return raw.trim().lowercase()
            .map { digitMap[it] ?: it }
            .joinToString("")
            .replace(Regex("[^\p{L}\p{N}]+"), " ")
            .replace(Regex("\s+"), " ")
            .trim()
    }

    fun buildKey(query: PartQuery): String =
        listOf(query.make, query.model, query.partName, query.brand, query.partNumber)
            .filterNotNull().filter { it.isNotBlank() }
            .joinToString("|") { normalizeText(it) }

    fun fromQuery(query: PartQuery): NormalizedPart {
        val key = buildKey(query)
        val title = listOfNotNull(query.make, query.model, query.partName, query.brand, query.partNumber)
            .joinToString(" ")
        return NormalizedPart(
            canonicalKey = key,
            title = title.ifBlank { normalizeText(query.raw) },
            aliases = setOf(normalizeText(query.raw)),
            make = query.make, model = query.model, partName = query.partName,
            brand = query.brand, partNumber = query.partNumber
        )
    }
}
