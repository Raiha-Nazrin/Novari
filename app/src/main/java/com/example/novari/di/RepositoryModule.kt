package com.example.novari.di

import com.example.novari.data.repository.RoomTransactionRepository
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.sms.repository.RoomSmsProcessingRepository
import com.example.novari.sms.repository.SmsProcessingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTransactionRepository(impl: RoomTransactionRepository): TransactionRepository

    @Binds
    abstract fun bindSmsProcessingRepository(impl: RoomSmsProcessingRepository): SmsProcessingRepository
}
