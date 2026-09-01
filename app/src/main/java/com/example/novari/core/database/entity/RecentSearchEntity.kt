package com.example.novari.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One entry in the search screen's recent-searches list. [query] is unique
 * (case-insensitive) so repeating a search re-surfaces the existing row at
 * the top instead of creating a duplicate; [searchField] remembers which
 * "Search by" scope the query was run under so tapping it later restores it.
 */
@Entity(
    tableName = "recent_searches",
    indices = [Index(value = ["query"], unique = true)]
)
data class RecentSearchEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val query: String,
    val searchField: String,
    val searchedAt: Long
)
