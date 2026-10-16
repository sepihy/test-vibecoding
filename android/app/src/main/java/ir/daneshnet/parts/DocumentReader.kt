package ir.daneshnet.parts

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor

class DocumentReader(private val resolver: ContentResolver) {
    fun pages(uri: Uri, maxPages: Int = 50): List<Bitmap> {
        val pfd: ParcelFileDescriptor = resolver.openFileDescriptor(uri, "r") ?: return emptyList()
        PdfRenderer(pfd).use { renderer ->
            val count = minOf(renderer.pageCount, maxPages)
            return (0 until count).map { index ->
                renderer.openPage(index).use { page ->
                    val width = 1800
                    val height = (width.toFloat() * page.height / page.width).toInt()
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
                        it.eraseColor(Color.WHITE)
                        page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    }
                }
            }
        }
    }
}
