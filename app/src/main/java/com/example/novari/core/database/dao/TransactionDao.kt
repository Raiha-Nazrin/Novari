package com.example.novari.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.novari.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(entity: TransactionEntity)

    @Update
    suspend fun update(entity: TransactionEntity)

    @Query("""
        SELECT * FROM transactions
        WHERE deletedAt IS NULL
        ORDER BY transactionDate DESC, createdAt DESC
    """)
    fun observeActive(): Flow<List<TransactionEntity>>

    /**
     * Same ordering as [observeActive] but capped in SQL, so a screen showing
     * only the newest few rows doesn't re-map the entire table on every write.
     */
    @Query("""
        SELECT * FROM transactions
        WHERE deletedAt IS NULL
        ORDER BY transactionDate DESC, createdAt DESC
        LIMIT :limit
    """)
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): TransactionEntity?

    /**
     * All active transactions whose [TransactionEntity.transactionDate] falls
     * within [startInclusive, endInclusive] (both epoch millis), newest first.
     * Used by TransactionListScreen to show a single calendar month at a time.
     */
    @Query("""
        SELECT * FROM transactions
        WHERE deletedAt IS NULL
        AND transactionDate BETWEEN :startInclusive AND :endInclusive
        ORDER BY transactionDate DESC, createdAt DESC
    """)
    fun observeBetween(startInclusive: Long, endInclusive: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE deletedAt IS NULL
        AND merchant LIKE '%' || :query || '%'
        ORDER BY transactionDate DESC, createdAt DESC
    """)
    fun searchActive(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE sourceReference = :reference LIMIT 1")
    suspend fun findBySourceReference(reference: String): TransactionEntity?
}
