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
     * Live view of a single transaction, including soft-deleted rows -- the
     * detail screen needs to see the row disappear (as null) right after a
     * delete rather than keep showing stale data.
     */
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<TransactionEntity?>

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

    /**
     * Backs SearchScreen. Every filter is optional -- a null/empty argument is a
     * no-op -- so the single query serves whichever "Search by" scope is active:
     * [merchantQuery] matches merchant or notes, [categoryIds] narrows by category
     * (pass an empty set for no category filter), [minAmountMinor]/[maxAmountMinor]
     * bound the amount (minor units), and [startInclusive]/[endInclusive] bound the
     * date range (epoch millis, both-or-neither).
     */
    @Query("""
        SELECT * FROM transactions
        WHERE deletedAt IS NULL
        AND (:merchantQuery IS NULL OR merchant LIKE '%' || :merchantQuery || '%' OR notes LIKE '%' || :merchantQuery || '%')
        AND (:hasCategoryFilter = 0 OR categoryId IN (:categoryIds))
        AND (:minAmountMinor IS NULL OR amountMinor >= :minAmountMinor)
        AND (:maxAmountMinor IS NULL OR amountMinor <= :maxAmountMinor)
        AND (:startInclusive IS NULL OR transactionDate BETWEEN :startInclusive AND :endInclusive)
        ORDER BY transactionDate DESC, createdAt DESC
    """)
    fun observeSearch(
        merchantQuery: String?,
        hasCategoryFilter: Boolean,
        categoryIds: List<String>,
        minAmountMinor: Long?,
        maxAmountMinor: Long?,
        startInclusive: Long?,
        endInclusive: Long?
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE sourceReference = :reference LIMIT 1")
    suspend fun findBySourceReference(reference: String): TransactionEntity?
}
