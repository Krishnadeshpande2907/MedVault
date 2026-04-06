package com.medvault.data.repository

import com.medvault.domain.model.AiSummary
import com.medvault.domain.model.Medicine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for AI-generated prescription summaries.
 *
 * // TODO: PHASE 2 — AI
 * This is a stub. In Phase 2, implement with:
 * - Retrofit + OkHttp for API calls (Gemini Flash or GPT-4o-mini)
 * - API key stored in EncryptedSharedPreferences
 * - Privacy warning before first use
 * - Response parsing into AiSummary data class
 *
 * The ViewModel calls generateSummary() and does not know or care
 * whether it hits Gemini, OpenAI, or a future on-device model.
 * This makes swapping the AI backend a one-file change.
 */
@Singleton
class AiSummaryRepository @Inject constructor() {

    /**
     * Check if AI features are available (API key configured).
     * // TODO: PHASE 2 — AI: Check EncryptedSharedPreferences for API key
     */
    fun isAiAvailable(): Boolean = false

    /**
     * Generate a plain-language summary of a prescription.
     * Returns null if AI is not configured or the call fails.
     *
     * // TODO: PHASE 2 — AI: Implement with Retrofit API call
     *
     * The prompt to send:
     * "You are a helpful medical assistant. Summarise the following prescription
     * in plain language that a non-medical person can understand. Be concise.
     * Do not add any information not present in the input. Output a JSON object
     * with three keys: whatYouWerePrescribed, why (null if no diagnosis),
     * whatToWatchFor. Prescription data: {serialized input}"
     */
    suspend fun generateSummary(
        medicines: List<Medicine>,
        diagnosis: String?,
        notes: String?
    ): AiSummary? {
        // TODO: PHASE 2 — AI: Replace with actual API call
        return null
    }
}
