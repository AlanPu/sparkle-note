package com.sparkle.note.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migration from version 1 to version 2.
 * Introduces independent theme management with themes table and foreign key relationships.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create themes table
        db.execSQL("""
            CREATE TABLE themes (
                name TEXT PRIMARY KEY NOT NULL,
                icon TEXT NOT NULL DEFAULT '💡',
                color INTEGER NOT NULL DEFAULT ${0xFF4A90E2},
                description TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                lastUsed INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                inspirationCount INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // Get distinct theme names from existing inspirations
        val cursor = db.query("SELECT DISTINCT theme_name FROM inspirations WHERE content != '__THEME_MARKER__'")
        val themes = mutableListOf<String>()
        
        while (cursor.moveToNext()) {
            val themeName = cursor.getString(0)
            if (themeName.isNotBlank() && themeName != "__THEME_MARKER__") {
                themes.add(themeName)
            }
        }
        cursor.close()
        
        // Insert themes into the new table
        for (themeName in themes) {
            db.execSQL("""
                INSERT INTO themes (name, icon, color, description, createdAt, lastUsed, inspirationCount)
                VALUES (?, '💡', ${0xFF4A90E2}, '', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 
                    (SELECT COUNT(*) FROM inspirations WHERE theme_name = ? AND content != '__THEME_MARKER__'))
            """, arrayOf(themeName, themeName))
        }
        
        // Add foreign key constraint to inspirations table (we need to recreate the table)
        // First, create a temporary table with the foreign key
        db.execSQL("""
            CREATE TABLE inspirations_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                content TEXT NOT NULL,
                theme_name TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                word_count INTEGER NOT NULL,
                FOREIGN KEY (theme_name) REFERENCES themes(name) ON DELETE CASCADE ON UPDATE CASCADE
            )
        """)
        
        // Copy data from old table to new table, excluding __THEME_MARKER__ entries
        db.execSQL("""
            INSERT INTO inspirations_new (id, content, theme_name, created_at, word_count)
            SELECT id, content, theme_name, created_at, word_count 
            FROM inspirations 
            WHERE content != '__THEME_MARKER__'
        """)
        
        // Drop old table and rename new table
        db.execSQL("DROP TABLE inspirations")
        db.execSQL("ALTER TABLE inspirations_new RENAME TO inspirations")
        
        // Create indices on the new table
        db.execSQL("CREATE INDEX index_inspirations_content ON inspirations(content)")
        db.execSQL("CREATE INDEX index_inspirations_theme_name ON inspirations(theme_name)")
    }
}