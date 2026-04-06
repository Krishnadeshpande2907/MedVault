package com.medvault.ocr

import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.medvault.domain.model.Medicine
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service for extracting text from prescription photos using ML Kit.
 * On-device, no API key needed.
 */
@Singleton
class OcrService @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extract text from an image URI using ML Kit Text Recognition.
     */
    suspend fun extractTextFromImage(context: android.content.Context, imageUri: Uri): String =
        suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        continuation.resume(result.text)
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    /**
     * Best-effort parsing of OCR text into medicine objects.
     * This is a heuristic parser — results may need user correction.
     */
    fun parseMedicinesFromText(rawText: String): List<Medicine> {
        if (rawText.isBlank()) return emptyList()

        val medicines = mutableListOf<Medicine>()
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotBlank() }

        for (line in lines) {
            // Simple heuristic: look for lines with medicine-like patterns
            // (word followed by dosage like "500mg", "250 mg", etc.)
            val dosagePattern = Regex("""(\d+\s*(?:mg|ml|mcg|g|iu|units?))\b""", RegexOption.IGNORE_CASE)
            val match = dosagePattern.find(line)

            if (match != null) {
                val dosage = match.value
                val nameEnd = match.range.first
                val name = line.substring(0, nameEnd).trim().trimEnd('-', ':', ' ')

                if (name.isNotBlank()) {
                    medicines.add(
                        Medicine(
                            name = name,
                            dosage = dosage,
                            frequency = "",
                            duration = "",
                            instructions = null
                        )
                    )
                }
            }
        }

        return medicines
    }
}
