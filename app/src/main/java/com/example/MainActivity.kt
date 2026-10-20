package com.example

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.FullPlayerScreen
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCard
import com.example.ui.theme.SpotifyCardElevated
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextSecondary
import com.example.ui.theme.SpotifyWhite

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HarmonixApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HarmonixApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    // Splash Screen Gate
    if (uiState.showSplash) {
        SplashScreen(
            onSplashFinished = { viewModel.dismissSplash() }
        )
        return
    }

    // Handle Android system back button properly across nested states
    BackHandler(
        enabled = uiState.isFullPlayerOpen ||
                uiState.isQueueOpen ||
                uiState.selectedPlaylist != null ||
                uiState.searchQuery.isNotEmpty()
    ) {
        when {
            uiState.isQueueOpen -> viewModel.toggleQueue(false)
            uiState.isFullPlayerOpen -> viewModel.setFullPlayerOpen(false)
            uiState.selectedPlaylist != null -> viewModel.closePlaylist()
            uiState.searchQuery.isNotEmpty() -> viewModel.updateSearchQuery("")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = SpotifyBlack,
            bottomBar = {
                Column {
                    // Mini Player (rendered above BottomNavigation)
                    if (playbackState.currentTrack != null && !uiState.isFullPlayerOpen) {
                        MiniPlayer(
                            track = playbackState.currentTrack!!,
                            isPlaying = playbackState.isPlaying,
                            currentPositionMs = playbackState.currentPositionMs,
                            durationMs = playbackState.durationMs,
                            onExpandClick = { viewModel.setFullPlayerOpen(true) },
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onToggleLike = { viewModel.toggleLikeTrack(playbackState.currentTrack!!.id) }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = SpotifyBlack.copy(alpha = 0.98f),
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = uiState.selectedTab == NavigationTab.HOME && uiState.selectedPlaylist == null,
                            onClick = { viewModel.selectTab(NavigationTab.HOME) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.selectedTab == NavigationTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SpotifyWhite,
                                selectedTextColor = SpotifyWhite,
                                unselectedIconColor = SpotifyTextMuted,
                                unselectedTextColor = SpotifyTextMuted,
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_item_home")
                        )

                        NavigationBarItem(
                            selected = uiState.selectedTab == NavigationTab.SEARCH && uiState.selectedPlaylist == null,
                            onClick = { viewModel.selectTab(NavigationTab.SEARCH) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.selectedTab == NavigationTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = "Search"
                                )
                            },
                            label = { Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SpotifyWhite,
                                selectedTextColor = SpotifyWhite,
                                unselectedIconColor = SpotifyTextMuted,
                                unselectedTextColor = SpotifyTextMuted,
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_item_search")
                        )

                        NavigationBarItem(
                            selected = uiState.selectedTab == NavigationTab.LIBRARY && uiState.selectedPlaylist == null,
                            onClick = { viewModel.selectTab(NavigationTab.LIBRARY) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.selectedTab == NavigationTab.LIBRARY) Icons.Filled.Bookmarks else Icons.Outlined.Bookmarks,
                                    contentDescription = "Your Library"
                                )
                            },
                            label = { Text("Your Library", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SpotifyWhite,
                                selectedTextColor = SpotifyWhite,
                                unselectedIconColor = SpotifyTextMuted,
                                unselectedTextColor = SpotifyTextMuted,
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_item_library")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (uiState.selectedPlaylist != null) {
                    PlaylistDetailScreen(
                        playlist = uiState.selectedPlaylist!!,
                        allTracks = tracks,
                        playbackState = playbackState,
                        onBackClick = { viewModel.closePlaylist() },
                        onPlayPlaylistClick = { viewModel.playPlaylist(uiState.selectedPlaylist!!) },
                        onShufflePlaylistClick = { viewModel.shufflePlaylist(uiState.selectedPlaylist!!) },
                        onTrackClick = { track, index ->
                            viewModel.playPlaylist(uiState.selectedPlaylist!!, index)
                        },
                        onToggleLikeTrack = { viewModel.toggleLikeTrack(it) },
                        onAddToPlaylist = { viewModel.setTrackToAddToPlaylist(it) },
                        onAddToQueue = { viewModel.audioManager.addToQueue(it) }
                    )
                } else {
                    when (uiState.selectedTab) {
                        NavigationTab.HOME -> {
                            val homeTracks = if (uiState.globalHits.isNotEmpty()) uiState.globalHits else tracks
                            HomeScreen(
                                greeting = viewModel.getGreeting(),
                                userInitial = viewModel.getUserInitial(),
                                selectedFilterChip = uiState.selectedFilterChip,
                                playlists = playlists,
                                tracks = homeTracks,
                                onSelectFilterChip = { viewModel.selectFilterChip(it) },
                                onPlaylistClick = { viewModel.openPlaylist(it) },
                                onQuickPlayPlaylist = { viewModel.playPlaylist(it) },
                                onTrackClick = { viewModel.playTrack(it) }
                            )
                        }

                        NavigationTab.SEARCH -> {
                            SearchScreen(
                                searchQuery = uiState.searchQuery,
                                searchResults = uiState.searchResults,
                                isSearching = uiState.isSearching,
                                categories = viewModel.categories,
                                playbackState = playbackState,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onTrackClick = { viewModel.playTrack(it) },
                                onToggleLike = { viewModel.toggleLikeTrack(it) },
                                onAddToPlaylist = { viewModel.setTrackToAddToPlaylist(it) },
                                onAddToQueue = { viewModel.audioManager.addToQueue(it) },
                                onCategoryClick = { cat ->
                                    viewModel.updateSearchQuery(cat.title)
                                }
                            )
                        }

                        NavigationTab.LIBRARY -> {
                            LibraryScreen(
                                playlists = playlists,
                                isGridView = uiState.libraryViewIsGrid,
                                userInitial = viewModel.getUserInitial(),
                                onPlaylistClick = { viewModel.openPlaylist(it) },
                                onCreatePlaylistClick = { viewModel.setCreatePlaylistDialogOpen(true) },
                                onImportPlaylistClick = { viewModel.setImportPlaylistDialogOpen(true) },
                                onToggleViewMode = { viewModel.toggleLibraryViewMode() }
                            )
                        }
                    }
                }
            }
        }

        // Animated Full Screen Player Overlay
        AnimatedVisibility(
            visible = uiState.isFullPlayerOpen && playbackState.currentTrack != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            FullPlayerScreen(
                playbackState = playbackState,
                isQueueOpen = uiState.isQueueOpen,
                isLyricsExpanded = uiState.isLyricsExpanded,
                onCollapseClick = { viewModel.setFullPlayerOpen(false) },
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onNext = { viewModel.skipNext() },
                onPrevious = { viewModel.skipPrevious() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onToggleLike = { viewModel.toggleLikeTrack(it) },
                onToggleQueue = { viewModel.toggleQueue() },
                onToggleLyrics = { viewModel.toggleLyrics() },
                onSelectQueueTrack = { viewModel.playTrack(it) }
            )
        }

        // First-Time User Name Prompt Dialog
        if (uiState.isNamePromptVisible && !uiState.showSplash) {
            NameOnboardingDialog(
                onConfirm = { name -> viewModel.saveUserName(name) }
            )
        }

        // Create Playlist Dialog
        if (uiState.isCreatePlaylistDialogOpen) {
            CreatePlaylistDialog(
                onDismiss = { viewModel.setCreatePlaylistDialogOpen(false) },
                onCreate = { name -> viewModel.createPlaylist(name) }
            )
        }

        // Import Playlist from Link Dialog
        if (uiState.isImportPlaylistDialogOpen) {
            ImportPlaylistDialog(
                isImporting = uiState.isImporting,
                errorMessage = uiState.importErrorMessage,
                onDismiss = { viewModel.setImportPlaylistDialogOpen(false) },
                onImport = { url -> viewModel.importPlaylistFromUrl(url) }
            )
        }

        // Add to Playlist Dialog
        if (uiState.trackToAddToPlaylist != null) {
            AddToPlaylistDialog(
                track = uiState.trackToAddToPlaylist!!,
                playlists = playlists.filter { it.isCustom },
                onDismiss = { viewModel.setTrackToAddToPlaylist(null) },
                onSelectPlaylist = { pl ->
                    viewModel.addTrackToPlaylist(pl.id, uiState.trackToAddToPlaylist!!.id)
                },
                onCreateNewPlaylist = {
                    viewModel.setCreatePlaylistDialogOpen(true)
                }
            )
        }
    }
}

@Composable
fun NameOnboardingDialog(
    onConfirm: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { /* Require input on first run */ },
        containerColor = SpotifyCardElevated,
        icon = {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        },
        title = {
            Text(
                text = "Welcome to Harmonix",
                color = SpotifyWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter your name to personalize your greetings and recommendations.",
                    color = SpotifyTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = { Text("Your name (e.g. Harsh)", color = SpotifyTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SpotifyWhite,
                        unfocusedTextColor = SpotifyWhite,
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = SpotifyTextMuted,
                        cursorColor = SpotifyGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nameInput.ifBlank { "Harsh" }) },
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_name_confirm")
            ) {
                Text(
                    text = "Get Started",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    )
}

@Composable
fun ImportPlaylistDialog(
    isImporting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var urlText by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        containerColor = SpotifyCardElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Import Playlist from Link",
                    color = SpotifyWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Paste any Spotify, YouTube, or web music playlist link. All tracks will be automatically extracted and added to a new playlist in your library.",
                    color = SpotifyTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    placeholder = {
                        Text(
                            text = "https://open.spotify.com/playlist/...",
                            color = SpotifyTextMuted,
                            fontSize = 12.sp
                        )
                    },
                    trailingIcon = {
                        Row {
                            if (urlText.isNotEmpty()) {
                                IconButton(onClick = { urlText = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = SpotifyWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!clip.isNullOrBlank()) {
                                        urlText = clip.trim()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste from clipboard",
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SpotifyWhite,
                        unfocusedTextColor = SpotifyWhite,
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = SpotifyTextMuted,
                        cursorColor = SpotifyGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_link_input")
                )

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quick Sample Links:",
                    color = SpotifyTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            urlText = "https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M"
                        },
                        label = { Text("Top Hits", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SpotifyCard,
                            labelColor = SpotifyWhite
                        )
                    )
                    SuggestionChip(
                        onClick = {
                            urlText = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                        },
                        label = { Text("YouTube MV", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SpotifyCard,
                            labelColor = SpotifyWhite
                        )
                    )
                    SuggestionChip(
                        onClick = {
                            urlText = "https://open.spotify.com/playlist/37i9dQZF1DXdLEN7aqioXM"
                        },
                        label = { Text("Chill Lofi", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SpotifyCard,
                            labelColor = SpotifyWhite
                        )
                    )
                }

                if (isImporting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = SpotifyGreen,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Extracting tracks from link...",
                            color = SpotifyGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(urlText) },
                enabled = !isImporting && urlText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpotifyGreen,
                    disabledContainerColor = SpotifyGreen.copy(alpha = 0.4f)
                ),
                modifier = Modifier.testTag("import_link_confirm")
            ) {
                Text("Extract & Add", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isImporting,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpotifyWhite)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpotifyCardElevated,
        title = {
            Text(
                text = "Give your playlist a name",
                color = SpotifyWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("My playlist #1", color = SpotifyTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SpotifyWhite,
                    unfocusedTextColor = SpotifyWhite,
                    focusedBorderColor = SpotifyGreen,
                    unfocusedBorderColor = SpotifyTextMuted,
                    cursorColor = SpotifyGreen
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_playlist_input")
            )
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name) },
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                modifier = Modifier.testTag("create_playlist_confirm")
            ) {
                Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpotifyWhite)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    track: Track,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpotifyCardElevated,
        title = {
            Text(
                text = "Add to playlist",
                color = SpotifyWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${track.title} • ${track.artist}",
                    color = SpotifyTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (playlists.isEmpty()) {
                    Text(
                        text = "You haven't created any custom playlists yet.",
                        color = SpotifyTextMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(playlists) { pl ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onSelectPlaylist(pl) },
                                colors = CardDefaults.cardColors(containerColor = SpotifyCard)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pl.name,
                                        color = SpotifyWhite,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onCreateNewPlaylist()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
            ) {
                Text("New playlist", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpotifyWhite)
            ) {
                Text("Done")
            }
        }
    )
}
