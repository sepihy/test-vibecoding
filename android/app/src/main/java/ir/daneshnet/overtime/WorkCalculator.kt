package ir.daneshnet.overtime

data class WorkResult(
    val requiredEnd: Int?,
    val workedMinutes: Int,
    val overtimeMinutes: Int,
    val shortageMinutes: Int,
    val status: String
)

object WorkCalculator {
    private const val OFFICIAL_START = 450
    private const val FLEX_END = 480
    private const val REQUIRED_MINUTES = 390

    fun calculate(entry: Int, exit: Int, thursday: Boolean = false): WorkResult {
        require(exit >= entry) { "خروج نمی‌تواند قبل از ورود باشد." }
        if (thursday) {
            val worked = exit - entry
            return WorkResult(null, worked, worked, 0, "اضافه‌کاری پنجشنبه")
        }
        val requiredStart = when {
            entry < OFFICIAL_START -> OFFICIAL_START
            entry <= FLEX_END -> entry
            else -> FLEX_END
        }
        val lateShortage = maxOf(0, entry - FLEX_END)
        val requiredEnd = requiredStart + REQUIRED_MINUTES
        val overtime = maxOf(0, exit - requiredEnd)
        val earlyShortage = maxOf(0, requiredEnd - exit)
        return WorkResult(requiredEnd, exit - entry, overtime, lateShortage + earlyShortage,
            when {
                overtime > 0 -> "اضافه‌کاری"
                lateShortage + earlyShortage > 0 -> "کسری"
                else -> "کامل"
            })
    }
}
