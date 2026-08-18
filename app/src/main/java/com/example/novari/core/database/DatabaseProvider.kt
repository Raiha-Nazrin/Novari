package com.example.novari.core.database

import android.content.Context
import androidx.room.Room
import com.example.novari.BuildConfig
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * The only place that knows how Room is backed by SQLCipher.
 *
 * Keep SQLCipher-specific code out of ViewModels and repositories.
 */
object DatabaseProvider {

    fun create(
        context: Context,
        databasePassword: ByteArray
    ): NovariDatabase {
        // Debug builds skip SQLCipher and use Room's plain SQLite connection so the
        // Android Studio Database Inspector can attach (it cannot parse SQLCipher's
        // encrypted page format). Release builds stay encrypted.
        if (BuildConfig.DEBUG) {
            databasePassword.fill(0)
            return Room.databaseBuilder(
                context,
                NovariDatabase::class.java,
                DATABASE_NAME
            )
                // Add only real, tested migrations here.
                // .addMigrations(NovariMigrations.MIGRATION_1_2)
                .build()
        }

        System.loadLibrary("sqlcipher")

        // Room opens the connection lazily, so this array must stay valid beyond this
        // function's scope. SupportOpenHelperFactory takes ownership of it and clears
        // it itself once the connection is opened. Only the caller's original array,
        // which is no longer needed after this defensive copy, is wiped here.
        val passwordCopy = databasePassword.copyOf()

        return try {
            val factory = SupportOpenHelperFactory(passwordCopy)

            Room.databaseBuilder(
                context,
                NovariDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory)
                // Add only real, tested migrations here.
                // .addMigrations(NovariMigrations.MIGRATION_1_2)
                .build()
        } finally {
            databasePassword.fill(0)
        }
    }

    private const val DATABASE_NAME = "novari.db"
}
