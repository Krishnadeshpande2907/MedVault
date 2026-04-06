package com.medvault.domain.model

import kotlinx.serialization.Serializable

/**
 * AI-generated summary of a prescription.
 * // TODO: PHASE 2 — AI: This model is ready but not populated until AI is integrated.
 */
@Serializable
data class AiSummary(
    val whatYouWerePrescribed: String,
    val why: String? = null,        // null if no diagnosis provided
    val whatToWatchFor: String
)
