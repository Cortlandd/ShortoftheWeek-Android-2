package com.cortlandwalker.shortoftheweek.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedCacheDao {
    @Query("SELECT * FROM feed_cache WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): FeedCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FeedCacheEntity)

    /**
     * Keep only the newest [keep] entries for keys starting with [prefix] (e.g., "search:").
     */
    @Query(
        "DELETE FROM feed_cache " +
            "WHERE `key` LIKE :prefix || '%' " +
            "AND `key` NOT IN (" +
              "SELECT `key` FROM feed_cache " +
              "WHERE `key` LIKE :prefix || '%' " +
              "ORDER BY fetchedAtMs DESC " +
              "LIMIT :keep" +
            ")"
    )
    suspend fun trimPrefix(prefix: String, keep: Int)
}
