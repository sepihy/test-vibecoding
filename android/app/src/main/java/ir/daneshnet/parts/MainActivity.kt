package ir.daneshnet.parts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.daneshnet.parts.sources.PublicPageSourceAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AppHome() } }
    }
}

private data class SourceTarget(val name: String, val url: String)
private val targets = listOf(
    SourceTarget("WhatsApp", "https://whatsapp.com/channel/0029Vb3Xp1CInlqTzXlogg0h"),
    SourceTarget("Telegram / MellatSt", "https://t.me/MellatSt"),
    SourceTarget("Rubika / saipayadakhojatiii", "https://rubika.ir/saipayadakhojatiii")
)

@Composable
fun AppHome() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var report by remember { mutableStateOf("هنوز منبعی آزمایش نشده است.") }
    var selectedFile by remember { mutableStateOf("") }
    var extractedText by remember { mutableStateOf("") }
    var fileWarnings by remember { mutableStateOf("") }

    fun displayName(uri: Uri): String {
        var name: String? = null
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) name = c.getString(0)
        }
        return name ?: uri.lastPathSegment ?: "فایل انتخاب‌شده"
    }

    fun processMedia(uri: Uri) {
        val name = displayName(uri)
        selectedFile = name
        extractedText = ""
        fileWarnings = ""
        busy = true
        report = "در حال خواندن فایل..."
        scope.launch {
            try {
                val output = withContext(Dispatchers.IO) {
                    val lower = name.lowercase()
                    if (lower.endsWith(".pdf") || context.contentResolver.getType(uri) == "application/pdf") {
                        val pages = DocumentReader(context.contentResolver).pages(uri, 5)
                        if (pages.isEmpty()) "هیچ صفحه‌ای از PDF خوانده نشد."
                        else {
                            val ocr = OcrEngine(context)
                            try {
                                pages.mapIndexed { index, bitmap ->
                                    val text = ocr.read(bitmap)
                                    "صفحه ${index + 1}:\n" + (text.ifBlank { "متنی با OCR لاتین پیدا نشد." })
                                }.joinToString("\n\n")
                            } finally { ocr.close(); pages.forEach { it.recycle() } }
                        }
                    } else {
                        val bitmap = LocalFileProcessor(context.contentResolver).readImage(uri)
                        if (bitmap == null) "تصویر باز نشد یا قالب آن پشتیبانی نمی‌شود."
                        else {
                            val ocr = OcrEngine(context)
                            try { ocr.read(bitmap).ifBlank { "متنی با OCR لاتین پیدا نشد." } }
                            finally { ocr.close(); bitmap.recycle() }
                        }
                    }
                }
                extractedText = output
                fileWarnings = "توجه: OCR خط عربی برای متن فارسی فعال است؛ با این حال ارقام، جدول‌ها و قیمت‌ها را با فایل اصلی تطبیق بده. متن استخراج‌شده قیمت تأییدشده نیست."
                report = "پردازش اولیه فایل انجام شد. متن را بازبینی و در صورت نیاز اصلاح کن؛ این متن هنوز قیمت تأییدشده نیست."
            } catch (e: Exception) {
                report = "خواندن فایل ناموفق بود: ${e.message ?: e.javaClass.simpleName}"
            } finally { busy = false }
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) processMedia(uri)
    }
    val tablePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val name = displayName(uri)
            selectedFile = name
            busy = true
            report = "در حال خواندن جدول..."
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) { LocalFileProcessor(context.contentResolver).readTabular(uri, name) }
                    extractedText = result.extractedText
                    fileWarnings = buildString {
                        append("ردیف‌های شناسایی‌شده: ${result.rowCount}. ")
                        if (result.warnings.isNotEmpty()) append(result.warnings.joinToString("\n"))
                    }
                    report = if (result.extractedText.isBlank()) "فایلی انتخاب شد اما داده‌ای استخراج نشد." else "فایل خوانده شد. ستون‌ها و قیمت‌ها را بازبینی کن؛ هنوز قیمت‌ها وارد بازار قطعی نشده‌اند."
                } catch (e: Exception) {
                    report = "خواندن جدول ناموفق بود: ${e.message ?: e.javaClass.simpleName}"
                } finally { busy = false }
            }
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("قیمت‌یار قطعات", style = MaterialTheme.typography.headlineMedium)
        Text("نام قطعه یا شماره فنی را وارد کن، یا فایل تأمین‌کننده را بررسی کن.")
        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("نام قطعه یا شماره فنی") }, singleLine = true
        )
        Button(onClick = {
            report = if (query.isBlank()) "ابتدا نام قطعه یا شماره فنی را وارد کن."
            else "جستجوی «$query» ثبت شد؛ دریافت قیمت زنده هنوز به فید واقعی و قابل‌دسترسی منبع نیاز دارد. هیچ قیمت ساختگی نمایش داده نمی‌شود."
        }, modifier = Modifier.fillMaxWidth()) { Text("جستجوی قیمت روز") }

        OutlinedButton(onClick = { mediaPicker.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
            Text("افزودن PDF یا عکس")
        }
        OutlinedButton(onClick = {
            tablePicker.launch("*/*")
        }, modifier = Modifier.fillMaxWidth()) { Text("وارد کردن Excel / CSV") }

        if (selectedFile.isNotBlank()) {
            Text("فایل: $selectedFile", style = MaterialTheme.typography.titleSmall)
            if (fileWarnings.isNotBlank()) Text(fileWarnings, style = MaterialTheme.typography.bodySmall)
        }
        if (extractedText.isNotBlank()) {
            OutlinedTextField(
                value = extractedText, onValueChange = { extractedText = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 360.dp),
                label = { Text("متن استخراج‌شده — قابل ویرایش") }, minLines = 5
            )
            Text("قبل از استفاده، نام قطعه، شماره فنی، واحد پول و قیمت را با فایل اصلی تطبیق بده.", style = MaterialTheme.typography.bodySmall)
        }

        HorizontalDivider()
        Text("اتصال مستقیم منابع", style = MaterialTheme.typography.titleLarge)
        Text("آزمایش اتصال صفحه عمومی می‌کند؛ صفحه عمومی لزوماً پست‌های قیمت را در اختیار نمی‌گذارد.")
        targets.forEach { target ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        report = "در حال بررسی ${target.name} ..."
                        scope.launch {
                            try {
                                val r = withContext(Dispatchers.IO) { PublicPageSourceAdapter(target.name, target.url).probe() }
                                report = buildString {
                                    appendLine("منبع: ${r.sourceName}")
                                    appendLine("HTTP: ${r.httpCode ?: "نامشخص"}")
                                    appendLine("آدرس نهایی: ${r.finalUrl ?: "نامشخص"}")
                                    r.title?.let { appendLine("عنوان: $it") }
                                    r.description?.let { appendLine("توضیح: $it") }
                                    appendLine(r.note)
                                    if (r.candidatePriceLines.isNotEmpty()) {
                                        appendLine("متن‌های کاندیدا (تأییدنشده):")
                                        r.candidatePriceLines.take(8).forEach { appendLine("• $it") }
                                    }
                                }
                            } catch (e: Exception) {
                                report = "اتصال ناموفق: ${e.message ?: e.javaClass.simpleName}"
                            } finally { busy = false }
                        }
                    }, modifier = Modifier.weight(1f)
                ) { Text("آزمایش ${target.name}") }
                OutlinedButton(onClick = {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target.url))) }
                    catch (e: Exception) { report = "بازکردن مرورگر ممکن نشد: ${e.message}" }
                }) { Text("مرورگر") }
            }
        }
        if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Card(modifier = Modifier.fillMaxWidth()) { Text(report, Modifier.padding(14.dp), fontSize = 14.sp) }
        Text("OCR خط عربی برای خواندن فارسی فعال است؛ دقت آن به کیفیت اسکن و فونت PDF بستگی دارد و قیمت‌ها باید بازبینی شوند. اتصال مستقیم هم به دسترسی واقعی شبکه و محتوای عمومی هر سرویس وابسته است.", style = MaterialTheme.typography.bodySmall)
    }
}
