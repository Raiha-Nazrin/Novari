package com.example.novari.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.novari.core.database.entity.SmsProcessingEntity
import com.example.novari.core.database.entity.SmsProcessingStatus

@Dao
interface SmsProcessingDao {

    /** Returns -1 when the fingerprint's unique index rejected the insert. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SmsProcessingEntity): Long

    @Update
    suspend fun update(entity: SmsProcessingEntity)

    @Query("SELECT * FROM sms_processing WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun findByFingerprint(fingerprint: String): SmsProcessingEntity?

    @Query("DELETE FROM sms_processing WHERE status = :status")
    suspend fun deleteByStatus(status: SmsProcessingStatus)
}
