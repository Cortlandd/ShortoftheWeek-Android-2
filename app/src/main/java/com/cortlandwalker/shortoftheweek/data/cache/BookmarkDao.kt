package com.cortlandwalker.shortoftheweek.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    // Add this Flow-based query
    @Query("SELECT * FROM bookmarks ORDER BY addedAtMs DESC")
    fun getAllBookmarksFlow(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY addedAtMs DESC")
    fun getAllBookmarks(): List<BookmarkEntity>

    @Query("SELECT filmId FROM bookmarks")
    fun getBookmarkedIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE filmId = :id")
    suspend fun delete(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE filmId = :id)")
    suspend fun isBookmarked(id: Int): Boolean
}