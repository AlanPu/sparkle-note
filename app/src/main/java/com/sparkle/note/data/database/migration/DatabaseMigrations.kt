package com.sparkle.note.data.database.migration

/**
 * Database migration definitions for Sparkle Note.
 * Handles schema changes and data migration between versions.
 * Currently empty as we're starting with version 1.
 */
object DatabaseMigrations {
    
    /**
     * Placeholder for future migrations.
     * When database version needs to be updated, add migration logic here.
     * 
     * Example future migrations:
     * - Adding new columns: ALTER TABLE inspirations ADD COLUMN new_column TEXT
     * - Adding indexes: CREATE INDEX idx_created_at ON inspirations(created_at)
     * - Adding new tables: CREATE TABLE themes (...)
     */
    
    /**
     * List of all available migrations.
     * Used when building the Room database.
     * Currently empty as we're starting with version 1.
     */
    val ALL_MIGRATIONS: Array<Any> = emptyArray()
}