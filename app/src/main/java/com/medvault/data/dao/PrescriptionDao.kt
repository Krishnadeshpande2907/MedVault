package com.medvault.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.medvault.data.entity.PrescriptionEntity

@Dao
interface PrescriptionDao {

    @Query("SELECT * FROM prescriptions WHERE visitId = :visitId")
    suspend fun getForVisit(visitId: String): PrescriptionEntity?

    @Upsert
    suspend fun upsertPrescription(prescription: PrescriptionEntity)

    @Query("DELETE FROM prescriptions WHERE visitId = :visitId")
    suspend fun deleteForVisit(visitId: String)
}
