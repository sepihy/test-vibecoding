package ir.daneshnet.price

object SourcePolicy{
 fun allowed(source:SourceConfig):Boolean=source.enabled&&source.trustScore>0.0
 fun priority(source:SourceConfig):Int=when(source.type){SourceType.OFFICIAL->100;SourceType.DISTRIBUTOR->85;SourceType.RETAILER->75;SourceType.USER_SUPPLIER->60;SourceType.PUBLIC_MESSAGE->40}
}