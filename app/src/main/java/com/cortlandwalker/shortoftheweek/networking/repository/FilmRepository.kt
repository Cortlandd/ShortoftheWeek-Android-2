package com.cortlandwalker.shortoftheweek.networking.repository

import android.util.Log
import com.cortlandwalker.shortoftheweek.data.cache.FeedCacheDao
import com.cortlandwalker.shortoftheweek.data.cache.FeedCacheEntity
import com.cortlandwalker.shortoftheweek.data.cache.FilmDao
import com.cortlandwalker.shortoftheweek.data.cache.FilmEntity
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.data.models.normalized
import com.cortlandwalker.shortoftheweek.networking.FilmItem
import com.cortlandwalker.shortoftheweek.networking.SotwApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caching strategy:
 * - In-memory map for quick Film lookup by id.
 * - Room-backed cache for:
 *    - feed pages -> list of film ids + fetchedAt
 *    - film id -> full Film JSON + fetchedAt
 *
 * Default TTL is 1 hour. Pass forceRefresh=true (used by pull-to-refresh) to bypass TTL.
 */
@Singleton
class FilmRepository @Inject constructor(
    private val api: SotwApi,
    private val feedDao: FeedCacheDao,
    private val filmDao: FilmDao,
    private val json: Json
) {
    private val ttlMs: Long = 60L * 60L * 1000L
    private val nowMs: () -> Long = { System.currentTimeMillis() }

    private val tag = "FilmRepository"

    // Helps FilmDetail avoid extra calls if the user navigates quickly.
    private val memoryFilms = ConcurrentHashMap<Int, Film>()

    private fun ageMs(fetchedAtMs: Long): Long = nowMs() - fetchedAtMs
    private fun ttlLeftMs(fetchedAtMs: Long): Long = ttlMs - ageMs(fetchedAtMs)

    suspend fun getFilm(id: Int): Film? {
        memoryFilms[id]?.let {
            Log.d(tag, "getFilm(id=$id) MEMORY_HIT")
            return it
        }

        val entity = filmDao.get(id)
        if (entity == null) {
            Log.d(tag, "getFilm(id=$id) DB_MISS")
            return null
        }

        val film = runCatching { json.decodeFromString<Film>(entity.filmJson) }.getOrNull()
        return film?.also {
            memoryFilms[id] = it
            Log.d(
                tag,
                "getFilm(id=$id) DB_HIT ageMs=${ageMs(entity.fetchedAtMs)} ttlLeftMs=${ttlLeftMs(entity.fetchedAtMs)}"
            )
        }
    }

    /** Backwards-compatible name used by reducers. */
    suspend fun getCachedFilm(id: Int): Film? = getFilm(id)

    suspend fun mixed(page: Int, limit: Int = 10, forceRefresh: Boolean = false): List<Film> =
        getFeed(key = "mixed:$page:$limit", forceRefresh = forceRefresh, trimPrefix = null) {
            api.mixed(page = page, limit = limit).data
        }

    suspend fun news(page: Int, limit: Int = 10, forceRefresh: Boolean = false): List<Film> =
        getFeed(key = "news:$page:$limit", forceRefresh = forceRefresh, trimPrefix = null) {
            api.news(page = page, limit = limit).data
        }

    suspend fun search(q: String, page: Int, limit: Int = 10): List<Film> {
        val q = URLEncoder.encode(q.trim(), "UTF-8")
        return api.search(query = q, page = page, limit = limit)
            .data
            .map(Film.Companion::from)
    }

    private suspend fun getFeed(
        key: String,
        forceRefresh: Boolean,
        trimPrefix: String?,
        loader: suspend () -> List<FilmItem>
    ): List<Film> {
        val cached = feedDao.get(key)
        if (cached == null) {
            Log.d(tag, "FEED[$key] DB_INDEX_MISS -> NETWORK (forceRefresh=$forceRefresh)")
        } else {
            val age = ageMs(cached.fetchedAtMs)
            val valid = !forceRefresh && age < ttlMs
            Log.d(
                tag,
                "FEED[$key] DB_INDEX_HIT ageMs=$age ttlMs=$ttlMs valid=$valid forceRefresh=$forceRefresh"
            )
        }

        val canUseCache = cached != null && !forceRefresh && (nowMs() - cached.fetchedAtMs) < ttlMs
        if (canUseCache) {
            val ids = runCatching { json.decodeFromString<List<Int>>(cached!!.filmIdsJson) }
                .getOrElse {
                    Log.w(tag, "FEED[$key] idsJson decode FAILED -> NETWORK", it)
                    emptyList()
                }

            Log.d(tag, "FEED[$key] idsCount=${ids.size}")

            if (ids.isNotEmpty()) {
                val entities = filmDao.getByIds(ids)
                Log.d(tag, "FEED[$key] filmsFromDb=${entities.size} expected=${ids.size}")

                if (entities.size == ids.size) {
                    val byId = entities.associateBy({ it.id }) { e ->
                        runCatching { json.decodeFromString<Film>(e.filmJson) }.getOrNull()
                    }
                    val films = ids.mapNotNull { byId[it] }.map { it.normalized() }
                    if (films.size == ids.size) {
                        films.forEach { memoryFilms[it.id] = it }
                        Log.d(tag, "FEED[$key] CACHE_HIT -> RETURN ${films.size} films")
                        return films
                    } else {
                        Log.d(tag, "FEED[$key] CACHE_PARTIAL (decoded=${films.size}/${ids.size}) -> NETWORK")
                    }
                }
            }
            // Fall through to network if cache was incomplete.
        }

        val fetchedAt = nowMs()
        val items = loader()
        val films = items.map(Film.Companion::from)

        // Persist films + feed index.
        val filmEntities = films.map { film ->
            FilmEntity(id = film.id, fetchedAtMs = fetchedAt, filmJson = json.encodeToString(film))
        }
        filmDao.upsertAll(filmEntities)
        films.forEach { memoryFilms[it.id] = it }

        val idsJson = json.encodeToString(films.map { it.id })
        feedDao.upsert(FeedCacheEntity(key = key, fetchedAtMs = fetchedAt, filmIdsJson = idsJson))

        if (trimPrefix != null) {
            // Keep the most recent search results only (bounded disk usage).
            feedDao.trimPrefix(trimPrefix, keep = 25)
        }

        return films
    }
}
