package ir.daneshnet.parts

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

data class LocalImportResult(
    val fileName: String,
    val kind: String,
    val extractedText: String,
    val rowCount: Int = 0,
    val warnings: List<String> = emptyList()
)

class LocalFileProcessor(private val resolver: ContentResolver) {
    fun readTabular(uri: Uri, displayName: String): LocalImportResult {
        val lower = displayName.lowercase()
        return when {
            lower.endsWith(".csv") || lower.endsWith(".txt") || lower.endsWith(".tsv") -> readCsv(uri, displayName)
            lower.endsWith(".xlsx") -> readXlsx(uri, displayName)
            lower.endsWith(".xls") -> LocalImportResult(displayName, "Excel قدیمی", "", warnings=listOf("فرمت XLS قدیمی مستقیماً پشتیبانی نمی‌شود؛ فایل را از Excel به XLSX یا CSV تبدیل کن."))
            else -> readCsv(uri, displayName)
        }
    }

    private fun readCsv(uri: Uri, name: String): LocalImportResult {
        val text = resolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: return LocalImportResult(name, "CSV", "", warnings=listOf("فایل باز نشد."))
        val rows = parseDelimited(text)
        if (rows.isEmpty()) return LocalImportResult(name, "CSV", "", warnings=listOf("فایل خالی است یا جداکننده تشخیص داده نشد."))
        val header = rows.first()
        val lines = rows.take(501).mapIndexed { i, row ->
            if (i == 0) "ستون‌ها: " + row.joinToString(" | ")
            else row.joinToString(" | ")
        }
        val priceColumn = header.indexOfFirst { it.contains("قیمت") || it.contains("فی") || it.contains("price", true) }
        val partColumn = header.indexOfFirst { it.contains("شرح") || it.contains("قطعه") || it.contains("نام") || it.contains("description", true) }
        val warnings = mutableListOf<String>()
        if (priceColumn < 0) warnings += "ستون قیمت با اطمینان تشخیص داده نشد؛ سرستون‌ها را بررسی کن."
        if (partColumn < 0) warnings += "ستون نام/شرح قطعه با اطمینان تشخیص داده نشد."
        return LocalImportResult(name, "CSV", lines.joinToString("\n"), rows.size - 1, warnings)
    }

    private fun parseDelimited(text: String): List<List<String>> {
        val first = text.lineSequence().firstOrNull().orEmpty()
        val delimiter = listOf(',', ';', '\t', '|').maxByOrNull { d -> first.count { it == d } } ?: ','
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>(); val cell = StringBuilder(); var quoted = false; var i = 0
        while (i < text.length) {
            val ch = text[i]
            when {
                ch == '"' && quoted && i + 1 < text.length && text[i + 1] == '"' -> { cell.append('"'); i++ }
                ch == '"' -> quoted = !quoted
                ch == delimiter && !quoted -> { row.add(cell.toString().trim()); cell.setLength(0) }
                (ch == '\n' || ch == '\r') && !quoted -> {
                    if (ch == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    row.add(cell.toString().trim()); cell.setLength(0)
                    if (row.any { it.isNotBlank() }) rows.add(row)
                    row = mutableListOf()
                }
                else -> cell.append(ch)
            }
            i++
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) { row.add(cell.toString().trim()); if (row.any { it.isNotBlank() }) rows.add(row) }
        return rows
    }

    private fun readXlsx(uri: Uri, name: String): LocalImportResult {
        val entries = mutableMapOf<String, String>()
        val input = resolver.openInputStream(uri) ?: return LocalImportResult(name, "XLSX", "", warnings=listOf("فایل باز نشد."))
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "xl/sharedStrings.xml" || entry.name == "xl/worksheets/sheet1.xml") {
                    entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        val sheet = entries["xl/worksheets/sheet1.xml"]
            ?: return LocalImportResult(name, "XLSX", "", warnings=listOf("برگه اول فایل Excel پیدا نشد یا فایل ساختار پشتیبانی‌شده ندارد."))
        val stringsXml = entries["xl/sharedStrings.xml"].orEmpty()
        val sharedItemRegex = Regex("(?s)<si\\b[^>]*>(.*?)</si>")
        val sharedTextRegex = Regex("(?s)<t\\b[^>]*>(.*?)</t>")
        val shared = sharedItemRegex.findAll(stringsXml).map { match ->
            sharedTextRegex.findAll(match.groupValues[1]).joinToString("") { xmlDecode(it.groupValues[1]) }
        }.toList()
        val rowRegex = Regex("(?s)<row\\b[^>]*>(.*?)</row>")
        val cellRegex = Regex("(?s)<c\\b([^>]*)>(.*?)</c>")
        val refRegex = Regex("r=\"([A-Z]+)[0-9]+\"")
        val valueRegex = Regex("(?s)<v>(.*?)</v>")
        val inlineTextRegex = Regex("(?s)<t[^>]*>(.*?)</t>")
        val sharedTypeRegex = Regex("t=\"s\"")
        val rows = mutableListOf<MutableMap<Int, String>>()
        // Keep imports responsive on large supplier price books.
        rowRegex.findAll(sheet).take(1000).forEach { rowMatch ->
            val row = mutableMapOf<Int, String>()
            cellRegex.findAll(rowMatch.groupValues[1]).forEach cellLoop@ { cellMatch ->
                val attrs = cellMatch.groupValues[1]; val body = cellMatch.groupValues[2]
                val ref = refRegex.find(attrs)?.groupValues?.get(1) ?: return@cellLoop
                val col = ref.fold(0) { acc, c -> acc * 26 + (c - 'A' + 1) } - 1
                val raw = valueRegex.find(body)?.groupValues?.get(1)
                    ?: inlineTextRegex.find(body)?.groupValues?.get(1).orEmpty()
                val value = if (sharedTypeRegex.containsMatchIn(attrs)) shared.getOrNull(raw.toIntOrNull() ?: -1).orEmpty() else xmlDecode(raw)
                row[col] = value
            }
            if (row.isNotEmpty()) rows += row
        }
        if (rows.isEmpty()) return LocalImportResult(name, "XLSX", "", warnings=listOf("ردیفی از برگه اول استخراج نشد."))
        val maxCol = rows.maxOf { it.keys.maxOrNull() ?: 0 }
        val rendered = rows.take(501).map { row -> (0..maxCol).joinToString(" | ") { row[it].orEmpty() } }
        val header = rendered.firstOrNull().orEmpty()
        val warnings = mutableListOf<String>()
        if (!header.contains("قیمت") && !header.contains("price", true) && !header.contains("فی")) warnings += "ستون قیمت به‌طور خودکار پیدا نشد؛ سرستون‌ها را بررسی کن."
        if (!header.contains("قطعه") && !header.contains("شرح") && !header.contains("نام") && !header.contains("description", true)) warnings += "ستون نام/شرح قطعه به‌طور خودکار پیدا نشد."
        if (rows.size >= 1000) warnings += "برای سرعت، فقط ۱۰۰۰ ردیف اول پردازش شد؛ فایل بزرگ‌تر را به چند بخش تقسیم کن."
        return LocalImportResult(name, "XLSX", rendered.joinToString("\n"), (rows.size - 1).coerceAtLeast(0), warnings)
    }

    fun readImage(uri: Uri): Bitmap? = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }

    private fun xmlDecode(s: String) = s.replace("&amp;", "&").replace("&quot;", "\"").replace("&apos;", "'").replace("&lt;", "<").replace("&gt;", ">")
}
