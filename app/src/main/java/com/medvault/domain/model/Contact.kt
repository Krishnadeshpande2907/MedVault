package com.medvault.domain.model

data class Contact(
    val doctorName: String,
    val doctorPhone: String? = null,
    val hospitalName: String? = null,
    val specialty: String? = null,
    val lastVisited: String                 // "YYYY-MM-DD"
)
