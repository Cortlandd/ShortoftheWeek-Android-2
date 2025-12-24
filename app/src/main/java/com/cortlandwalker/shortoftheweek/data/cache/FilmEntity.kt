package com.cortlandwalker.shortoftheweek.data.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "film_cache")
data class FilmEntity(
    @PrimaryKey val id: Int,
    val fetchedAtMs: Long,
    val filmJson: String
)

