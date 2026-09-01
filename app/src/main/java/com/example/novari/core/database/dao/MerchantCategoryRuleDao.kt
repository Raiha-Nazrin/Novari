package com.example.novari.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.novari.core.database.entity.MerchantCategoryRuleEntity

@Dao
interface MerchantCategoryRuleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rules: List<MerchantCategoryRuleEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rule: MerchantCategoryRuleEntity): Long

    @Update
    suspend fun update(rule: MerchantCategoryRuleEntity)

    @Query("SELECT * FROM merchant_category_rule WHERE merchantPattern = :pattern LIMIT 1")
    suspend fun findByPattern(pattern: String): MerchantCategoryRuleEntity?

    @Query("SELECT COUNT(*) FROM merchant_category_rule")
    suspend fun count(): Int

    // LEARNED rules outrank SEED rules regardless of hit count; a user correction is a
    // stronger signal than a bundled default. Ties within a source break on hitCount so a
    // more-confirmed rule wins over a rarely-matched one.
    @Query(
        """
        SELECT * FROM merchant_category_rule
        WHERE :merchant LIKE '%' || merchantPattern || '%'
        ORDER BY CASE source WHEN 'LEARNED' THEN 0 ELSE 1 END, hitCount DESC
        LIMIT 1
        """
    )
    suspend fun findMatch(merchant: String): MerchantCategoryRuleEntity?
}
