package com.rateel.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RateelMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE radio_stations ADD COLUMN categoryOrigin TEXT NOT NULL DEFAULT 'UNKNOWN'")
            db.execSQL("ALTER TABLE radio_stations ADD COLUMN sourceHealth TEXT NOT NULL DEFAULT 'UNKNOWN'")
            db.execSQL("ALTER TABLE radio_streams ADD COLUMN returnedBySourceId TEXT")
            db.execSQL("ALTER TABLE radio_streams ADD COLUMN originalUrl TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE radio_streams ADD COLUMN resolvedUrl TEXT")
            db.execSQL("ALTER TABLE radio_streams ADD COLUMN assetHost TEXT")
            db.execSQL("ALTER TABLE radio_streams ADD COLUMN resolvedHost TEXT")
            db.execSQL("ALTER TABLE radio_streams ADD COLUMN lastResolvedAt INTEGER")
            db.execSQL("ALTER TABLE radio_streams ADD COLUMN health TEXT NOT NULL DEFAULT 'UNKNOWN'")
            db.execSQL("ALTER TABLE radio_streams ADD COLUMN assetRightsStatus TEXT NOT NULL DEFAULT 'INHERIT_SOURCE'")
            db.execSQL("ALTER TABLE audio_tracks ADD COLUMN assetRightsStatus TEXT NOT NULL DEFAULT 'INHERIT_SOURCE'")
            db.execSQL("CREATE TABLE IF NOT EXISTS quran_languages (id TEXT NOT NULL, code TEXT NOT NULL, name TEXT NOT NULL, nativeName TEXT NOT NULL, PRIMARY KEY(id))")
            db.execSQL("CREATE TABLE IF NOT EXISTS quran_surahs (number INTEGER NOT NULL, name TEXT NOT NULL, startPage INTEGER, endPage INTEGER, isMakki INTEGER, PRIMARY KEY(number))")
            db.execSQL("CREATE TABLE IF NOT EXISTS riwayat (id INTEGER NOT NULL, name TEXT NOT NULL, PRIMARY KEY(id))")
            db.execSQL("CREATE TABLE IF NOT EXISTS source_sync (syncKey TEXT NOT NULL, sourceId TEXT NOT NULL, lastAttemptAt INTEGER NOT NULL, lastSuccessfulSyncAt INTEGER, expiresAt INTEGER, PRIMARY KEY(syncKey))")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE audio_tracks ADD COLUMN reciterId TEXT")
        }
    }

    val all = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
