package ir.daneshnet.price

data class RefreshPolicy(
    val intervalMinutes: Long = 30,
    val maxItemsPerRun: Int = 500
)

class RefreshScheduler(private val policy: RefreshPolicy = RefreshPolicy()) {
    fun nextRunEpochMs(nowMs: Long): Long = nowMs + policy.intervalMinutes * 60_000L
}
