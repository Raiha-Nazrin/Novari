package com.example.novari.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object NovariMigrations {

    // Released migrations must never be edited. Add 2->3, 3->4, etc.

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE categories ADD COLUMN colorKey TEXT")
            db.execSQL("ALTER TABLE categories ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 100")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE sms_processing ADD COLUMN failureReason TEXT")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS merchant_category_rule (
                    id TEXT NOT NULL PRIMARY KEY,
                    merchantPattern TEXT NOT NULL,
                    categoryId TEXT NOT NULL,
                    source TEXT NOT NULL,
                    hitCount INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_merchant_category_rule_merchantPattern " +
                    "ON merchant_category_rule (merchantPattern)"
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE sms_processing ADD COLUMN derivedRevision INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE sms_processing_new (
                    id TEXT NOT NULL,
                    fingerprint TEXT NOT NULL,
                    smsTimestamp INTEGER NOT NULL,
                    sender TEXT,
                    status TEXT NOT NULL,
                    transactionId TEXT,
                    processedAt INTEGER,
                    createdAt INTEGER NOT NULL,
                    derivedRevision INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO sms_processing_new
                    (id, fingerprint, smsTimestamp, sender, status, transactionId, processedAt, createdAt, derivedRevision)
                SELECT id, fingerprint, smsTimestamp, sender, status, transactionId, processedAt, createdAt, derivedRevision
                FROM sms_processing
                """.trimIndent()
            )
            db.execSQL("DROP TABLE sms_processing")
            db.execSQL("ALTER TABLE sms_processing_new RENAME TO sms_processing")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_sms_processing_fingerprint " +
                    "ON sms_processing (fingerprint)"
            )
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS recent_searches (
                    id TEXT NOT NULL PRIMARY KEY,
                    query TEXT NOT NULL COLLATE NOCASE,
                    searchField TEXT NOT NULL,
                    searchedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_recent_searches_query " +
                    "ON recent_searches (query)"
            )
        }
    }
}
