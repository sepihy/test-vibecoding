package ir.daneshnet.price

/**
 * قرارداد کلاینت زنده منابع.
 * پیاده‌سازی واقعی باید به API/فید/صفحه عمومی مجاز همان منبع متصل شود.
 */
interface LiveSourceClient {
    suspend fun fetch(config: SourceConfig, query: PartQuery): List<PriceObservation>
}

class ConfiguredSourceAdapter(
    private val config: SourceConfig,
    private val client: LiveSourceClient
) : SourceAdapter {
    override val sourceId = config.id
    override val displayName = config.name
    override val type = config.type

    override suspend fun search(query: PartQuery): List<PriceObservation> =
        if (config.enabled) client.fetch(config, query) else emptyList()
}
