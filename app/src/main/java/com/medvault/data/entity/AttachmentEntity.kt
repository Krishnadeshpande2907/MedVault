package com.medvault.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [ForeignKey(
        entity = VisitEntity::class,
        parentColumns = ["visitId"],
        childColumns = ["visitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("visitId")]
)
data class AttachmentEntity(
    @PrimaryKey val mediaId: String,
    val visitId: String,
    val type: String,                       // "xray" | "report" | "before" | "after" | "other"
    val localUri: String,                   // relative path: "media/{visitId}/{mediaId}.jpg"
    val caption: String?,
    val capturedAt: Long
)
