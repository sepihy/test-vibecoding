package ir.daneshnet.price

data class RawSourceItem(
    val sourceId: String,
    val externalId: String,
    val text: String,
    val publishedAtEpochMs: Long,
    val url: String?
)

data class ExtractedOffer(
    val rawItem: RawSourceItem,
    val part: PartQuery,
    val priceRial: Long,
    val matchScore: Double,
    val availability: Availability = Availability.UNKNOWN
)

interface SourceReader {
    suspend fun read(source: DetectedSource, cursor: String? = null): List<RawSourceItem>
}

interface OfferExtractor {
    fun extract(item: RawSourceItem): List<ExtractedOffer>
}

class SourceIngestionPipeline(
    private val reader: SourceReader,
    private val extractor: OfferExtractor
) {
    suspend fun ingest(source: DetectedSource, cursor: String? = null): List<ExtractedOffer> =
        reader.read(source, cursor).flatMap(extractor::extract)
}
