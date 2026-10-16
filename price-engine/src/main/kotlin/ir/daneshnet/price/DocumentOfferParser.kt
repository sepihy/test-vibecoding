package ir.daneshnet.price

data class DocumentOffer(
    val rawLine: String,
    val partText: String,
    val brand: String?,
    val partNumber: String?,
    val vehicle: String?,
    val priceRial: Long?,
    val priceConfidence: Double,
    val parseConfidence: Double
)

object DocumentOfferParser {
    private val price = Regex("""(?<!\d)(\d[\d۰-۹٠-٩,\. ]{2,})(?:\s*)(تومان|ت|ریال)?(?!\d)""")
    private val partNumber = Regex("""\b[A-Za-z]{1,5}[-/ ]?\d{3,}[A-Za-z0-9-]*\b|\b\d{4,}[A-Za-z0-9-]*\b""")
    private val vehicles = listOf("۲۰۶","207","۲۰۷","۴۰۵","پارس","سمند","دنا","دنا پلاس","تیبا","ساینا","شاهین","پراید","کوییک","ال نود","ساندرو","پژو","سایپا")
    private val brands = listOf("ایساکو","عظام","والئو","ساکورا","کروز","امکو","تکستار","امکو","جنیون")

    fun parse(text: String): List<DocumentOffer> =
        text.lines().mapNotNull { line -> parseLine(line) }

    private fun parseLine(raw: String): DocumentOffer? {
        val line = PartNormalizer.normalizeText(raw)
        if (line.length < 4) return null
        val pm = price.find(line) ?: return null
        val rawNumber = pm.groupValues[1].filter { it.isDigit() || it == ',' || it == '.' }.replace(",", "").replace(".", "")
        val number = rawNumber.toLongOrNull() ?: return null
        if (number <= 0) return null
        val unit = pm.groupValues.getOrNull(2).orEmpty()
        val rial = when {
            unit == "ریال" -> number
            number < 100_000_000 -> number * 10
            else -> number
        }
        val pn = partNumber.find(line)?.value
        val vehicle = vehicles.firstOrNull { line.contains(PartNormalizer.normalizeText(it)) }
        val brand = brands.firstOrNull { line.contains(PartNormalizer.normalizeText(it)) }
        val beforePrice = line.substring(0, pm.range.first).trim()
        if (beforePrice.length < 3) return null
        val confidence = (
            .45 +
            if (pn != null) .25 else 0.0 +
            if (vehicle != null) .15 else 0.0 +
            if (brand != null) .10 else 0.0 +
            if (unit.isNotBlank()) .05 else 0.0
        ).coerceIn(0.0, 1.0)
        return DocumentOffer(raw, beforePrice, brand, pn, vehicle, rial, confidence, confidence)
    }
}
