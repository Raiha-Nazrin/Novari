package com.example.novari.sms.repository

import com.example.novari.core.database.dao.SmsProcessingDao
import com.example.novari.core.database.entity.SmsProcessingEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSmsProcessingRepository @Inject constructor(
    private val dao: SmsProcessingDao
) : SmsProcessingRepository {

    override suspend fun findByFingerprint(fingerprint: String): SmsProcessingEntity? =
        dao.findByFingerprint(fingerprint)

    override suspend fun save(record: SmsProcessingEntity): Boolean =
        dao.insert(record) != -1L
}
