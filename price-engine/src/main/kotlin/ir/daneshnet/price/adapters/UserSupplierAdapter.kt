package ir.daneshnet.price.adapters

import ir.daneshnet.price.*

class UserSupplierAdapter(
    override val sourceId: String,
    override val displayName: String,
    private val observations: suspend (PartQuery) -> List<PriceObservation>
) : SourceAdapter {
    override val type = SourceType.USER_SUPPLIER
    override suspend fun search(query: PartQuery): List<PriceObservation> = observations(query)
}
