package com.cortlandwalker.shortoftheweek.data.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val filmId: Int,
    val addedAtMs: Long,
    val filmJson: String
)
