package ir.daneshnet.price.adapters

import ir.daneshnet.price.*

/**
 * Adapter boundary for public product pages/search endpoints.
 * Do not bypass robots, authentication, CAPTCHAs or access controls.
 */
class PublicWebSourceAdapter(
    override val sourceId: String,
    override val displayName: String
) : SourceAdapter {
    override val type = SourceType.RETAILER

    override suspend fun search(query: PartQuery): List<PriceObservation> {
        // TODO: inject an approved search/fetch client.
        return emptyList()
    }
}
