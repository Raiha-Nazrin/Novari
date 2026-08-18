package com.example.novari.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.novari.core.database.dao.CategoryDao
import com.example.novari.core.database.dao.SmsProcessingDao
import com.example.novari.core.database.dao.TransactionDao
import com.example.novari.core.database.entity.*

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        SmsProcessingEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(NovariConverters::class)
abstract class NovariDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun smsProcessingDao(): SmsProcessingDao
}
