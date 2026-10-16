package ir.daneshnet.parts.sources

import java.net.HttpURLConnection
import java.net.URL

enum class ChannelAccessState { PUBLIC_CONTENT_FOUND, PUBLIC_PAGE_ONLY, BLOCKED_OR_UNAVAILABLE, INVALID_RESPONSE }

data class PublicChannelProbe(
    val state: ChannelAccessState,
    val requestedUrl: String,
    val finalUrl: String?,
    val httpCode: Int?,
    val title: String?,
    val description: String?,
    val visibleText: String,
    val candidatePriceLines: List<String>,
    val note: String
)

class WhatsAppChannelAdapter(
    private val channelUrl: String = "https://whatsapp.com/channel/0029Vb3Xp1CInlqTzXlogg0h"
) {
    fun probe(): PublicChannelProbe {
        if (!channelUrl.startsWith("https://whatsapp.com/channel/")) {
            return PublicChannelProbe(ChannelAccessState.INVALID_RESPONSE, channelUrl, null, null, null, null, "", emptyList(), "فقط لینک عمومی WhatsApp Channel پشتیبانی می‌شود.")
        }
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(channelUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/126.0 Mobile Safari/537.36")
                setRequestProperty("Accept", "text/html,application/xhtml+xml")
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val html = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText().take(2_000_000) }.orEmpty()
            val title = meta(html, "og:title") ?: tag(html, "title")
            val description = meta(html, "og:description") ?: meta(html, "description")
            val text = html
                .replace(Regex("(?is)<script[^>]*>.*?</script>"), " ")
                .replace(Regex("(?is)<style[^>]*>.*?</style>"), " ")
                .replace(Regex("(?is)<[^>]+>"), " ")
                .replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"")
                .replace("&#x27;", "'").replace("&lt;", "<").replace("&gt;", ">")
                .replace(Regex("\\s+"), " ").trim()
            val pricePattern = Regex("(?i).{0,80}(?:قیمت|تومان|ریال|لنت|دیسک|کلاچ|تسمه|فیلتر).{0,100}\\d[\\d,۰-۹. ]{3,}.{0,60}")
            val lines = pricePattern.findAll(text).map { it.value.trim() }.distinct().take(50).toList()
            val pageLooksLikeChannel = (title.orEmpty() + " " + description.orEmpty() + " " + text.take(1500)).contains("WhatsApp", true)
            val state = when {
                code !in 200..299 -> ChannelAccessState.BLOCKED_OR_UNAVAILABLE
                lines.isNotEmpty() -> ChannelAccessState.PUBLIC_CONTENT_FOUND
                pageLooksLikeChannel -> ChannelAccessState.PUBLIC_PAGE_ONLY
                else -> ChannelAccessState.PUBLIC_PAGE_ONLY
            }
            val note = when (state) {
                ChannelAccessState.PUBLIC_CONTENT_FOUND -> "صفحه عمومی باز شد و چند خط کاندیدا پیدا شد؛ هنوز قیمت‌ها اعتبارسنجی نشده‌اند."
                ChannelAccessState.PUBLIC_PAGE_ONLY -> "صفحه عمومی باز شد، اما پست‌های قابل استخراج در HTML عمومی پیدا نشدند. این به معنی دسترسی به محتوای کانال نیست."
                ChannelAccessState.BLOCKED_OR_UNAVAILABLE -> "صفحه از این محیط قابل دریافت نیست (HTTP $code). محدودیت را دور نمی‌زنیم."
                ChannelAccessState.INVALID_RESPONSE -> "پاسخ معتبر دریافت نشد."
            }
            PublicChannelProbe(state, channelUrl, connection.url?.toString(), code, title, description, text.take(12_000), lines, note)
        } catch (e: Exception) {
            PublicChannelProbe(ChannelAccessState.BLOCKED_OR_UNAVAILABLE, channelUrl, null, null, null, null, "", emptyList(), "اتصال عمومی ناموفق بود: ${e.javaClass.simpleName}. ممکن است دسترسی شبکه یا خود سرویس محدود باشد.")
        } finally {
            connection?.disconnect()
        }
    }

    private fun meta(html: String, property: String): String? {
        val tag = Regex("(?is)<meta[^>]+(?:property|name)=[\"']" + Regex.escape(property) + "[\"'][^>]*>").find(html)?.value ?: return null
        return Regex("(?is)content=[\"'](.*?)[\"']").find(tag)?.groupValues?.getOrNull(1)?.let(::decode)?.takeIf { it.isNotBlank() }
    }
    private fun tag(html: String, name: String): String? =
        Regex("(?is)<$name[^>]*>(.*?)</$name>").find(html)?.groupValues?.getOrNull(1)?.let(::decode)?.trim()?.takeIf { it.isNotBlank() }
    private fun decode(s: String) = s.replace("&amp;", "&").replace("&quot;", "\"").replace("&#x27;", "'").replace("&lt;", "<").replace("&gt;", ">")
}
