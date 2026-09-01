package com.example.novari.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.novari.core.database.entity.SmsProcessingEntity
import com.example.novari.core.database.entity.SmsProcessingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsProcessingDao {

    /** Returns -1 when the fingerprint's unique index rejected the insert. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SmsProcessingEntity): Long

    @Update
    suspend fun update(entity: SmsProcessingEntity)

    @Query("SELECT * FROM sms_processing WHERE fingerprint IN (:fingerprints) LIMIT 1")
    suspend fun findByFingerprint(fingerprints: List<String>): SmsProcessingEntity?

    @Query("DELETE FROM sms_processing WHERE status = :status")
    suspend fun deleteByStatus(status: SmsProcessingStatus)

    @Query("DELETE FROM sms_processing WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM sms_processing WHERE status = 'PROCESSED' AND derivedRevision < :currentRevision")
    suspend fun findStaleProcessed(currentRevision: Int): List<SmsProcessingEntity>

    @Query("SELECT COUNT(*) FROM sms_processing WHERE status = 'PROCESSED'")
    fun observeProcessedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sms_processing WHERE status = 'IGNORED'")
    fun observeSilentlyIgnoredCount(): Flow<Int>
}
