package com.medvault.domain.model

/**
 * Clean domain model for a visit. ViewModels and UI work with this, not the Room entity.
 */
data class Visit(
    val visitId: String,
    val date: String,                       // "YYYY-MM-DD"
    val doctorName: String,
    val doctorPhone: String? = null,
    val hospitalName: String? = null,
    val doctorSpecialty: String? = null,
    val diagnosis: String? = null,
    val notes: String? = null,
    val nextAppointmentDate: String? = null,
    val tags: List<String> = emptyList(),
    val prescription: Prescription? = null,
    val attachments: List<Attachment> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
