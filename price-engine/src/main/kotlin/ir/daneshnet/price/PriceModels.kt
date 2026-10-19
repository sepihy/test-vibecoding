package ir.daneshnet.price

enum class SourceType { OFFICIAL, DISTRIBUTOR, RETAILER, USER_SUPPLIER, PUBLIC_MESSAGE }
enum class Availability { IN_STOCK, OUT_OF_STOCK, UNKNOWN }
enum class DataQuality { INSUFFICIENT, WEAK, MODERATE, STRONG }

data class NormalizedPart(
    val canonicalKey: String,
    val displayName: String,
    val aliases: Set<String>,
    val make: String,
    val model: String,
    val trim: String?,
    val partName: String,
    val partNumber: String?,
    val brand: String?,
    val position: String?
)

data class PriceObservation(
    val id: String,
    val sourceId: String,
    val sourceType: SourceType,
    val priceRial: Long,
    val observedAtEpochMs: Long,
    val matchScore: Double,
    val sellerScore: Double,
    val availability: Availability = Availability.UNKNOWN,
    val partKey: String
)

data class MarketSnapshot(
    val part: NormalizedPart,
    val validPricesRial: List<Long>,
    val lowRial: Long?,
    val medianRial: Long?,
    val highRial: Long?,
    val suggestedSaleRial: Long?,
    val sourceCount: Int,
    val confidence: Int,
    val dataQuality: DataQuality,
    val latestObservedAtEpochMs: Long
)
