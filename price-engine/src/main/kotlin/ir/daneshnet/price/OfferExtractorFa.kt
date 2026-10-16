package ir.daneshnet.price

object OfferExtractorFa : OfferExtractor {
    private val priceRegex = Regex("""(?i)(?:قیمت|فقط|تومان|ت|ریال)?\s*([0-9۰-۹][0-9۰-۹,\. ]{2,})\s*(?:تومان|ت)?""")
    private val partHints = listOf("لنت","دیسک","صفحه کلاچ","تسمه","فیلتر","شمع","چراغ","کمک فنر","بلبرینگ","واتر پمپ","پمپ","سنسور")

    override fun extract(item: RawSourceItem): List<ExtractedOffer> {
        val text = PartNormalizer.normalizeText(item.text)
        val price = priceRegex.find(text)?.groupValues?.getOrNull(1)
            ?.filter { it.isDigit() || it == ',' || it == '.' }
            ?.replace(",", "")?.replace(".", "")?.toLongOrNull() ?: return emptyList()
        val hint = partHints.firstOrNull { text.contains(PartNormalizer.normalizeText(it)) } ?: return emptyList()
        return listOf(
            ExtractedOffer(
                item,
                PartQuery(raw=item.text, partName=hint),
                if (price < 100_000_000) price * 10 else price,
                .82
            )
        )
    }
}
