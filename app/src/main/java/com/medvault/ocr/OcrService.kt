package com.medvault.ocr

import android.content.Context
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
 * On-device, no API key needed. All processing happens locally.
 */
@Singleton
class OcrService @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extract text from an image URI using ML Kit Text Recognition.
     * Suspends until ML Kit finishes processing.
     */
    suspend fun extractTextFromImage(context: Context, imageUri: Uri): String =
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
     * Best-effort parsing of OCR text into [Medicine] objects.
     *
     * Pass 1 — Dosage-keyed lines:
     *   Look for lines containing a dosage pattern (e.g. "500mg", "250 ml").
     *   Extract medicine name as the text before the dosage token.
     *
     * Pass 2 — Frequency/duration-keyed lines:
     *   For lines that passed Pass 1, attempt to detect frequency and duration
     *   from the same line or the very next line.
     *
     * Pass 3 — Fallback lines:
     *   Lines without a dosage but containing known frequency abbreviations
     *   (BD, TDS, OD, QID, etc.) are added as name-only entries so the user
     *   can fill in the dosage manually.
     *
     * Results always need user review and correction.
     */
    fun parseMedicinesFromText(rawText: String): List<Medicine> {
        if (rawText.isBlank()) return emptyList()

        val lines = rawText
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val medicines = mutableListOf<Medicine>()
        val usedIndices = mutableSetOf<Int>()

        val dosagePattern = Regex(
            """(\d+(?:\.\d+)?\s*(?:mg|ml|mcg|g|iu|units?|tab(?:let)?s?|cap(?:sule)?s?))""",
            RegexOption.IGNORE_CASE
        )
        val frequencyPattern = Regex(
            """(\d+-\d+-\d+|\b(?:od|bd|tds|qid|sos|stat|nocte|mane|once\s+daily|twice\s+daily|thrice\s+daily|once\s+a\s+day|twice\s+a\s+day|every\s+\d+\s+hours?)\b)""",
            RegexOption.IGNORE_CASE
        )
        val durationPattern = Regex(
            """(\d+\s*(?:day|week|month|fortnight)s?)""",
            RegexOption.IGNORE_CASE
        )

        // Pass 1 & 2: dosage-keyed lines
        lines.forEachIndexed { index, line ->
            val dosageMatch = dosagePattern.find(line) ?: return@forEachIndexed

            val dosage = dosageMatch.value.trim()
            val nameRaw = line.substring(0, dosageMatch.range.first).trim().trimEnd('-', ':', ' ', '.')
            if (nameRaw.isBlank()) return@forEachIndexed

            // Scan current line + next line for frequency and duration
            val searchText = buildString {
                append(line.substring(dosageMatch.range.last + 1))
                if (index + 1 < lines.size) {
                    append(" ")
                    append(lines[index + 1])
                }
            }

            val frequency = frequencyPattern.find(searchText)?.value?.trim() ?: ""
            val duration = durationPattern.find(searchText)?.value?.trim() ?: ""

            medicines.add(
                Medicine(
                    name = nameRaw,
                    dosage = dosage,
                    frequency = frequency,
                    duration = duration,
                    instructions = null
                )
            )
            usedIndices.add(index)
            if (frequency.isNotEmpty() || duration.isNotEmpty()) {
                usedIndices.add(index + 1) // mark next line as consumed
            }
        }

        // Pass 3: frequency-keyed fallback (no dosage found)
        lines.forEachIndexed { index, line ->
            if (index in usedIndices) return@forEachIndexed
            val freqMatch = frequencyPattern.find(line) ?: return@forEachIndexed

            val nameRaw = line.substring(0, freqMatch.range.first).trim().trimEnd('-', ':', ' ', '.')
            if (nameRaw.isBlank()) return@forEachIndexed

            val frequency = freqMatch.value.trim()
            val duration = durationPattern.find(
                line.substring(freqMatch.range.last + 1)
            )?.value?.trim() ?: ""

            medicines.add(
                Medicine(
                    name = nameRaw,
                    dosage = "",
                    frequency = frequency,
                    duration = duration,
                    instructions = null
                )
            )
            usedIndices.add(index)
        }

        return medicines
    }
}
