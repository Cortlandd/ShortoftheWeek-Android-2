package com.cortlandwalker.shortoftheweek.data.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_cache")
data class FeedCacheEntity(
    @PrimaryKey val key: String,
    val fetchedAtMs: Long,
    val filmIdsJson: String
)

