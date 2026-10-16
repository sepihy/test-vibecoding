package ir.daneshnet.parts

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.arabic.ArabicTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class OcrEngine(context: Context) {
    private val arabic = TextRecognition.getClient(ArabicTextRecognizerOptions.Builder().build())
    private val latin = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun read(bitmap: Bitmap, onResult: (String) -> Unit, onError: (Exception) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        arabic.process(image).addOnSuccessListener { ar ->
            latin.process(image).addOnSuccessListener { en ->
                val merged = merge(ar.text, en.text)
                if (merged.isBlank()) onError(IllegalStateException("OCR returned no text")) else onResult(merged)
            }.addOnFailureListener { 
                if (ar.text.isNotBlank()) onResult(ar.text) else onError(it)
            }
        }.addOnFailureListener {
            latin.process(image).addOnSuccessListener { onResult(it.text) }
                .addOnFailureListener { onError(it) }
        }
    }

    private fun merge(arabicText: String, latinText: String): String {
        val lines = (arabicText.lines() + latinText.lines())
            .map { normalize(it) }
            .filter { it.isNotBlank() }
            .distinct()
        return lines.joinToString("\n")
    }

    private fun normalize(s: String): String = s
        .replace('ي','ی').replace('ى','ی').replace('ك','ک')
        .replace('٠','۰').replace('١','۱').replace('٢','۲').replace('٣','۳')
        .replace('٤','۴').replace('٥','۵').replace('٦','۶').replace('٧','۷')
        .replace('٨','۸').replace('٩','۹')
        .trim()
}
