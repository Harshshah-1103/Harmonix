package com.example

import com.example.data.MockMusicRepository
import com.example.model.Track
import com.example.ui.components.formatDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testFormatDuration() {
        assertEquals("0:00", formatDuration(0L))
        assertEquals("1:05", formatDuration(65000L))
        assertEquals("3:34", formatDuration(214000L))
    }

    @Test
    fun testRepositoryInitialTracks() {
        val repo = MockMusicRepository()
        val tracks = repo.getAllTracks()
        assertTrue("Tracks should not be empty", tracks.isNotEmpty())

        val firstTrack = repo.getTrackById("track_1")
        assertNotNull("Track 1 should exist", firstTrack)
        assertEquals("Neon Drift Horizon", firstTrack?.title)
    }

    @Test
    fun testToggleLikeTrack() {
        val repo = MockMusicRepository()
        val track3 = repo.getTrackById("track_3")
        assertNotNull(track3)
        val initialLiked = track3!!.isLiked

        val newLiked = repo.toggleLikeTrack("track_3")
        assertEquals(!initialLiked, newLiked)

        val updatedTrack = repo.getTrackById("track_3")
        assertEquals(newLiked, updatedTrack?.isLiked)
    }

    @Test
    fun testCreateCustomPlaylist() {
        val repo = MockMusicRepository()
        val playlist = repo.createCustomPlaylist("My Summer Vibes", "Best tracks for road trips")
        assertEquals("My Summer Vibes", playlist.name)
        assertTrue(playlist.isCustom)

        repo.addTrackToPlaylist(playlist.id, "track_1")
        val customPlaylists = repo.customPlaylists.value
        val found = customPlaylists.find { it.id == playlist.id }
        assertNotNull(found)
        assertTrue(found!!.trackIds.contains("track_1"))
    }

    @Test
    fun testTrackWithCoverUrl() {
        val onlineTrack = Track(
            id = "test_online_1",
            title = "Cruel Summer",
            artist = "Taylor Swift",
            album = "Lover",
            durationMs = 178000L,
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/61/e7/3f/61e73f94-018d-5f50-50ec-8521952bc72e/cover.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/preview.m4a"
        )
        assertNotNull(onlineTrack.coverUrl)
        assertEquals("Taylor Swift", onlineTrack.artist)
    }

    @Test
    fun testRegisterTracks() {
        val repo = MockMusicRepository()
        val newTrack = Track(
            id = "test_custom_99",
            title = "Starboy",
            artist = "The Weeknd",
            album = "Starboy",
            durationMs = 230000L,
            audioUrl = "https://test.url/song.mp3"
        )
        repo.registerTracks(listOf(newTrack))
        val fetched = repo.getTrackById("test_custom_99")
        assertNotNull(fetched)
        assertEquals("Starboy", fetched?.title)
    }

    @Test
    fun testAllTracksHaveLyrics() {
        val repo = MockMusicRepository()
        val all = repo.getAllTracks()
        for (track in all) {
            assertTrue("Track ${track.title} should have lyrics", track.lyrics.isNotEmpty())
        }
    }
}
