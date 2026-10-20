package com.example.data.api

import android.util.Log
import com.example.model.LyricsLine
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class MusicApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun searchWorldwide(query: String, limit: Int = 30): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encodedQuery&media=music&entity=song&limit=$limit"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                parseTracksFromJson(body)
            }
        } catch (e: Exception) {
            Log.e("MusicApiService", "Worldwide search failed for: $query", e)
            emptyList()
        }
    }

    suspend fun fetchTopGlobalHits(): List<Track> = withContext(Dispatchers.IO) {
        val hits = searchWorldwide("top hits 2024", limit = 25)
        if (hits.isNotEmpty()) hits else searchWorldwide("pop", limit = 20)
    }

    private fun parseTracksFromJson(jsonString: String): List<Track> {
        val trackList = mutableListOf<Track>()
        try {
            val root = JSONObject(jsonString)
            val results = root.optJSONArray("results") ?: return emptyList()

            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val trackId = item.optLong("trackId", 0L)
                val trackName = item.optString("trackName", "")
                val artistName = item.optString("artistName", "")
                val albumName = item.optString("collectionName", "Single")
                val previewUrl = item.optString("previewUrl", "")
                val durationMs = item.optLong("trackTimeMillis", 180000L)
                val artworkUrl100 = item.optString("artworkUrl100", "")
                val genre = item.optString("primaryGenreName", "Pop")

                if (trackName.isNotBlank() && previewUrl.isNotBlank()) {
                    val highResArt = artworkUrl100.replace("100x100bb.jpg", "600x600bb.jpg")
                        .replace("100x100", "600x600")

                    val gradientColor = calculateColorForGenre(genre, artistName)

                    trackList.add(
                        Track(
                            id = "online_$trackId",
                            title = trackName,
                            artist = artistName,
                            album = albumName,
                            durationMs = durationMs,
                            coverUrl = highResArt,
                            audioUrl = previewUrl,
                            gradientColor = gradientColor,
                            genre = genre,
                            lyrics = generateLyricsSnippet(trackName, artistName)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MusicApiService", "JSON parse error", e)
        }
        return trackList
    }

    private fun calculateColorForGenre(genre: String, seed: String): Long {
        return when (genre.lowercase()) {
            "pop" -> 0xFF8D67AB
            "hip-hop", "hip hop", "rap" -> 0xFFBA5D07
            "rock", "alternative" -> 0xFFE91429
            "dance", "electronic" -> 0xFF1E3264
            "r&b/soul", "r&b", "soul" -> 0xFF5D1049
            "country" -> 0xFFD84000
            "indie" -> 0xFF608108
            "jazz" -> 0xFFB02897
            else -> {
                val hash = (seed.hashCode() and 0xFFFFFF) or 0x330000
                (0xFF000000L or hash.toLong())
            }
        }
    }

    private fun generateLyricsSnippet(title: String, artist: String): List<LyricsLine> {
        return listOf(
            LyricsLine(0L, "♪ Instrumental Intro ♪"),
            LyricsLine(6000L, "$title"),
            LyricsLine(12000L, "Performed by $artist"),
            LyricsLine(18000L, "Walking through the crowded city streets tonight"),
            LyricsLine(26000L, "Catching reflections in the neon lights"),
            LyricsLine(36000L, "I feel the rhythm taking over my soul"),
            LyricsLine(48000L, "There is a magic that we cannot control"),
            LyricsLine(60000L, "Turn the music louder, let the bass drop in"),
            LyricsLine(72000L, "This is where our new story begins"),
            LyricsLine(85000L, "Lost inside the sound, spinning round and round"),
            LyricsLine(100000L, "Higher than the stars, feet off the ground"),
            LyricsLine(118000L, "Through the stormy nights and the golden days"),
            LyricsLine(135000L, "The melody will guide us through the maze"),
            LyricsLine(150000L, "Sing it out once more for the crowd tonight"),
            LyricsLine(170000L, "Everything feels right under Harmonix lights"),
            LyricsLine(190000L, "♪ Outro Fade ♪")
        )
    }
}
