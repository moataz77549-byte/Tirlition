package com.rateel.app.data.local

/**
 * Add explicit Room Migration objects here whenever the schema version increases.
 * Production database construction must never enable destructive fallback.
 */
object RateelMigrations {
    private val from1To2 = object : androidx.room.migration.Migration(1, 2) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE mushafs ADD COLUMN availableSurahs TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE TABLE IF NOT EXISTS surah_metadata (number INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, startPage INTEGER, endPage INTEGER, isMakki INTEGER)")
        }
    }
    private val from2To3 = object : androidx.room.migration.Migration(2, 3) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE playback_progress ADD COLUMN completed INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE listening_history ADD COLUMN titleSnapshot TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE listening_history ADD COLUMN sourceId TEXT")
            db.execSQL("ALTER TABLE listening_history ADD COLUMN playedDurationMs INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE listening_history ADD COLUMN completed INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE recordings ADD COLUMN rightsSnapshot TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE recordings ADD COLUMN isComplete INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE recordings ADD COLUMN codec TEXT")
        }
    }
    private val from3To4 = object : androidx.room.migration.Migration(3, 4) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE downloads ADD COLUMN contentId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE downloads ADD COLUMN expectedSize INTEGER")
            db.execSQL("ALTER TABLE downloads ADD COLUMN mimeType TEXT")
            db.execSQL("ALTER TABLE downloads ADD COLUMN format TEXT")
            db.execSQL("ALTER TABLE downloads ADD COLUMN checksumAlgorithm TEXT")
            db.execSQL("ALTER TABLE downloads ADD COLUMN failureReason TEXT")
            db.execSQL("ALTER TABLE downloads ADD COLUMN etag TEXT")
            db.execSQL("ALTER TABLE downloads ADD COLUMN lastModified TEXT")
            db.execSQL("ALTER TABLE downloads ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE downloads ADD COLUMN startedAt INTEGER")
            db.execSQL("ALTER TABLE downloads ADD COLUMN completedAt INTEGER")
            db.execSQL("ALTER TABLE downloads ADD COLUMN lastVerifiedAt INTEGER")
            db.execSQL("ALTER TABLE downloads ADD COLUMN retryCount INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE downloads ADD COLUMN expiresAt INTEGER")
            db.execSQL("UPDATE downloads SET contentId = id WHERE contentId = ''")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_downloads_contentId ON downloads(contentId)")
        }
    }
    val all = arrayOf(from1To2, from2To3, from3To4)
}
