package com.example.novari.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.novari.core.database.entity.SmsProcessingEntity

@Dao
interface SmsProcessingDao {

    @Insert
    suspend fun insert(entity: SmsProcessingEntity)

    @Update
    suspend fun update(entity: SmsProcessingEntity)

    @Query("SELECT * FROM sms_processing WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun findByFingerprint(fingerprint: String): SmsProcessingEntity?
}
