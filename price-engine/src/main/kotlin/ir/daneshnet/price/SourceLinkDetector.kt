package ir.daneshnet.price

enum class SourcePlatform { TELEGRAM, RUBIKA, WHATSAPP, WEB, UNKNOWN }

data class DetectedSource(val platform: SourcePlatform, val normalizedUrl: String, val public: Boolean)

object SourceLinkDetector {
    fun detect(url: String): DetectedSource {
        val u = url.trim().lowercase()
        val platform = when {
            "t.me/" in u || "telegram.me/" in u -> SourcePlatform.TELEGRAM
            "rubika.ir/" in u || "rubika.ir/" in u -> SourcePlatform.RUBIKA
            "whatsapp.com/channel/" in u -> SourcePlatform.WHATSAPP
            u.startsWith("http://") || u.startsWith("https://") -> SourcePlatform.WEB
            else -> SourcePlatform.UNKNOWN
        }
        return DetectedSource(platform, url.trim(), platform != SourcePlatform.UNKNOWN)
    }
}
