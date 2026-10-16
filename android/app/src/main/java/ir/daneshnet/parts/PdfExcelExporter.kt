package ir.daneshnet.parts

import android.content.ContentResolver
import android.net.Uri
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Writes a minimal standards-compliant XLSX workbook using inline strings.
 * OCR output is kept line-by-line so uncertain table boundaries aren't silently discarded.
 */
class PdfExcelExporter(private val resolver: ContentResolver) {
    fun write(uri: Uri, rows: List<List<String>>) {
        val output = resolver.openOutputStream(uri) ?: error("فایل خروجی برای نوشتن باز نشد.")
        ZipOutputStream(output).use { zip ->
            add(zip, "[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>""")
            add(zip, "_rels/.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""")
            add(zip, "xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="استخراج PDF" sheetId="1" r:id="rId1"/></sheets></workbook>""")
            add(zip, "xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>""")
            val xml = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
            rows.forEachIndexed { rowIndex, row ->
                val r = rowIndex + 1
                xml.append("<row r=\"").append(r).append("\">")
                row.forEachIndexed { colIndex, value ->
                    val ref = columnName(colIndex + 1) + r
                    xml.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                        .append(escapeXml(value)).append("</t></is></c>")
                }
                xml.append("</row>")
            }
            xml.append("</sheetData></worksheet>")
            add(zip, "xl/worksheets/sheet1.xml", xml.toString())
        }
    }

    private fun add(zip: ZipOutputStream, name: String, text: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(text.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun columnName(n: Int): String {
        var value = n
        val out = StringBuilder()
        while (value > 0) {
            val rem = (value - 1) % 26
            out.insert(0, ('A'.code + rem).toChar())
            value = (value - 1) / 26
        }
        return out.toString()
    }

    private fun escapeXml(s: String): String = s
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&apos;")
        .filter { it == '\n' || it == '\r' || it == '\t' || it >= ' ' }
}
