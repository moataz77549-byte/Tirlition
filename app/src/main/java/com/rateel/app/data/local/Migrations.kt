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
    val all = arrayOf(from1To2)
}
