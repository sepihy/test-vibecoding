package ir.daneshnet.price

object PartTextNormalizer {
    private val persianDigits = mapOf('۰' to '0','۱' to '1','۲' to '2','۳' to '3','۴' to '4','۵' to '5','۶' to '6','۷' to '7','۸' to '8','۹' to '9')
    fun normalize(s: String): String = s.lowercase()
        .map { persianDigits[it] ?: it }
        .joinToString("")
        .replace('ي','ی').replace('ى','ی').replace('ك','ک')
        .replace(Regex("[\u200c\s\-_/]+"), "")
        .replace("پژو","پژو")
}
