package com.medvault.domain.model

data class Prescription(
    val prescriptionId: String,
    val visitId: String,
    val rawText: String? = null,
    val photoUri: String? = null,           // relative path
    val medicines: List<Medicine> = emptyList(),
    val aiSummary: AiSummary? = null,       // TODO: PHASE 2 — AI
    val aiSummaryGeneratedAt: Long? = null  // TODO: PHASE 2 — AI
)
