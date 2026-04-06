package com.medvault.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.medvault.data.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE visitId = :visitId ORDER BY capturedAt ASC")
    fun getForVisit(visitId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE visitId = :visitId ORDER BY capturedAt ASC")
    suspend fun getForVisitOnce(visitId: String): List<AttachmentEntity>

    @Upsert
    suspend fun upsertAttachment(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE mediaId = :mediaId")
    suspend fun deleteAttachment(mediaId: String)

    @Query("DELETE FROM attachments WHERE visitId = :visitId")
    suspend fun deleteForVisit(visitId: String)

    @Query("SELECT COUNT(*) FROM attachments WHERE visitId = :visitId")
    suspend fun getCountForVisit(visitId: String): Int
}
