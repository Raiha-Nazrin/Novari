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
     * [merchantQuery] matches merchant or notes, [merchantKeys] narrows by exact
     * normalized merchant (pass an empty set for no merchant filter), [categoryIds]
     * narrows by category (pass an empty set for no category filter),
     * [minAmountMinor]/[maxAmountMinor] bound the amount (minor units), and
     * [startInclusive]/[endInclusive] bound the date range (epoch millis, both-or-neither).
     */
    @Query("""
        SELECT * FROM transactions
        WHERE deletedAt IS NULL
        AND (:merchantQuery IS NULL OR merchant LIKE '%' || :merchantQuery || '%' OR notes LIKE '%' || :merchantQuery || '%')
        AND (:hasMerchantFilter = 0 OR UPPER(TRIM(merchant)) IN (:merchantKeys))
        AND (:hasCategoryFilter = 0 OR categoryId IN (:categoryIds))
        AND (:minAmountMinor IS NULL OR amountMinor >= :minAmountMinor)
        AND (:maxAmountMinor IS NULL OR amountMinor <= :maxAmountMinor)
        AND (:startInclusive IS NULL OR transactionDate BETWEEN :startInclusive AND :endInclusive)
        ORDER BY transactionDate DESC, createdAt DESC
    """)
    fun observeSearch(
        merchantQuery: String?,
        hasMerchantFilter: Boolean,
        merchantKeys: List<String>,
        hasCategoryFilter: Boolean,
        categoryIds: List<String>,
        minAmountMinor: Long?,
        maxAmountMinor: Long?,
        startInclusive: Long?,
        endInclusive: Long?
    ): Flow<List<TransactionEntity>>

    /**
     * Distinct merchants derived from active transactions, grouped by normalized
     * (uppercase, trimmed) form so "SWIGGY*BANGALORE" and "SWIGGY " collapse to one
     * entry. Ordered by frequency -- the merchants a user transacts with most are the
     * ones most useful to filter by. [MerchantSummary.merchant] returns the raw spelling
     * of whichever row `GROUP BY` happens to keep, which SQLite guarantees is a row from
     * the group when combined with MAX/COUNT aggregates in the same query.
     */
    @Query("""
        SELECT merchant AS merchant, COUNT(*) AS transactionCount, MAX(transactionDate) AS lastTransactionDate
        FROM transactions
        WHERE deletedAt IS NULL AND merchant IS NOT NULL AND TRIM(merchant) <> ''
        GROUP BY UPPER(TRIM(merchant))
        ORDER BY transactionCount DESC, lastTransactionDate DESC
    """)
    fun observeMerchants(): Flow<List<MerchantSummary>>

    @Query("SELECT * FROM transactions WHERE sourceReference = :reference LIMIT 1")
    suspend fun findBySourceReference(reference: String): TransactionEntity?
}
