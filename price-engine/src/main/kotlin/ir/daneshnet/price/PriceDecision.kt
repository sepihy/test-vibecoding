package ir.daneshnet.price

enum class Decision { SHOW_PRICE, ASK_CLARIFICATION, NO_DATA }

object PriceDecision {
    fun decide(result: LookupResult): Decision = when {
        result.snapshot.dataQuality == DataQuality.INSUFFICIENT -> Decision.NO_DATA
        result.match != null && result.match.needsConfirmation -> Decision.ASK_CLARIFICATION
        else -> Decision.SHOW_PRICE
    }
}
