package ir.daneshnet.price

data class ParsedQuery(val query: PartQuery, val confidence: Double, val unresolved: List<String>)

object QueryParser {
    private val makes = listOf("پژو","ایران خودرو","سایپا","کیا","هیوندای","تویوتا","نیسان","رنو")
    private val models = listOf("۲۰۶","207","۲۰۷","۴۰۵","پارس","سمند","دنا","دنا پلاس","تیبا","ساینا","شاهین","پراید","کوییک","ال نود","ساندرو")

    fun parse(raw: String): ParsedQuery {
        val n = PartNormalizer.normalizeText(raw)
        val make = makes.firstOrNull { n.contains(PartNormalizer.normalizeText(it)) }
        val model = models.firstOrNull { n.contains(PartNormalizer.normalizeText(it)) }
        val partNumber = Regex("""\b[A-Za-z0-9-]{4,}\b""").find(n)?.value
        val unresolved = mutableListOf<String>()
        if (make == null) unresolved += "برند خودرو"
        if (model == null) unresolved += "مدل خودرو"
        val score = (if (make != null) .45 else 0.0) + (if (model != null) .35 else 0.0) + (if (partNumber != null) .20 else 0.0)
        return ParsedQuery(PartQuery(raw = raw, make = make, model = model, partNumber = partNumber), score, unresolved)
    }
}
