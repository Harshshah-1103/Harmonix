package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.data.MockMusicRepository
import com.example.data.UserPreferencesManager
import com.example.model.Category
import com.example.model.PlaybackState
import com.example.model.Playlist
import com.example.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class NavigationTab {
    HOME, SEARCH, LIBRARY
}

data class AppUiState(
    val showSplash: Boolean = true,
    val userName: String = "",
    val isNamePromptVisible: Boolean = false,
    val selectedTab: NavigationTab = NavigationTab.HOME,
    val selectedFilterChip: String = "All",
    val searchQuery: String = "",
    val searchResults: List<Track> = emptyList(),
    val isSearching: Boolean = false,
    val selectedPlaylist: Playlist? = null,
    val isFullPlayerOpen: Boolean = false,
    val isQueueOpen: Boolean = false,
    val isLyricsExpanded: Boolean = false,
    val isCreatePlaylistDialogOpen: Boolean = false,
    val isImportPlaylistDialogOpen: Boolean = false,
    val isImporting: Boolean = false,
    val importErrorMessage: String? = null,
    val trackToAddToPlaylist: Track? = null,
    val libraryViewIsGrid: Boolean = false,
    val globalHits: List<Track> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MockMusicRepository()
    val audioManager = AudioPlayerManager(application.applicationContext)
    val prefsManager = UserPreferencesManager(application.applicationContext)

    private val _uiState = MutableStateFlow(
        AppUiState(
            userName = prefsManager.getUserName() ?: "",
            isNamePromptVisible = !prefsManager.hasCompletedOnboarding()
        )
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = audioManager.playbackState

    val tracks: StateFlow<List<Track>> = combine(
        repository.dynamicTracks,
        repository.likedTrackIds
    ) { _, _ ->
        repository.getAllTracks()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, repository.getAllTracks())

    val playlists: StateFlow<List<Playlist>> = combine(
        repository.customPlaylists,
        repository.likedTrackIds
    ) { customs, _ ->
        listOf(repository.getLikedSongsPlaylist()) + repository.getInitialPlaylists() + customs
    }.stateIn(viewModelScope, SharingStarted.Eagerly, listOf(repository.getLikedSongsPlaylist()) + repository.getInitialPlaylists())

    val categories: List<Category> = repository.getCategories()

    private var searchJob: Job? = null

    init {
        // Load initial global trending hits
        viewModelScope.launch {
            try {
                val hits = repository.loadGlobalHits()
                _uiState.update { it.copy(globalHits = hits) }
            } catch (e: Exception) {
                // Ignore initial network issues
            }
        }
    }

    fun dismissSplash() {
        _uiState.update { it.copy(showSplash = false) }
    }

    fun saveUserName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            prefsManager.saveUserName(trimmed)
            _uiState.update { it.copy(userName = trimmed, isNamePromptVisible = false) }
        } else {
            prefsManager.saveUserName("Harsh")
            _uiState.update { it.copy(userName = "Harsh", isNamePromptVisible = false) }
        }
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
        val name = _uiState.value.userName.trim()
        return if (name.isNotBlank()) "$timeGreeting $name!" else "$timeGreeting!"
    }

    fun getUserInitial(): String {
        val name = _uiState.value.userName.trim()
        return if (name.isNotBlank()) name.take(1).uppercase() else "H"
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(selectedTab = tab, selectedPlaylist = null) }
    }

    fun selectFilterChip(chip: String) {
        _uiState.update { it.copy(selectedFilterChip = chip) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            delay(350L) // Debounce typing
            val results = repository.searchWorldwide(query)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun openPlaylist(playlist: Playlist) {
        _uiState.update { it.copy(selectedPlaylist = playlist) }
    }

    fun closePlaylist() {
        _uiState.update { it.copy(selectedPlaylist = null) }
    }

    fun setFullPlayerOpen(open: Boolean) {
        _uiState.update { it.copy(isFullPlayerOpen = open) }
    }

    fun toggleQueue(open: Boolean? = null) {
        _uiState.update { it.copy(isQueueOpen = open ?: !it.isQueueOpen) }
    }

    fun toggleLyrics(expanded: Boolean? = null) {
        _uiState.update { it.copy(isLyricsExpanded = expanded ?: !it.isLyricsExpanded) }
    }

    fun setCreatePlaylistDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isCreatePlaylistDialogOpen = open) }
    }

    fun setImportPlaylistDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isImportPlaylistDialogOpen = open, importErrorMessage = null) }
    }

    fun setTrackToAddToPlaylist(track: Track?) {
        _uiState.update { it.copy(trackToAddToPlaylist = track) }
    }

    fun toggleLibraryViewMode() {
        _uiState.update { it.copy(libraryViewIsGrid = !it.libraryViewIsGrid) }
    }

    fun playTrack(track: Track) {
        val currentQueue = _uiState.value.searchResults.ifEmpty {
            _uiState.value.globalHits.ifEmpty { repository.getAllTracks() }
        }
        val targetList = if (currentQueue.any { it.id == track.id }) currentQueue else listOf(track) + currentQueue
        val index = targetList.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        audioManager.setQueueAndPlay(targetList, index)
    }

    fun playPlaylist(playlist: Playlist, startIndex: Int = 0) {
        val all = repository.getAllTracks()
        val playlistTracks = playlist.trackIds.mapNotNull { id -> all.find { it.id == id } }
        if (playlistTracks.isNotEmpty()) {
            audioManager.setQueueAndPlay(playlistTracks, startIndex.coerceIn(0, playlistTracks.size - 1))
        }
    }

    fun shufflePlaylist(playlist: Playlist) {
        val all = repository.getAllTracks()
        val playlistTracks = playlist.trackIds.mapNotNull { id -> all.find { it.id == id } }
        if (playlistTracks.isNotEmpty()) {
            if (!playbackState.value.isShuffle) {
                audioManager.toggleShuffle()
            }
            audioManager.setQueueAndPlay(playlistTracks.shuffled(), 0)
        }
    }

    fun togglePlayPause() = audioManager.togglePlayPause()

    fun seekTo(positionMs: Long) = audioManager.seekTo(positionMs)

    fun skipNext() = audioManager.skipNext()

    fun skipPrevious() = audioManager.skipPrevious()

    fun toggleShuffle() = audioManager.toggleShuffle()

    fun toggleRepeat() = audioManager.toggleRepeat()

    fun toggleLikeTrack(trackId: String) {
        repository.toggleLikeTrack(trackId)
    }

    fun createPlaylist(name: String) {
        val newPl = repository.createCustomPlaylist(name)
        _uiState.update { it.copy(isCreatePlaylistDialogOpen = false, selectedPlaylist = newPl) }
    }

    fun importPlaylistFromUrl(url: String) {
        if (url.isBlank()) {
            _uiState.update { it.copy(importErrorMessage = "Please enter or paste a valid link") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importErrorMessage = null) }
            try {
                val importedPlaylist = repository.importPlaylistFromUrl(url)
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        isImportPlaylistDialogOpen = false,
                        selectedPlaylist = importedPlaylist
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importErrorMessage = "Could not extract tracks from this link. Please check the URL and try again."
                    )
                }
            }
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        repository.addTrackToPlaylist(playlistId, trackId)
        _uiState.update { it.copy(trackToAddToPlaylist = null) }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
    }
}
