package com.cortlandwalker.shortoftheweek.core.navigation
import android.net.Uri
import androidx.compose.animation.core.copy
import com.cortlandwalker.shortoftheweek.data.models.Film
import kotlinx.serialization.Serializable
import com.google.gson.Gson

// We use type-safe navigation (available in newer Compose Nav versions)
// or simple sealed classes if using older versions. 
// Let's use simple string routes for compatibility with your current setup.

object Routes {
    const val Home = "home"
    const val News = "news"
    const val Search = "search"
    const val Bookmarks = "bookmarks"
    
    // Detail route with arguments
    const val Detail = "detail/{filmJson}?prefix={originPrefix}"

    fun detail(film: Film, originPrefix: String): String {
        val json = Gson().toJson(film)
        val encodedJson = Uri.encode(json)
        return "detail/$encodedJson?prefix=$originPrefix"
    }
}