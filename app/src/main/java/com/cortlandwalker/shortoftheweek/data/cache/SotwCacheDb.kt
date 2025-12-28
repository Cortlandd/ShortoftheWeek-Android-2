package com.cortlandwalker.shortoftheweek.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FeedCacheEntity::class, FilmEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SotwCacheDb : RoomDatabase() {
    abstract fun feedCacheDao(): FeedCacheDao
    abstract fun filmDao(): FilmDao
    abstract fun bookmarkDao(): BookmarkDao
}

