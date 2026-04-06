package com.medvault.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for file I/O operations in context.filesDir.
 * Room stores relative paths; this resolves them to absolute paths at read time.
 */
@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val baseDir: File
        get() = File(context.filesDir, "medvault")

    init {
        // Ensure directory structure exists
        File(baseDir, "prescriptions").mkdirs()
        File(baseDir, "media").mkdirs()
    }

    /**
     * Resolve a relative path to an absolute File.
     */
    fun resolveFile(relativePath: String): File =
        File(context.filesDir, relativePath)

    /**
     * Resolve a relative path to an absolute path string.
     */
    fun resolveAbsolutePath(relativePath: String): String =
        File(context.filesDir, relativePath).absolutePath

    /**
     * Copy an image from a source URI to the target relative path.
     * Returns the relative path on success.
     */
    suspend fun saveImage(sourceUri: Uri, targetRelativePath: String): String =
        withContext(Dispatchers.IO) {
            val targetFile = File(context.filesDir, targetRelativePath)
            targetFile.parentFile?.mkdirs()

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Cannot open input stream for $sourceUri")

            targetRelativePath
        }

    /**
     * Copy a file from source bytes to the target relative path.
     */
    suspend fun saveFile(bytes: ByteArray, targetRelativePath: String): String =
        withContext(Dispatchers.IO) {
            val targetFile = File(context.filesDir, targetRelativePath)
            targetFile.parentFile?.mkdirs()
            targetFile.writeBytes(bytes)
            targetRelativePath
        }

    /**
     * Delete a file at the given relative path.
     */
    suspend fun deleteFile(relativePath: String) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, relativePath)
        if (file.exists()) file.delete()
    }

    /**
     * Delete all files for a given visit (prescription photo + media folder).
     */
    suspend fun deleteVisitFiles(visitId: String) = withContext(Dispatchers.IO) {
        // Delete prescription photo
        val prescriptionFile = File(baseDir, "prescriptions/$visitId.jpg")
        if (prescriptionFile.exists()) prescriptionFile.delete()

        // Delete media folder
        val mediaDir = File(baseDir, "media/$visitId")
        if (mediaDir.exists()) mediaDir.deleteRecursively()
    }

    /**
     * Export all data as a ZIP file. Returns the URI of the created ZIP.
     * Includes the Room database file and all media files.
     */
    suspend fun exportDataAsZip(): File = withContext(Dispatchers.IO) {
        val zipFile = File(context.cacheDir, "medvault_export.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // Add database file
            val dbFile = context.getDatabasePath("medvault.db")
            if (dbFile.exists()) {
                addFileToZip(zos, dbFile, "medvault.db")
            }

            // Add all media files
            if (baseDir.exists()) {
                baseDir.walkTopDown().filter { it.isFile }.forEach { file ->
                    val entryPath = "medvault/${file.relativeTo(baseDir).path}"
                    addFileToZip(zos, file, entryPath)
                }
            }
        }
        zipFile
    }

    /**
     * Import data from a ZIP file. Replaces current database and media files.
     */
    suspend fun importDataFromZip(zipUri: Uri) = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(zipUri)?.use { input ->
            ZipInputStream(input).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val targetFile = if (entry.name == "medvault.db") {
                            context.getDatabasePath("medvault.db")
                        } else {
                            File(context.filesDir, entry.name)
                        }
                        targetFile.parentFile?.mkdirs()
                        FileOutputStream(targetFile).use { output ->
                            zis.copyTo(output)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }
    }

    /**
     * Delete all data — media files and database.
     */
    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        if (baseDir.exists()) baseDir.deleteRecursively()
        baseDir.mkdirs()
        File(baseDir, "prescriptions").mkdirs()
        File(baseDir, "media").mkdirs()
    }

    private fun addFileToZip(zos: ZipOutputStream, file: File, entryName: String) {
        zos.putNextEntry(ZipEntry(entryName))
        file.inputStream().use { it.copyTo(zos) }
        zos.closeEntry()
    }
}
