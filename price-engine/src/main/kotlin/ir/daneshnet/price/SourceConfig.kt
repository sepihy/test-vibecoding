package ir.daneshnet.price

data class SourceConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val enabled: Boolean = true,
    val type: SourceType = SourceType.RETAILER,
    val trust: Double = 0.8
)

class SourceConfigStore(private val configs: MutableList<SourceConfig> = mutableListOf()) {
    fun enabled(): List<SourceConfig> = configs.filter { it.enabled }
    fun add(config: SourceConfig) { configs.removeAll { it.id == config.id }; configs.add(config) }
}
