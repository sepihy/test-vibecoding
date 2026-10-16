package ir.daneshnet.price

object PriceNormalization {
    fun toRial(raw: String): Long? {
        val s = raw.map { when(it){'۰'->'0';'۱'->'1';'۲'->'2';'۳'->'3';'۴'->'4';'۵'->'5';'۶'->'6';'۷'->'7';'۸'->'8';'۹'->'9'; else->it } }
            .joinToString("").replace(",", "").replace(".", "").replace(" ", "")
        val n = s.filter(Char::isDigit).toLongOrNull() ?: return null
        if(n <= 0) return null
        return if (raw.contains("ریال")) n else n * 10
    }
}
