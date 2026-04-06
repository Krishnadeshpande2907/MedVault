package com.medvault.di

import android.content.Context
import androidx.room.Room
import com.medvault.data.dao.AttachmentDao
import com.medvault.data.dao.ContactDao
import com.medvault.data.dao.PrescriptionDao
import com.medvault.data.dao.VisitDao
import com.medvault.data.db.MedVaultDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MedVaultDatabase =
        Room.databaseBuilder(
            context,
            MedVaultDatabase::class.java,
            "medvault.db"
        )
        // Add migrations here as schema evolves:
        // .addMigrations(Migrations.MIGRATION_1_2, ...)
        .build()

    @Provides
    fun provideVisitDao(db: MedVaultDatabase): VisitDao = db.visitDao()

    @Provides
    fun providePrescriptionDao(db: MedVaultDatabase): PrescriptionDao = db.prescriptionDao()

    @Provides
    fun provideAttachmentDao(db: MedVaultDatabase): AttachmentDao = db.attachmentDao()

    @Provides
    fun provideContactDao(db: MedVaultDatabase): ContactDao = db.contactDao()
}
