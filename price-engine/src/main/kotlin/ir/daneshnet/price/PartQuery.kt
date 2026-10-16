package ir.daneshnet.price

data class PartQuery(
    val make: String = "",
    val model: String = "",
    val trim: String? = null,
    val year: Int? = null,
    val partName: String = "",
    val position: String? = null,
    val brand: String? = null,
    val partNumber: String? = null,
    val condition: Condition = Condition.NEW
)
