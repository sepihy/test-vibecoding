package ir.daneshnet.parts

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor

class DocumentReader(private val resolver: ContentResolver) {
    fun firstPages(uri: Uri, maxPages: Int = 10): List<Bitmap> {
        val pfd: ParcelFileDescriptor = resolver.openFileDescriptor(uri, "r") ?: return emptyList()
        PdfRenderer(pfd).use { renderer ->
            val count = minOf(renderer.pageCount, maxPages)
            return (0 until count).map { index ->
                renderer.openPage(index).use { page ->
                    val width = 1500
                    val height = (width.toFloat() * page.height / page.width).toInt()
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            }
        }
    }
}
