package com.medvault.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val doctorName: String,
    val doctorPhone: String?,
    val hospitalName: String?,
    val specialty: String?,
    val lastVisited: String                 // "YYYY-MM-DD"
)
