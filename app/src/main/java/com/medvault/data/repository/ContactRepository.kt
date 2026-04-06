package com.medvault.data.repository

import com.medvault.data.dao.ContactDao
import com.medvault.data.entity.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for doctor contacts, used for autocomplete in Add Visit form.
 */
@Singleton
class ContactRepository @Inject constructor(
    private val contactDao: ContactDao
) {

    /**
     * Search contacts by doctor name or hospital name.
     * Returns up to 10 matches sorted by most recent visit.
     */
    suspend fun searchDoctors(query: String): List<ContactEntity> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) {
                contactDao.getAll()
            } else {
                contactDao.search(query)
            }
        }

    /**
     * Upsert a contact — called on every visit save.
     * If doctor already exists, updates lastVisited and any changed fields.
     */
    suspend fun upsertContact(contact: ContactEntity) =
        withContext(Dispatchers.IO) {
            contactDao.upsertContact(contact)
        }
}
