package ir.daneshnet.price

data class PartQuery(
    val raw: String,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val partName: String? = null,
    val brand: String? = null,
    val partNumber: String? = null
)

data class NormalizedPart(
    val canonicalKey: String,
    val title: String,
    val aliases: Set<String>,
    val make: String?,
    val model: String?,
    val year: Int? = null,
    val partName: String?,
    val brand: String?,
    val partNumber: String?
)

enum class SourceType { OFFICIAL, RETAILER, DISTRIBUTOR, USER_SUPPLIER, PUBLIC_MESSAGE }

data class PriceObservation(
    val sourceId: String,
    val sourceName: String,
    val sourceType: SourceType,
    val priceRial: Long,
    val observedAtEpochMs: Long,
    val matchScore: Double,
    val sellerScore: Double = 1.0,
    val url: String? = null,
    val partKey: String? = null,
    val availability: Availability = Availability.UNKNOWN
)

enum class Availability { IN_STOCK, OUT_OF_STOCK, UNKNOWN }

data class MarketSnapshot(
    val part: NormalizedPart,
    val validPricesRial: List<Long>,
    val lowRial: Long?,
    val medianRial: Long?,
    val highRial: Long?,
    val suggestedRetailRial: Long?,
    val sourceCount: Int,
    val confidence: Int,
    val dataQuality: DataQuality,
    val observedAtEpochMs: Long
)

enum class DataQuality { STRONG, MODERATE, WEAK, INSUFFICIENT }
