package com.example.novari.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object NovariMigrations {

    // Example for the first future schema change:
    //
    // val MIGRATION_1_2 = object : Migration(1, 2) {
    //     override fun migrate(db: SupportSQLiteDatabase) {
    //         db.execSQL("ALTER TABLE transactions ADD COLUMN example TEXT")
    //     }
    // }
    //
    // Released migrations must never be edited. Add 2->3, 3->4, etc.
}
