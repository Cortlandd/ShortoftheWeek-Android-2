package com.cortlandwalker.shortoftheweek.data.recent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.recentSearchesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "recent_searches"
)

@Singleton
class RecentSearchesStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    private val key = stringPreferencesKey("recent_searches_json")

    fun recentSearchesFlow(): Flow<List<String>> {
        return context.recentSearchesDataStore.data.map { prefs ->
            val raw = prefs[key].orEmpty()
            if (raw.isBlank()) return@map emptyList()
            runCatching { json.decodeFromString<List<String>>(raw) }.getOrElse { emptyList() }
        }
    }

    suspend fun add(query: String, max: Int = 12) {
        val cleaned = query.cleanQuery() ?: return
        context.recentSearchesDataStore.edit { prefs ->
            val existing = prefs[key].orEmpty()
            val list = runCatching { json.decodeFromString<List<String>>(existing) }
                .getOrElse { emptyList() }
                .toMutableList()

            // Remove duplicate (case-insensitive), then insert at front.
            val dupIndex = list.indexOfFirst { it.equals(cleaned, ignoreCase = true) }
            if (dupIndex >= 0) list.removeAt(dupIndex)
            list.add(0, cleaned)

            // Bound list
            val bounded = if (list.size > max) list.take(max) else list
            prefs[key] = json.encodeToString(bounded)
        }
    }

    suspend fun remove(query: String) {
        val cleaned = query.cleanQuery() ?: return
        context.recentSearchesDataStore.edit { prefs ->
            val existing = prefs[key].orEmpty()
            val list = runCatching { json.decodeFromString<List<String>>(existing) }
                .getOrElse { emptyList() }
                .filterNot { it.equals(cleaned, ignoreCase = true) }
            prefs[key] = json.encodeToString(list)
        }
    }

    suspend fun clear() {
        context.recentSearchesDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(emptyList<String>())
        }
    }
}

private fun String.cleanQuery(): String? {
    val cleaned = trim().replace(Regex("\\s+"), " ")
    return cleaned.takeIf { it.isNotBlank() }
}
