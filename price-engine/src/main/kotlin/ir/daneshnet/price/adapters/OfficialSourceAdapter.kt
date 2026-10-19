package ir.daneshnet.price.adapters

import ir.daneshnet.price.*

/**
 * Adapter boundary for official catalogs such as manufacturer/ISACO data.
 * The network implementation is intentionally injected later so credentials,
 * rate limits and terms of service remain explicit.
 */
class OfficialSourceAdapter(
    override val sourceId: String,
    override val displayName: String
) : SourceAdapter {
    override val type = SourceType.OFFICIAL

    override suspend fun search(query: PartQuery): List<PriceObservation> {
        // TODO: connect to an official/public API or permitted feed.
        return emptyList()
    }
}
