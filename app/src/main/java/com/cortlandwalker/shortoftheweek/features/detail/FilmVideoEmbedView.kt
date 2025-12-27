package com.cortlandwalker.shortoftheweek.features.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.net.URL
import java.util.Locale

/**
 * A composite view that determines which video provider (YouTube or Vimeo) to use
 * based on the provided [url] and renders the appropriate embedded player.
 *
 * @param url The raw URL string from the film data (e.g., a YouTube or Vimeo link).
 * @param modifier The modifier to apply to the container.
 */
@Composable
fun FilmVideoEmbedView(url: String, modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) return

    // Parse the URL to determine the provider and extract the ID
    val embed = remember(url) { VideoEmbed.from(url) }

    // Keying by 'embed' ensures we don't swap players unnecessarily,
    // but allows the internal composables to manage their own view instances safely.
    key(embed) {
        when (val provider = embed?.provider) {
            is VideoProvider.YouTube -> {
                YouTubePlayerCompose(
                    videoId = provider.id
                )
            }

            is VideoProvider.Vimeo -> {
                VimeoEmbedWebView(provider.id, modifier)
            }

            null -> {
                // Fallback for unrecognized URLs or missing data
                IframeWebView(
                    baseUrl = "https://www.shortoftheweek.com",
                    html = minimalFallbackHtml(url),
                    modifier = modifier
                )
            }
        }
    }
}

/**
 * Renders a Vimeo video using a WebView.
 *
 * Handles fullscreen toggling by capturing the custom view from [WebChromeClient]
 * and attaching it directly to the Activity's Window decor view, forcing landscape mode.
 *
 * @param vimeoId The numeric Vimeo video ID.
 * @param modifier Modifier for the WebView container.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VimeoEmbedWebView(
    vimeoId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Helper to unwrap ContextWrappers (like Hilt) to get the raw Activity
    val activity = remember(context) { context.findActivity() }

    // State for the fullscreen overlay view provided by the WebView
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // 1. Create the WebView instance explicitly and remember it.
    // This prevents the WebView from being destroyed/recreated by AndroidView recompositions,
    // which would stop playback.
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            // Hardware acceleration is required for video playback
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(android.graphics.Color.BLACK)
        }
    }

    // 2. Set the WebChromeClient to handle "Enter Fullscreen" events from the iframe.
    // Done in a DisposableEffect to capture state setters correctly.
    DisposableEffect(webView) {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view == null) return
                customView = view
                customViewCallback = callback
            }

            override fun onHideCustomView() {
                customViewCallback?.onCustomViewHidden()
                customView = null
                customViewCallback = null
            }
        }
        onDispose {
            webView.destroy()
        }
    }

    // 3. Handle System Back Button to exit fullscreen instead of closing the screen
    BackHandler(enabled = customView != null) {
        customViewCallback?.onCustomViewHidden()
        customView = null
    }

    // 4. Handle Fullscreen Window Overlay Logic
    // When customView is set, attach it to the Window and hide system bars.
    DisposableEffect(customView) {
        val view = customView
        if (view != null && activity != null) {
            view.setBackgroundColor(android.graphics.Color.BLACK)
            val root = activity.window.decorView as ViewGroup
            root.addView(view, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))

            // Hide UI bars
            val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

            // Force Landscape
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        onDispose {
            // Cleanup: Remove view, show bars, reset orientation
            if (view != null && activity != null) {
                val root = activity.window.decorView as ViewGroup
                root.removeView(view)

                val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    // 5. Generate the HTML wrapper for the Vimeo Iframe
    val html = remember(vimeoId) {
        """
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <style>
              html, body { margin:0; padding:0; height:100%; background:black; overflow:hidden; }
              iframe { position:absolute; top:0; left:0; width:100%; height:100%; border:0; }
            </style>
          </head>
          <body>
            <iframe
              src="https://player.vimeo.com/video/$vimeoId?autoplay=1&title=0&byline=0&portrait=0"
              frameborder="0"
              allow="autoplay; fullscreen; picture-in-picture"
              allowfullscreen>
            </iframe>
          </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { _ ->
            // Return the already-created WebView instance
            webView
        },
        update = { view ->
            // Only load if content changed to avoid reloading on simple recompositions
            if (view.tag as? String != html) {
                view.loadDataWithBaseURL(
                    "https://player.vimeo.com",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
                view.tag = html
            }
        }
    )
}

/**
 * Renders a YouTube video using the Android-YouTube-Player library.
 *
 * Configured to support manual fullscreen handling by lifting the player view
 * into the Activity's Window decor view.
 *
 * @param videoId The YouTube Video ID.
 */
@Composable
private fun YouTubePlayerCompose(videoId: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = remember(context) { context.findActivity() }

    // State to track if we are in fullscreen mode to handle the View overlay
    var fullscreenView by remember { mutableStateOf<View?>(null) }

    // Setter to be called from the inner anonymous object listener
    val setFullscreenView = { view: View? -> fullscreenView = view }

    // Handle Back Button to exit fullscreen
    BackHandler(enabled = fullscreenView != null) {
        // Resetting orientation triggers the player to exit fullscreen mode implicitly via its internal listener
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        fullscreenView = null
    }

    // Handle YouTube Fullscreen Overlay Logic
    DisposableEffect(fullscreenView) {
        val view = fullscreenView
        if (view != null && activity != null) {
            view.setBackgroundColor(android.graphics.Color.BLACK)
            val root = activity.window.decorView as ViewGroup
            // Add YouTube's fullscreen view to the root window
            root.addView(view, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))

            val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        onDispose {
            if (view != null && activity != null) {
                val root = activity.window.decorView as ViewGroup
                root.removeView(view)

                val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        factory = {
            YouTubePlayerView(context).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                enableAutomaticInitialization = false

                // Manually build options to enable the fullscreen button in the iFrame
                val options = IFramePlayerOptions.Builder(context)
                    .controls(1)
                    .fullscreen(1)
                    .rel(0)
                    .build()

                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.cueVideo(videoId, 0f)
                    }
                }, options)

                addFullscreenListener(object : FullscreenListener {
                    override fun onEnterFullscreen(
                        fullscreenView: View,
                        exitFullscreen: () -> Unit
                    ) {
                        setFullscreenView(fullscreenView)
                    }

                    override fun onExitFullscreen() {
                        setFullscreenView(null)
                    }
                })
            }
        },
        update = { view ->
            val currentVid = view.tag as? String
            if (currentVid != videoId) {
                view.tag = videoId
                view.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.cueVideo(videoId, 0f)
                    }
                })
            }
        }
    )

    // Ensure Lifecycle management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ -> }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

/**
 * A basic fallback WebView for unrecognized video URLs.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun IframeWebView(baseUrl: String, html: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.BLACK)
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
            }
        },
        update = { webView ->
            if (webView.tag as? String != html) {
                webView.loadDataWithBaseURL(baseUrl, html, "text/html", "utf-8", null)
                webView.tag = html
            }
        }
    )
}

// MARK: - Helper Logic

/**
 * Recursively unwraps the Context to find the underlying Activity.
 * Essential for Hilt/Navigation setups where LocalContext returns a ContextWrapper.
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

fun extractVimeoId(url: String): String? {
    val u = url.trim()
    val re = Regex("""vimeo\.com/(?:video/)?(\d+)""")
    return re.find(u)?.groupValues?.getOrNull(1)
}

private object YouTubeExtractor {
    fun extractVideoId(urlString: String): String? {
        val uri = runCatching { android.net.Uri.parse(urlString) }.getOrNull() ?: return null
        val host = (uri.host ?: "").lowercase()
        val path = (uri.path ?: "").trim('/')

        if (host == "youtu.be") return path.takeIf { it.isNotBlank() }?.sanitize()

        if (host.contains("youtube.com") || host.contains("youtube-nocookie.com")) {
            if (path == "watch") {
                val v = uri.getQueryParameter("v")
                return v?.takeIf { it.isNotBlank() }?.sanitize()
            }
            if (path.startsWith("embed/")) {
                return path.removePrefix("embed/").takeIf { it.isNotBlank() }?.sanitize()
            }
            if (path.startsWith("shorts/")) {
                return path.removePrefix("shorts/").takeIf { it.isNotBlank() }?.sanitize()
            }
        }
        return null
    }

    private fun String.sanitize(): String = substringBefore("?").substringBefore("&")
}

private object VimeoExtractor {
    fun extract(urlString: String): String? {
        val uri = runCatching { android.net.Uri.parse(urlString) }.getOrNull() ?: return null
        val host = (uri.host ?: "").lowercase()
        val path = (uri.path ?: "").trim('/')

        if (host == "vimeo.com") return firstNumericComponent(path)

        if (host == "player.vimeo.com" && path.startsWith("video/")) {
            return firstNumericComponent(path.removePrefix("video/"))
        }
        return null
    }

    private fun firstNumericComponent(s: String): String? {
        val first = s.split("/").firstOrNull().orEmpty()
        return first.takeIf { it.all(Char::isDigit) }
    }
}

private sealed interface VideoProvider {
    data class YouTube(val id: String) : VideoProvider
    data class Vimeo(val id: String) : VideoProvider
}

private data class VideoEmbed(val provider: VideoProvider) {
    companion object {
        fun from(rawUrl: String): VideoEmbed? {
            val url = runCatching { URL(rawUrl) }.getOrNull() ?: return null
            extractYouTubeId(url)?.let { return VideoEmbed(VideoProvider.YouTube(it)) }
            extractVimeoId(url)?.let { return VideoEmbed(VideoProvider.Vimeo(it)) }
            return null
        }

        private fun extractYouTubeId(url: URL): String? {
            return YouTubeExtractor.extractVideoId(url.toString())
        }

        private fun extractVimeoId(url: URL): String? {
            return VimeoExtractor.extract(url.toString())
        }
    }
}

private fun minimalFallbackHtml(url: String): String =
    """
      <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <style>body{margin:0;padding:16px;background:black;color:white;}a{color:#9BD0FF;}</style>
        </head>
        <body><p>Open video:</p><p><a href="$url">$url</a></p></body>
      </html>
    """.trimIndent()
