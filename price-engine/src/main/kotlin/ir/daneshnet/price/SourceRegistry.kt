package ir.daneshnet.price

data class SourceConfig(
    val id: String,
    val name: String,
    val type: SourceType,
    val enabled: Boolean = true,
    val trustScore: Double = .5,
    val url: String? = null
)

object SourceRegistry {
    private val sources = mutableListOf<SourceConfig>()

    @Synchronized fun upsert(source: SourceConfig) {
        sources.removeAll { it.id == source.id }
        sources += source
    }

    @Synchronized fun enabled(): List<SourceConfig> = sources.filter { it.enabled }
}
