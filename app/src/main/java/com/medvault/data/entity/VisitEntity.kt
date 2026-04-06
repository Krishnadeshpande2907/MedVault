package com.medvault.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey val visitId: String,
    val date: String,                       // "YYYY-MM-DD" — indexed for sort
    val doctorName: String,
    val doctorPhone: String?,
    val hospitalName: String?,
    val doctorSpecialty: String?,
    val diagnosis: String?,
    val notes: String?,
    val nextAppointmentDate: String?,
    val tags: String,                       // JSON array as string: '["antibiotics","infection"]'
    val createdAt: Long,
    val updatedAt: Long
)
