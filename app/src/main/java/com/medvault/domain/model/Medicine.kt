package com.medvault.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Medicine(
    val name: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val instructions: String? = null
)
