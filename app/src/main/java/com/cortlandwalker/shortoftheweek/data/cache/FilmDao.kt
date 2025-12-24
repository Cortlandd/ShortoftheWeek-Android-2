package com.cortlandwalker.shortoftheweek.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FilmDao {
    @Query("SELECT * FROM film_cache WHERE id = :id LIMIT 1")
    suspend fun get(id: Int): FilmEntity?

    @Query("SELECT * FROM film_cache WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<FilmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<FilmEntity>)
}

