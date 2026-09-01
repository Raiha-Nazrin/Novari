package com.example.novari.di

import com.example.novari.data.repository.RoomCategoryRepository
import com.example.novari.data.repository.RoomMerchantCategoryRuleRepository
import com.example.novari.data.repository.RoomRecentSearchRepository
import com.example.novari.data.repository.RoomTransactionRepository
import com.example.novari.domain.repository.CategoryRepository
import com.example.novari.domain.repository.MerchantCategoryRuleRepository
import com.example.novari.domain.repository.RecentSearchRepository
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.sms.health.DefaultSmsDetectionHealthRepository
import com.example.novari.sms.health.SmsDetectionHealthRepository
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

    @Binds
    abstract fun bindCategoryRepository(impl: RoomCategoryRepository): CategoryRepository

    @Binds
    abstract fun bindMerchantCategoryRuleRepository(
        impl: RoomMerchantCategoryRuleRepository
    ): MerchantCategoryRuleRepository

    @Binds
    abstract fun bindSmsDetectionHealthRepository(
        impl: DefaultSmsDetectionHealthRepository
    ): SmsDetectionHealthRepository

    @Binds
    abstract fun bindRecentSearchRepository(
        impl: RoomRecentSearchRepository
    ): RecentSearchRepository
}
