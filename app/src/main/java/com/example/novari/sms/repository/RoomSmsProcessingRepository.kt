package com.example.novari.sms.repository

import com.example.novari.core.database.dao.SmsProcessingDao
import com.example.novari.core.database.entity.SmsProcessingEntity
import com.example.novari.core.database.entity.SmsProcessingStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSmsProcessingRepository @Inject constructor(
    private val dao: SmsProcessingDao
) : SmsProcessingRepository {

    override suspend fun findByFingerprint(fingerprints: List<String>): SmsProcessingEntity? =
        dao.findByFingerprint(fingerprints)

    override suspend fun save(record: SmsProcessingEntity): Boolean =
        dao.insert(record) != -1L

    override suspend fun deleteByStatus(status: SmsProcessingStatus) =
        dao.deleteByStatus(status)

    override suspend fun deleteById(id: String) =
        dao.deleteById(id)

    override suspend fun findStaleProcessed(currentRevision: Int): List<SmsProcessingEntity> =
        dao.findStaleProcessed(currentRevision)

    override fun observeProcessedCount(): Flow<Int> =
        dao.observeProcessedCount()

    override fun observeSilentlyIgnoredCount(): Flow<Int> =
        dao.observeSilentlyIgnoredCount()
}
