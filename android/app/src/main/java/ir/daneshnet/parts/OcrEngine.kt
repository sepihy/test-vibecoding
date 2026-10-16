package ir.daneshnet.parts

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * Latin OCR for part numbers, SKUs and Latin-script text.
 * Arabic/Persian OCR needs a separate supported engine; ML Kit Text Recognition does not expose
 * the ArabicTextRecognizerOptions API used by the previous implementation.
 */
class OcrEngine(context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun read(bitmap: Bitmap, onResult: (String) -> Unit, onError: (Exception) -> Unit) {
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { result ->
                val text = result.text.lines()
                    .map { normalize(it) }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString("\n")
                if (text.isBlank()) onError(IllegalStateException("OCR متنی پیدا نکرد")) else onResult(text)
            }
            .addOnFailureListener { onError(it) }
    }

    private fun normalize(s: String): String = s
        .replace('ي','ی').replace('ى','ی').replace('ك','ک')
        .replace('٠','۰').replace('١','۱').replace('٢','۲').replace('٣','۳')
        .replace('٤','۴').replace('٥','۵').replace('٦','۶').replace('٧','۷')
        .replace('٨','۸').replace('٩','۹')
        .trim()
}
