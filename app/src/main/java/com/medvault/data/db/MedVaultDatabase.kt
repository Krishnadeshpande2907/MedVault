package com.medvault.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medvault.data.dao.AttachmentDao
import com.medvault.data.dao.ContactDao
import com.medvault.data.dao.PrescriptionDao
import com.medvault.data.dao.VisitDao
import com.medvault.data.entity.AttachmentEntity
import com.medvault.data.entity.ContactEntity
import com.medvault.data.entity.PrescriptionEntity
import com.medvault.data.entity.VisitEntity
import com.medvault.data.entity.VisitFts

@Database(
    entities = [
        VisitEntity::class,
        PrescriptionEntity::class,
        AttachmentEntity::class,
        ContactEntity::class,
        VisitFts::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MedVaultDatabase : RoomDatabase() {
    abstract fun visitDao(): VisitDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun contactDao(): ContactDao
}
