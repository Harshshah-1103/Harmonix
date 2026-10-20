package com.example.model

enum class RepeatMode {
    OFF, ALL, ONE
}

data class LyricsLine(
    val timestampMs: Long,
    val text: String
)

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val coverResId: Int? = null,
    val coverUrl: String? = null,
    val audioUrl: String,
    val gradientColor: Long = 0xFF8D67AB,
    val lyrics: List<LyricsLine> = emptyList(),
    val genre: String = "Pop",
    val isLiked: Boolean = false
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val coverResId: Int? = null,
    val coverUrl: String? = null,
    val trackIds: List<String> = emptyList(),
    val isCustom: Boolean = false,
    val colorHex: Long = 0xFF1DB954,
    val creator: String = "Spotify"
)

data class Category(
    val id: String,
    val title: String,
    val colorHex: Long,
    val iconKey: String = "music"
)

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Track> = emptyList(),
    val isBuffering: Boolean = false
)
