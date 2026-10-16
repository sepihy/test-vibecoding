package ir.daneshnet.parts.sources

import java.net.HttpURLConnection
import java.net.URL

enum class PublicSourceState { CONTENT_CANDIDATES_FOUND, PAGE_ONLY, BLOCKED_OR_UNAVAILABLE, INVALID_URL }
data class PublicSourceProbe(
 val sourceName:String,val requestedUrl:String,val finalUrl:String?,val httpCode:Int?,
 val title:String?,val description:String?,val visibleText:String,
 val candidatePriceLines:List<String>,val note:String
)

/** Fetches only ordinary public web pages. Does not log in or call private APIs. */
class PublicPageSourceAdapter(
 private val sourceName:String,
 private val sourceUrl:String
) {
 fun probe():PublicSourceProbe {
  if(!sourceUrl.startsWith("https://") && !sourceUrl.startsWith("http://"))
   return PublicSourceProbe(sourceName,sourceUrl,null,null,null,null,"",emptyList(),"لینک معتبر نیست.")
  var c:HttpURLConnection?=null
  return try {
   c=(URL(sourceUrl).openConnection() as HttpURLConnection).apply{
    requestMethod="GET";connectTimeout=12000;readTimeout=12000;instanceFollowRedirects=true
    setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/126.0 Mobile Safari/537.36")
    setRequestProperty("Accept","text/html,application/xhtml+xml")
   }
   val code=c.responseCode
   val stream=if(code in 200..299)c.inputStream else c.errorStream
   val html=stream?.bufferedReader(Charsets.UTF_8)?.use{it.readText().take(2_000_000)}.orEmpty()
   val title=meta(html,"og:title")?:tag(html,"title")
   val desc=meta(html,"og:description")?:meta(html,"description")
   val plain=html.replace(Regex("(?is)<script[^>]*>.*?</script>")," ")
    .replace(Regex("(?is)<style[^>]*>.*?</style>")," ")
    .replace(Regex("(?is)<[^>]+>")," ").replace("&nbsp;"," ").replace("&amp;","&")
    .replace("&quot;","\"").replace("&#x27;","'").replace("&lt;","<").replace("&gt;",">")
    .replace(Regex("\\s+")," ").trim()
   val price=Regex("(?i).{0,80}(?:قیمت|تومان|ریال|لنت|دیسک|کلاچ|تسمه|فیلتر|قطعه).{0,100}\\d[\\d,۰-۹. ]{3,}.{0,60}")
   val lines=price.findAll(plain).map{it.value.trim()}.distinct().take(50).toList()
   val state=when{code !in 200..299->PublicSourceState.BLOCKED_OR_UNAVAILABLE;lines.isNotEmpty()->PublicSourceState.CONTENT_CANDIDATES_FOUND;else->PublicSourceState.PAGE_ONLY}
   val note=when(state){
    PublicSourceState.CONTENT_CANDIDATES_FOUND->"صفحه عمومی دریافت شد و متن‌های احتمالی قیمت پیدا شد؛ هنوز پست کامل و قیمت معتبر بودنشان تأیید نشده."
    PublicSourceState.PAGE_ONLY->"صفحه عمومی دریافت شد، اما متن پست‌های قابل استخراج در HTML پیدا نشد."
    PublicSourceState.BLOCKED_OR_UNAVAILABLE->"دریافت صفحه ناموفق بود (HTTP $code). محدودیت دور زده نمی‌شود."
    PublicSourceState.INVALID_URL->"لینک نامعتبر است."
   }
   PublicSourceProbe(sourceName,sourceUrl,c.url?.toString(),code,title,desc,plain.take(12000),lines,note)
  }catch(e:Exception){
   PublicSourceProbe(sourceName,sourceUrl,null,null,null,null,"",emptyList(),"اتصال ناموفق: ${e.javaClass.simpleName}. ممکن است سرویس یا شبکه این درخواست را نپذیرد.")
  }finally{c?.disconnect()}
 }
 private fun meta(html:String,key:String):String?{
  val t=Regex("(?is)<meta[^>]+(?:property|name)=[\"']"+Regex.escape(key)+"[\"'][^>]*>").find(html)?.value?:return null
  return Regex("(?is)content=[\"'](.*?)[\"']").find(t)?.groupValues?.getOrNull(1)?.let(::decode)?.takeIf{it.isNotBlank()}
 }
 private fun tag(html:String,name:String):String?=Regex("(?is)<$name[^>]*>(.*?)</$name>").find(html)?.groupValues?.getOrNull(1)?.let(::decode)?.trim()?.takeIf{it.isNotBlank()}
 private fun decode(s:String)=s.replace("&amp;","&").replace("&quot;","\"").replace("&#x27;","'").replace("&lt;","<").replace("&gt;",">")
}
