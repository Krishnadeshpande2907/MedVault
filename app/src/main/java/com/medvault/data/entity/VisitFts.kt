package com.medvault.data.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = VisitEntity::class)
@Entity(tableName = "visit_fts")
data class VisitFts(
    val doctorName: String,
    val hospitalName: String,
    val diagnosis: String,
    val notes: String,
    val tags: String
)
