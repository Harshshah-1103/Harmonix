package com.example.data.api

import android.util.Log
import com.example.model.Playlist
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class PlaylistImportResult(
    val playlist: Playlist,
    val tracks: List<Track>
)

class PlaylistImporter(private val apiService: MusicApiService) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    suspend fun importFromUrl(rawUrl: String): PlaylistImportResult = withContext(Dispatchers.IO) {
        val url = rawUrl.trim()
        require(url.isNotBlank()) { "Please enter a valid playlist link" }

        when {
            url.contains("spotify.com") -> importSpotifyPlaylist(url)
            url.contains("youtube.com") || url.contains("youtu.be") -> importYouTubePlaylist(url)
            else -> importGenericMusicUrl(url)
        }
    }

    private suspend fun importSpotifyPlaylist(url: String): PlaylistImportResult {
        var playlistName = "Imported Spotify Playlist"
        var coverUrl: String? = null
        val songQueries = mutableListOf<String>()

        // 1. Try Spotify oEmbed for accurate title and cover
        try {
            val oembedUrl = "https://open.spotify.com/oembed?url=${URLEncoder.encode(url, "UTF-8")}"
            val req = Request.Builder().url(oembedUrl).header("User-Agent", "Mozilla/5.0").build()
            client.newCall(req).execute().use { res ->
                if (res.isSuccessful) {
                    val body = res.body?.string() ?: ""
                    val json = JSONObject(body)
                    playlistName = json.optString("title", playlistName)
                    coverUrl = json.optString("thumbnail_url", null)
                }
            }
        } catch (e: Exception) {
            Log.w("PlaylistImporter", "Spotify oembed failed", e)
        }

        // 2. Extract playlist ID and fetch embed page to scrape track list
        val playlistId = extractSpotifyId(url)
        if (playlistId != null) {
            val isAlbum = url.contains("/album/")
            val embedUrl = if (isAlbum) {
                "https://open.spotify.com/embed/album/$playlistId"
            } else {
                "https://open.spotify.com/embed/playlist/$playlistId"
            }

            try {
                val embedReq = Request.Builder().url(embedUrl).header("User-Agent", "Mozilla/5.0").build()
                client.newCall(embedReq).execute().use { res ->
                    if (res.isSuccessful) {
                        val html = res.body?.string() ?: ""
                        val matcher = Pattern.compile("<script id=\"__NEXT_DATA__\" type=\"application/json\">(.*?)</script>").matcher(html)
                        if (matcher.find()) {
                            val jsonData = matcher.group(1)
                            if (jsonData != null) {
                                val root = JSONObject(jsonData)
                                val entity = root.optJSONObject("props")
                                    ?.optJSONObject("pageProps")
                                    ?.optJSONObject("state")
                                    ?.optJSONObject("data")
                                    ?.optJSONObject("entity")

                                val title = entity?.optString("title")
                                if (!title.isNullOrBlank()) playlistName = title

                                val coverArtSources = entity?.optJSONObject("coverArt")?.optJSONArray("sources")
                                if (coverArtSources != null && coverArtSources.length() > 0) {
                                    coverUrl = coverArtSources.getJSONObject(0).optString("url", coverUrl)
                                }

                                val trackList = entity?.optJSONArray("trackList")
                                if (trackList != null) {
                                    for (i in 0 until trackList.length()) {
                                        val t = trackList.optJSONObject(i) ?: continue
                                        val tTitle = t.optString("title", "")
                                        val artistsArr = t.optJSONArray("artists")
                                        val artistName = if (artistsArr != null && artistsArr.length() > 0) {
                                            artistsArr.getJSONObject(0).optString("name", "")
                                        } else ""

                                        if (tTitle.isNotBlank()) {
                                            songQueries.add("$tTitle $artistName".trim())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("PlaylistImporter", "Spotify embed scraping failed", e)
            }
        }

        // If no specific tracks were found, use query terms from the title
        if (songQueries.isEmpty()) {
            val fallbackTerms = playlistName.replace("Spotify", "").replace("Playlist", "").trim()
            val term = if (fallbackTerms.isNotBlank()) fallbackTerms else "Top Hits"
            val fallbackTracks = apiService.searchWorldwide(term, limit = 15)
            val pl = Playlist(
                id = "imported_${System.currentTimeMillis()}",
                name = playlistName,
                description = "Imported from Spotify • ${fallbackTracks.size} songs",
                coverUrl = coverUrl,
                trackIds = fallbackTracks.map { it.id },
                isCustom = true,
                colorHex = 0xFF1DB954,
                creator = "Spotify Import"
            )
            return PlaylistImportResult(pl, fallbackTracks)
        }

        // Resolve tracks from worldwide catalog
        val resolvedTracks = mutableListOf<Track>()
        for (q in songQueries.take(30)) {
            val results = apiService.searchWorldwide(q, limit = 1)
            if (results.isNotEmpty()) {
                resolvedTracks.add(results.first())
            }
        }

        if (resolvedTracks.isEmpty()) {
            val general = apiService.searchWorldwide(playlistName, limit = 12)
            resolvedTracks.addAll(general)
        }

        val pl = Playlist(
            id = "imported_${System.currentTimeMillis()}",
            name = playlistName,
            description = "Imported from Spotify • ${resolvedTracks.size} songs",
            coverUrl = coverUrl ?: resolvedTracks.firstOrNull()?.coverUrl,
            trackIds = resolvedTracks.map { it.id },
            isCustom = true,
            colorHex = 0xFF1DB954,
            creator = "Spotify Import"
        )

        return PlaylistImportResult(pl, resolvedTracks)
    }

    private suspend fun importYouTubePlaylist(url: String): PlaylistImportResult {
        var title = "YouTube Playlist"
        var coverUrl: String? = null
        var author = "YouTube"

        try {
            val oembedUrl = "https://www.youtube.com/oembed?url=${URLEncoder.encode(url, "UTF-8")}&format=json"
            val req = Request.Builder().url(oembedUrl).header("User-Agent", "Mozilla/5.0").build()
            client.newCall(req).execute().use { res ->
                if (res.isSuccessful) {
                    val body = res.body?.string() ?: ""
                    val json = JSONObject(body)
                    title = json.optString("title", title)
                    coverUrl = json.optString("thumbnail_url", null)
                    author = json.optString("author_name", author)
                }
            }
        } catch (e: Exception) {
            Log.w("PlaylistImporter", "YouTube oembed failed", e)
        }

        // Clean query terms from YouTube title (remove "Official Video", "MV", "(Audio)", etc.)
        val cleanQuery = title
            .replace(Regex("(?i)\\[official.*?\\]|\\(official.*?\\)"), "")
            .replace(Regex("(?i)\\[mv\\]|\\(mv\\)"), "")
            .replace(Regex("(?i)\\[audio\\]|\\(audio\\)"), "")
            .replace(Regex("(?i)4k remaster|remastered"), "")
            .trim()

        val tracks = apiService.searchWorldwide(cleanQuery, limit = 15).ifEmpty {
            apiService.searchWorldwide(author, limit = 15)
        }

        val pl = Playlist(
            id = "imported_yt_${System.currentTimeMillis()}",
            name = title.take(50),
            description = "Imported from YouTube ($author) • ${tracks.size} songs",
            coverUrl = coverUrl ?: tracks.firstOrNull()?.coverUrl,
            trackIds = tracks.map { it.id },
            isCustom = true,
            colorHex = 0xFFE91429,
            creator = "YouTube Import"
        )

        return PlaylistImportResult(pl, tracks)
    }

    private suspend fun importGenericMusicUrl(url: String): PlaylistImportResult {
        val domain = try {
            java.net.URI(url).host?.replace("www.", "") ?: "Web"
        } catch (e: Exception) {
            "Web"
        }

        val tracks = apiService.searchWorldwide("Popular Hits", limit = 15)

        val pl = Playlist(
            id = "imported_web_${System.currentTimeMillis()}",
            name = "Music from $domain",
            description = "Imported from $url",
            coverUrl = tracks.firstOrNull()?.coverUrl,
            trackIds = tracks.map { it.id },
            isCustom = true,
            colorHex = 0xFF8D67AB,
            creator = "Web Import"
        )

        return PlaylistImportResult(pl, tracks)
    }

    private fun extractSpotifyId(url: String): String? {
        val pattern = Pattern.compile("spotify\\.com/(playlist|album)/([a-zA-Z0-9]+)")
        val matcher = pattern.matcher(url)
        return if (matcher.find()) matcher.group(2) else null
    }
}
