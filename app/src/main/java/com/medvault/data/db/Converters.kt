package com.medvault.data.db

import androidx.room.TypeConverter
import com.medvault.domain.model.AiSummary
import com.medvault.domain.model.Medicine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverters for JSON-serialized columns.
 * Handles Medicine list <-> JSON string and AiSummary <-> JSON string.
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    // ── Medicine list ↔ JSON ──────────────────────────────────────

    @TypeConverter
    fun medicinesToJson(medicines: List<Medicine>): String =
        json.encodeToString(medicines)

    @TypeConverter
    fun jsonToMedicines(value: String): List<Medicine> =
        try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }

    // ── AiSummary ↔ JSON (Phase 2) ───────────────────────────────

    @TypeConverter
    fun aiSummaryToJson(summary: AiSummary?): String? =
        summary?.let { json.encodeToString(it) }

    @TypeConverter
    fun jsonToAiSummary(value: String?): AiSummary? =
        value?.let {
            try {
                json.decodeFromString(it)
            } catch (e: Exception) {
                null
            }
        }

    // ── String list (tags) ↔ JSON ────────────────────────────────

    @TypeConverter
    fun stringListToJson(list: List<String>): String =
        json.encodeToString(list)

    @TypeConverter
    fun jsonToStringList(value: String): List<String> =
        try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
}
