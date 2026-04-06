package com.medvault.data.db.migrations

/**
 * Room migration definitions.
 *
 * Never use fallbackToDestructiveMigration() in production.
 * Define a migration object for every schema change.
 *
 * Example:
 * ```
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE visits ADD COLUMN newField TEXT")
 *     }
 * }
 * ```
 *
 * Register in DatabaseModule.kt:
 * .addMigrations(MIGRATION_1_2, MIGRATION_2_3, ...)
 */
object Migrations {
    // No migrations yet — v1 is the initial schema.
    // Add migrations here as the schema evolves.
}
