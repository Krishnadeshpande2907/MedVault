package com.medvault.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.medvault.data.entity.ContactEntity

@Dao
interface ContactDao {

    @Query(
        """
        SELECT * FROM contacts 
        WHERE doctorName LIKE '%' || :query || '%' 
           OR hospitalName LIKE '%' || :query || '%'
        ORDER BY lastVisited DESC 
        LIMIT 10
        """
    )
    suspend fun search(query: String): List<ContactEntity>

    @Query("SELECT * FROM contacts ORDER BY lastVisited DESC")
    suspend fun getAll(): List<ContactEntity>

    @Upsert
    suspend fun upsertContact(contact: ContactEntity)
}
