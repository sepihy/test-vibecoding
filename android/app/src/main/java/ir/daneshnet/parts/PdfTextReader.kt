package ir.daneshnet.parts

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

/** Extracts selectable text directly from text-based PDFs. Scanned PDFs need OCR. */
class PdfTextReader(private val context: Context) {
    fun read(uri: Uri, maxPages: Int = 20): String {
        PDFBoxResourceLoader.init(context.applicationContext)
        val input = context.contentResolver.openInputStream(uri) ?: error("فایل PDF باز نشد.")
        input.use { stream ->
            PDDocument.load(stream).use { document ->
                val stripper = PDFTextStripper()
                stripper.startPage = 1
                stripper.endPage = minOf(document.numberOfPages, maxPages)
                return stripper.getText(document).trim()
            }
        }
    }
}
