package com.example.novari.core.database
import androidx.room.TypeConverter
import com.example.novari.core.database.entity.SmsProcessingStatus
import com.example.novari.core.model.TransactionSource
import com.example.novari.core.model.TransactionType

class NovariConverters {

    @TypeConverter
    fun transactionTypeToStorage(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun storageToTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    @TypeConverter
    fun transactionSourceToStorage(value: TransactionSource): String {
        return value.name
    }

    @TypeConverter
    fun storageToTransactionSource(value: String): TransactionSource {
        return TransactionSource.valueOf(value)
    }

    @TypeConverter
    fun smsStatusToStorage(value: SmsProcessingStatus): String {
        return value.name
    }

    @TypeConverter
    fun storageToSmsStatus(value: String): SmsProcessingStatus {
        return SmsProcessingStatus.valueOf(value)
    }
}
