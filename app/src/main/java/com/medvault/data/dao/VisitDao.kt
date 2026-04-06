package com.medvault.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.medvault.data.entity.VisitEntity
import com.medvault.data.relation.VisitWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {

    @Query("SELECT * FROM visits ORDER BY date DESC")
    fun getAllVisitsDesc(): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits ORDER BY date ASC")
    fun getAllVisitsAsc(): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE visitId = :id")
    suspend fun getVisitById(id: String): VisitEntity?

    @Transaction
    @Query("SELECT * FROM visits WHERE visitId = :id")
    suspend fun getVisitWithDetails(id: String): VisitWithDetails?

    @Transaction
    @Query("SELECT * FROM visits ORDER BY date DESC")
    fun getAllVisitsWithDetails(): Flow<List<VisitWithDetails>>

    @Upsert
    suspend fun upsertVisit(visit: VisitEntity)

    @Query("DELETE FROM visits WHERE visitId = :id")
    suspend fun deleteVisit(id: String)

    @Query(
        """
        SELECT visits.* FROM visits 
        JOIN visit_fts ON visits.rowid = visit_fts.rowid 
        WHERE visit_fts MATCH :query 
        ORDER BY date DESC
        """
    )
    fun search(query: String): Flow<List<VisitEntity>>

    @Query("SELECT DISTINCT tags FROM visits WHERE tags != '[]'")
    suspend fun getAllTags(): List<String>
}
