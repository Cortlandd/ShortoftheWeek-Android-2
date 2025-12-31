# ![Short of the Week](app/src/main/res/drawable/sotw_full_logo_white.png)

_Disclaimer: This is an unofficial, fan-made project and is not affiliated with Short of the Week._

An **unofficial** Android application for [Short of the Week](https://www.shortoftheweek.com), built with modern Android development practices. This app provides a native experience for browsing, searching, and watching the incredible collection of short films curated by the SotW team. I hand parse the Articles that are written with the films instead of a webview due to the complexities.

## ✨ Features

-   **Dynamic Home Feed**: A mixed feed of the latest films and news, similar to the website.
-   **News Section**: A dedicated tab for articles and industry news.
-   **Advanced Search**: Full-text search with a history of recent queries.
-   **Local Bookmarks**: Save your favorite films for offline viewing.
-   **Seamless Video Playback**: Integrated YouTube and Vimeo players that support fullscreen.
-   **Shared Element Transitions**: Smooth, animated transitions from film thumbnails to the detail screen for a polished user experience.
-   **Parallax Detail Screen**: A visually engaging film detail screen where the article content scrolls over the hero image.
-   **Efficient Caching**: A robust caching layer (memory and disk) to minimize network usage and provide a fast, offline-first experience.
-   **Modern Tech Stack**: Built entirely with Jetpack Compose and the latest Android architecture components.

## 🛠 Tech Stack & Architecture

-   **UI**: 100% [Jetpack Compose](https://developer.android.com/jetpack/compose) for a declarative and modern UI.
    -   **Shared Element Transitions**: Implemented with `SharedTransitionLayout` for seamless navigation.
    -   **Material 3**: For theming and UI components.
-   **Architecture**: Follows a MVI/Redux-like pattern using a custom `ViewModelReducer` class.
    -   **State**: Single source of truth for each screen's state.
    -   **Actions**: Events triggered by the UI or system.
    -   **Reducer**: Processes actions and produces new states.
    -   **Effects**: Side-effects for one-off events like navigation.
-   **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html) and [Flow](https://kotlinlang.org/docs/flow.html) are used extensively for background tasks, network calls, and database operations.
-   **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) for managing dependencies throughout the app.
-   **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) for all screen-to-screen routing.
-   **Networking**:
    -   [Retrofit](https://square.github.io/retrofit/) for type-safe HTTP requests to the SotW API.
    -   [GSON](https://github.com/google/gson) for JSON serialization.
-   **Data Persistence**:
    -   [Room](https://developer.android.com/training/data-storage/room) for caching feeds and film details.
    -   [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for storing recent search queries.
-   **Image Loading**: [Glide (Compose Integration)](https://bumptech.github.io/glide/int/compose.html) for efficient loading and caching of images.
-   **Video Playback**:
    -   [Android-YouTube-Player](https://github.com/PierfrancescoSoffritti/android-youtube-player) for a robust, lifecycle-aware YouTube player.
    -   `WebView` for the embedded Vimeo player.

## 🏗 Project Structure

The project is organized by feature, promoting separation of concerns and scalability.
```
com.cortlandwalker.shortoftheweek
├── core/             # Core components: ViewModelReducer, helpers, navigation 
├── data/             # Data models, caching (Room), and preference stores
├── features/         # Individual feature packages (home, news, search, detail, bookmarks) 
│  └── home/ 
│     ├── Home.kt          # State, Action, Effect definitions
│     ├── HomeScreen.kt    # Composable UI 
│     └── HomeReducer.kt   # ViewModel logic 
├── networking/       # Retrofit API interface and repository implementation 
│ 
└── ui/               # Global UI components, theme, and custom views
```

## 🚀 Getting Started
1.  Clone the repository: `git clone https://github.com/cortlandd/ShortoftheWeek-android-2.git`
2. Open the project in Android Studio.
3. Let Gradle sync the dependencies.
4. Run the app


## FAQ
> Q: you work there?

A: nah, i just like the website