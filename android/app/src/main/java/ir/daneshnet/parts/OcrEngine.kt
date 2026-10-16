package ir.daneshnet.parts

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.arabic.ArabicScriptRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Arabic-script OCR is used because Persian uses the Arabic script.
 * OCR is a candidate extraction step only; extracted prices must be reviewed.
 */
class OcrEngine(context: Context) {
    private val recognizer = TextRecognition.getClient(
        ArabicScriptRecognizerOptions.Builder().build()
    )

    suspend fun read(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { result ->
                val text = result.text.lines()
                    .map { normalize(it) }
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
                if (continuation.isActive) continuation.resume(text)
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }
    }

    fun close() { recognizer.close() }

    private fun normalize(s: String): String = s
        .replace('ي','ی').replace('ى','ی').replace('ك','ک')
        .replace('٠','۰').replace('١','۱').replace('٢','۲').replace('٣','۳')
        .replace('٤','۴').replace('٥','۵').replace('٦','۶').replace('٧','۷')
        .replace('٨','۸').replace('٩','۹').trim()
}
