package com.cortlandwalker.shortoftheweek.data.models

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

// Used so I don't have to send an entire object with navgraph
@Singleton
class FilmStore @Inject constructor() {
    private val map = ConcurrentHashMap<Int, Film>()

    fun upsertAll(films: List<Film>) {
        for (f in films) map[f.id] = f
    }

    fun get(id: Int): Film? = map[id]
}
