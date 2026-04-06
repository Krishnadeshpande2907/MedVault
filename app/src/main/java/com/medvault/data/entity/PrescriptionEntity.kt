package com.medvault.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prescriptions",
    foreignKeys = [ForeignKey(
        entity = VisitEntity::class,
        parentColumns = ["visitId"],
        childColumns = ["visitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("visitId")]
)
data class PrescriptionEntity(
    @PrimaryKey val prescriptionId: String,
    val visitId: String,
    val rawText: String?,
    val photoUri: String?,                  // relative path: "prescriptions/{visitId}.jpg"
    val medicines: String,                  // JSON array of Medicine objects
    val aiSummary: String?,                 // JSON of AiSummary object — null until Phase 2
    val aiSummaryGeneratedAt: Long?         // epoch millis — null until Phase 2
)
