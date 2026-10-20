package com.example.data

import com.example.R
import com.example.data.api.MusicApiService
import com.example.data.api.PlaylistImporter
import com.example.model.Category
import com.example.model.LyricsLine
import com.example.model.Playlist
import com.example.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockMusicRepository {
    val apiService = MusicApiService()
    val playlistImporter = PlaylistImporter(apiService)

    private val initialLocalTracks: List<Track> = listOf(
        Track(
            id = "track_1",
            title = "Neon Drift Horizon",
            artist = "Lazerhawk Echo",
            album = "Synthwave Rising",
            durationMs = 214000L,
            coverResId = R.drawable.cover_synthwave,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            gradientColor = 0xFF5D1049,
            genre = "Electronic",
            lyrics = listOf(
                LyricsLine(0L, "Night falls on the chrome horizon"),
                LyricsLine(12000L, "Driving fast through the neon grid"),
                LyricsLine(25000L, "Headlights cut through the purple haze"),
                LyricsLine(38000L, "Signals flash like memories"),
                LyricsLine(52000L, "We ride until the morning comes"),
                LyricsLine(68000L, "Underneath the cyber sky"),
                LyricsLine(84000L, "Electric dreams that never fade"),
                LyricsLine(102000L, "Synthesizer in the dark"),
                LyricsLine(125000L, "Speeding into the tomorrow"),
                LyricsLine(150000L, "Echoes calling through the stars")
            )
        ),
        Track(
            id = "track_2",
            title = "Rainy Bedroom Study",
            artist = "Komorebi Tape",
            album = "Cozy Rainy Beats",
            durationMs = 188000L,
            coverResId = R.drawable.cover_lofi,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            gradientColor = 0xFF7A4E2D,
            genre = "Chill",
            lyrics = listOf(
                LyricsLine(0L, "Drops of rain against the window pane"),
                LyricsLine(15000L, "Warm coffee steam in the cool air"),
                LyricsLine(30000L, "Pages turning softly in the quiet"),
                LyricsLine(48000L, "The cat sleeps peaceful on the desk"),
                LyricsLine(65000L, "Lo-fi chords repeating gentle rhythm"),
                LyricsLine(85000L, "Lost inside a world of calm thoughts"),
                LyricsLine(110000L, "Watching raindrops race down glass"),
                LyricsLine(135000L, "Midnight whispers and ambient calm")
            )
        ),
        Track(
            id = "track_3",
            title = "Holographic Crystal",
            artist = "Vortex 99",
            album = "Neon Aphelion",
            durationMs = 245000L,
            coverResId = R.drawable.cover_electronic,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            gradientColor = 0xFF005A60,
            genre = "Electronic",
            lyrics = listOf(
                LyricsLine(0L, "Pulse begins deep in the core"),
                LyricsLine(14000L, "Frequencies aligning with the light"),
                LyricsLine(28000L, "Laser beams refracting in the glass"),
                LyricsLine(45000L, "Feel the bass through the floorboards"),
                LyricsLine(62000L, "Crystal prism in the stratosphere"),
                LyricsLine(80000L, "Surge of kinetic hyper-energy")
            )
        ),
        Track(
            id = "track_4",
            title = "Echoes of the Vale",
            artist = "Wildflower Lyre",
            album = "Wildflower Lyre",
            durationMs = 205000L,
            coverResId = R.drawable.cover_acoustic,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            gradientColor = 0xFF8A5A20,
            genre = "Indie",
            lyrics = listOf(
                LyricsLine(0L, "Walking down the path of pine needles"),
                LyricsLine(16000L, "Sunset paints the peaks in gold and amber"),
                LyricsLine(32000L, "Acoustic strings vibrate in evening breeze")
            )
        ),
        Track(
            id = "track_5",
            title = "Midnight Espresso",
            artist = "Café Astrale",
            album = "Velvet City Nights",
            durationMs = 195000L,
            coverResId = R.drawable.cover_lofi,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            gradientColor = 0xFF3D2314,
            genre = "Chill",
            lyrics = listOf(
                LyricsLine(0L, "Corner bistro, streetlights on the asphalt"),
                LyricsLine(14000L, "Jazz brass murmur in the background"),
                LyricsLine(30000L, "Two sips of warm black espresso"),
                LyricsLine(48000L, "Watching raindrops race across the glass"),
                LyricsLine(68000L, "City that sleeps but never stops humming"),
                LyricsLine(90000L, "Lost in peaceful midnight thoughts"),
                LyricsLine(115000L, "Harmonix rhythm carries us through the night")
            )
        ),
        Track(
            id = "track_6",
            title = "Starlight Velocity",
            artist = "Cosmo Pulse",
            album = "Galaxy Odyssey",
            durationMs = 230000L,
            coverResId = R.drawable.cover_synthwave,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
            gradientColor = 0xFF1D2F6F,
            genre = "Electronic",
            lyrics = listOf(
                LyricsLine(0L, "Engines warming in the orbital bay"),
                LyricsLine(18000L, "Trajectory locked to the outer rim"),
                LyricsLine(36000L, "Hyperspace distortion shimmer"),
                LyricsLine(58000L, "Gliding through asteroid rings"),
                LyricsLine(82000L, "Neon constellations burning bright"),
                LyricsLine(110000L, "Accelerating into the boundless galaxy"),
                LyricsLine(140000L, "Cosmic pulse in perfect harmony")
            )
        )
    )

    // Dynamic registry storing all discovered & loaded tracks
    private val _dynamicTracks = MutableStateFlow<Map<String, Track>>(
        initialLocalTracks.associateBy { it.id }
    )
    val dynamicTracks: StateFlow<Map<String, Track>> = _dynamicTracks.asStateFlow()

    private val _likedTrackIds = MutableStateFlow<Set<String>>(setOf("track_1", "track_2"))
    val likedTrackIds: StateFlow<Set<String>> = _likedTrackIds.asStateFlow()

    private val _customPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val customPlaylists: StateFlow<List<Playlist>> = _customPlaylists.asStateFlow()

    fun getAllTracks(): List<Track> {
        val liked = _likedTrackIds.value
        return _dynamicTracks.value.values.map { it.copy(isLiked = liked.contains(it.id)) }
    }

    fun getTrackById(id: String): Track? {
        val liked = _likedTrackIds.value
        return _dynamicTracks.value[id]?.let { it.copy(isLiked = liked.contains(it.id)) }
    }

    fun registerTracks(newTracks: List<Track>) {
        if (newTracks.isEmpty()) return
        _dynamicTracks.update { current ->
            current + newTracks.associateBy { it.id }
        }
    }

    suspend fun searchWorldwide(query: String): List<Track> {
        val online = apiService.searchWorldwide(query)
        if (online.isNotEmpty()) {
            registerTracks(online)
            val liked = _likedTrackIds.value
            return online.map { it.copy(isLiked = liked.contains(it.id)) }
        }
        // Fallback to local match if offline or empty
        val q = query.trim().lowercase()
        return getAllTracks().filter {
            it.title.lowercase().contains(q) ||
            it.artist.lowercase().contains(q) ||
            it.album.lowercase().contains(q) ||
            it.genre.lowercase().contains(q)
        }
    }

    suspend fun loadGlobalHits(): List<Track> {
        val global = apiService.fetchTopGlobalHits()
        if (global.isNotEmpty()) {
            registerTracks(global)
            val liked = _likedTrackIds.value
            return global.map { it.copy(isLiked = liked.contains(it.id)) }
        }
        return getAllTracks()
    }

    suspend fun importPlaylistFromUrl(url: String): Playlist {
        val result = playlistImporter.importFromUrl(url)
        registerTracks(result.tracks)
        _customPlaylists.update { it + result.playlist }
        return result.playlist
    }

    fun toggleLikeTrack(trackId: String): Boolean {
        var isNowLiked = false
        _likedTrackIds.update { set ->
            if (set.contains(trackId)) {
                isNowLiked = false
                set - trackId
            } else {
                isNowLiked = true
                set + trackId
            }
        }
        return isNowLiked
    }

    fun getInitialPlaylists(): List<Playlist> {
        return listOf(
            Playlist(
                id = "pl_daily_mix_1",
                name = "Daily Mix 1",
                description = "Lazerhawk Echo, Vortex 99, Cosmo Pulse and more favorites.",
                coverResId = R.drawable.cover_synthwave,
                trackIds = listOf("track_1", "track_3", "track_6"),
                colorHex = 0xFF5D1049,
                creator = "Made for You"
            ),
            Playlist(
                id = "pl_lofi_study",
                name = "Chill Lo-Fi Study",
                description = "Soft rainy beats to relax, study, and unwind to.",
                coverResId = R.drawable.cover_lofi,
                trackIds = listOf("track_2", "track_5"),
                colorHex = 0xFF7A4E2D,
                creator = "Spotify Curated"
            ),
            Playlist(
                id = "pl_acoustic_afternoon",
                name = "Acoustic Afternoons",
                description = "Warm acoustic guitar, indie folk, and golden hour melodies.",
                coverResId = R.drawable.cover_acoustic,
                trackIds = listOf("track_4", "track_2"),
                colorHex = 0xFF8A5A20,
                creator = "Spotify Curated"
            ),
            Playlist(
                id = "pl_electronic_energy",
                name = "Future Cyber Club",
                description = "High octane electronic beats and futuristic dance tracks.",
                coverResId = R.drawable.cover_electronic,
                trackIds = listOf("track_3", "track_1", "track_6"),
                colorHex = 0xFF005A60,
                creator = "Spotify Curated"
            )
        )
    }

    fun getLikedSongsPlaylist(): Playlist {
        val likedIds = _likedTrackIds.value
        return Playlist(
            id = "pl_liked_songs",
            name = "Liked Songs",
            description = "${likedIds.size} songs you love",
            coverResId = R.drawable.cover_lofi,
            trackIds = likedIds.toList(),
            colorHex = 0xFF431F7A,
            creator = "You"
        )
    }

    fun createCustomPlaylist(name: String, description: String = ""): Playlist {
        val newPlaylist = Playlist(
            id = "custom_${System.currentTimeMillis()}",
            name = name.ifBlank { "My Playlist #${_customPlaylists.value.size + 1}" },
            description = description.ifBlank { "Playlist created by you" },
            coverResId = R.drawable.cover_electronic,
            trackIds = emptyList(),
            isCustom = true,
            colorHex = 0xFF1DB954,
            creator = "You"
        )
        _customPlaylists.update { it + newPlaylist }
        return newPlaylist
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        _customPlaylists.update { list ->
            list.map { pl ->
                if (pl.id == playlistId && !pl.trackIds.contains(trackId)) {
                    pl.copy(trackIds = pl.trackIds + trackId)
                } else pl
            }
        }
    }

    fun getCategories(): List<Category> {
        return listOf(
            Category("cat_pop", "Pop", 0xFF8D67AB, "music"),
            Category("cat_hiphop", "Hip-Hop", 0xFFBA5D07, "fire"),
            Category("cat_electronic", "Electronic", 0xFF1E3264, "headset"),
            Category("cat_chill", "Chill", 0xFFD84000, "spa"),
            Category("cat_indie", "Indie", 0xFF608108, "guitar"),
            Category("cat_rock", "Rock", 0xFFE91429, "bolt"),
            Category("cat_workout", "Workout", 0xFF777777, "fitness"),
            Category("cat_focus", "Focus", 0xFF503750, "lightbulb"),
            Category("cat_podcasts", "Podcasts", 0xFF006450, "mic"),
            Category("cat_jazz", "Jazz", 0xFFB02897, "piano")
        )
    }
}
