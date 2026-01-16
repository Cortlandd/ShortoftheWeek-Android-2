package com.cortlandwalker.shortoftheweek.networking.repository

import android.util.Log
import com.cortlandwalker.shortoftheweek.data.cache.BookmarkDao
import com.cortlandwalker.shortoftheweek.data.cache.BookmarkEntity
import com.cortlandwalker.shortoftheweek.data.cache.FeedCacheDao
import com.cortlandwalker.shortoftheweek.data.cache.FeedCacheEntity
import com.cortlandwalker.shortoftheweek.data.cache.FilmDao
import com.cortlandwalker.shortoftheweek.data.cache.FilmEntity
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.data.models.normalized
import com.cortlandwalker.shortoftheweek.networking.FilmItem
import com.cortlandwalker.shortoftheweek.networking.SotwApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
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
    private val bookmarkDao: BookmarkDao,
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
        // 1. Try Memory or Disk
        var film = memoryFilms[id]
        if (film == null) {
            val entity = filmDao.get(id)
            if (entity != null) {
                film = runCatching { json.decodeFromString<Film>(entity.filmJson) }.getOrNull()
            }
        }

        // 2. If found, ALWAYS ensure bookmark state is accurate
        if (film != null) {
            val isBookmarked = bookmarkDao.isBookmarked(id)
            // Only copy if necessary to save allocation
            val finalFilm = if (film.isBookmarked != isBookmarked) {
                film.copy(isBookmarked = isBookmarked)
            } else {
                film
            }
            // Update memory with the correct version
            memoryFilms[id] = finalFilm
            return finalFilm
        }

        return null
    }

    /** Backwards-compatible name used by reducers. */
    suspend fun getCachedFilm(id: Int): Film? = getFilm(id)

    suspend fun films(page: Int, limit: Int = 10, forceRefresh: Boolean = false): List<Film> {
        val raw = getFeed(key = "mixed:$page:$limit", forceRefresh = forceRefresh, trimPrefix = null) {
            api.films(page = page, limit = limit).data
        }
        return hydrateBookmarks(raw)
    }

    suspend fun news(page: Int, limit: Int = 10, forceRefresh: Boolean = false): List<Film> {
        val raw = getFeed(key = "news:$page:$limit", forceRefresh = forceRefresh, trimPrefix = null) {
            api.news(page = page, limit = limit).data
        }
        return hydrateBookmarks(raw)
    }

    suspend fun search(q: String, page: Int, limit: Int = 10): List<Film> {
        val qEnc = URLEncoder.encode(q.trim(), "UTF-8")
        val raw = api.search(query = qEnc, page = page, limit = limit)
            .data
            .map(Film.Companion::from)
        return hydrateBookmarks(raw)
    }

    /**
     * Synchronizes a list of [Film] objects with the local bookmark database.
     *
     * This function fetches all currently bookmarked film IDs from the local Room database
     * and efficiently maps the [Film.isBookmarked] property for each item in the provided list.
     * This ensures that lists coming from the network (which default to isBookmarked=false)
     * correctly reflect the user's local saved state.
     *
     * @param films The list of films (e.g., from a feed or search result) to update.
     * @return A new list of films where [Film.isBookmarked] is true if the film exists in the local DB.
     */
    private suspend fun hydrateBookmarks(films: List<Film>): List<Film> {
        val bookmarkedIds = bookmarkDao.getBookmarkedIds().toSet()
        return films.map { film ->
            if (bookmarkedIds.contains(film.id)) film.copy(isBookmarked = true) else film
        }
    }

    fun getBookmarkedFilmsFlow(): Flow<List<Film>> {
        return bookmarkDao.getAllBookmarksFlow()
            .map { entities ->
                entities.mapNotNull { entity ->
                    runCatching {
                        json.decodeFromString<Film>(entity.filmJson).copy(isBookmarked = true)
                    }.getOrNull()
                }
            }
    }

    suspend fun toggleBookmark(film: Film) {
        val isCurrentlyBookmarked = bookmarkDao.isBookmarked(film.id)

        if (isCurrentlyBookmarked) {
            bookmarkDao.delete(film.id)
            // Update memory cache to reflect removal
            memoryFilms[film.id] = film.copy(isBookmarked = false)
        } else {
            val entity = BookmarkEntity(
                filmId = film.id,
                addedAtMs = System.currentTimeMillis(),
                filmJson = json.encodeToString(film.copy(isBookmarked = true))
            )
            bookmarkDao.insert(entity)
            // Update memory cache
            memoryFilms[film.id] = film.copy(isBookmarked = true)
        }
    }

    suspend fun getBookmarkedFilms(): List<Film> {
        return bookmarkDao.getAllBookmarks().mapNotNull { entity ->
            runCatching {
                json.decodeFromString<Film>(entity.filmJson).copy(isBookmarked = true)
            }.getOrNull()
        }
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
