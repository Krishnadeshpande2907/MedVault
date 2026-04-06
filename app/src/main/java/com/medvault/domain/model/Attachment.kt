package com.medvault.domain.model

data class Attachment(
    val mediaId: String,
    val visitId: String,
    val type: AttachmentType,
    val localUri: String,                   // relative path
    val caption: String? = null,
    val capturedAt: Long = System.currentTimeMillis()
)

enum class AttachmentType(val label: String) {
    XRAY("X-ray"),
    REPORT("Lab report"),
    BEFORE("Before photo"),
    AFTER("After photo"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): AttachmentType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}
