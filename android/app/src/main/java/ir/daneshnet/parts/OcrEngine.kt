package ir.daneshnet.parts

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device Latin-script OCR for part numbers/SKUs. The bundled ML Kit model does not support
 * Persian/Arabic script; the UI must warn users and allow review/correction.
 */
class OcrEngine(context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun read(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { result ->
                val text = result.text.lines()
                    .map { normalize(it) }
                    .filter { it.isNotBlank() }
                    .distinct()
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
