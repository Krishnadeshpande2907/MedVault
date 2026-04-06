package com.medvault.data.repository

import com.medvault.data.dao.AttachmentDao
import com.medvault.data.dao.PrescriptionDao
import com.medvault.data.dao.VisitDao
import com.medvault.data.entity.AttachmentEntity
import com.medvault.data.entity.PrescriptionEntity
import com.medvault.data.entity.VisitEntity
import com.medvault.data.relation.VisitWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central repository for visit data.
 * Coordinates across VisitDao, PrescriptionDao, and AttachmentDao.
 * Callers (ViewModels) never talk to DAOs directly.
 */
@Singleton
class VisitRepository @Inject constructor(
    private val visitDao: VisitDao,
    private val prescriptionDao: PrescriptionDao,
    private val attachmentDao: AttachmentDao
) {

    fun getAllVisitsDesc(): Flow<List<VisitWithDetails>> =
        visitDao.getAllVisitsWithDetails()

    fun searchVisits(query: String): Flow<List<VisitEntity>> =
        visitDao.search(query)

    suspend fun getVisitById(id: String): VisitWithDetails? =
        withContext(Dispatchers.IO) {
            visitDao.getVisitWithDetails(id)
        }

    /**
     * Save a visit with its prescription and attachments in a single operation.
     * Updates the visit, upserts the prescription, and upserts all attachments.
     */
    suspend fun saveVisit(
        visit: VisitEntity,
        prescription: PrescriptionEntity?,
        attachments: List<AttachmentEntity> = emptyList()
    ) = withContext(Dispatchers.IO) {
        visitDao.upsertVisit(visit)
        prescription?.let { prescriptionDao.upsertPrescription(it) }
        attachments.forEach { attachmentDao.upsertAttachment(it) }
    }

    /**
     * Delete a visit. Room CASCADE handles prescription + attachment rows.
     * Caller must also delete physical files via MediaRepository.
     */
    suspend fun deleteVisit(id: String) = withContext(Dispatchers.IO) {
        // Get attachment paths before deleting (for file cleanup by caller)
        visitDao.deleteVisit(id)
    }

    /**
     * Get all attachments for a visit (for file cleanup before deletion).
     */
    suspend fun getAttachmentsForVisit(visitId: String): List<AttachmentEntity> =
        withContext(Dispatchers.IO) {
            attachmentDao.getForVisitOnce(visitId)
        }

    /**
     * Get the prescription for a visit.
     */
    suspend fun getPrescriptionForVisit(visitId: String): PrescriptionEntity? =
        withContext(Dispatchers.IO) {
            prescriptionDao.getForVisit(visitId)
        }

    /**
     * Get all distinct tags across visits.
     */
    suspend fun getAllTags(): List<String> = withContext(Dispatchers.IO) {
        visitDao.getAllTags()
    }

    /**
     * Delete a single attachment.
     */
    suspend fun deleteAttachment(mediaId: String) = withContext(Dispatchers.IO) {
        attachmentDao.deleteAttachment(mediaId)
    }
}
