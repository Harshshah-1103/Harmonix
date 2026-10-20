package com.example.ui.screens

import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackState
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.components.TrackListItem
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextSecondary
import com.example.ui.theme.SpotifyWhite

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    allTracks: List<Track>,
    playbackState: PlaybackState,
    onBackClick: () -> Unit,
    onPlayPlaylistClick: () -> Unit,
    onShufflePlaylistClick: () -> Unit,
    onTrackClick: (Track, Int) -> Unit,
    onToggleLikeTrack: (String) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLikedSongs = playlist.id == "pl_liked_songs"
    val baseColor = Color(playlist.colorHex)
    val playlistTracks = playlist.trackIds.mapNotNull { id -> allTracks.find { it.id == id } }

    val isPlaylistPlaying = playbackState.isPlaying && playlistTracks.any { it.id == playbackState.currentTrack?.id }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.8f),
                        baseColor.copy(alpha = 0.35f),
                        SpotifyBlack,
                        SpotifyBlack
                    )
                )
            )
            .statusBarsPadding()
            .testTag("playlist_detail_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Top Navigation Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("playlist_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SpotifyWhite
                        )
                    }

                    IconButton(onClick = { /* Menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = SpotifyWhite
                        )
                    }
                }
            }

            // Playlist Artwork Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLikedSongs) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF450AF5), Color(0xFFC4EFD9))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = SpotifyWhite,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    } else if (!playlist.coverUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = playlist.coverUrl,
                            contentDescription = playlist.name,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (playlist.coverResId != null) {
                        Image(
                            painter = painterResource(id = playlist.coverResId),
                            contentDescription = playlist.name,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(baseColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = playlist.name.take(1),
                                color = SpotifyWhite,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = playlist.name,
                        color = SpotifyWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = playlist.description,
                        color = SpotifyTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Made by ${playlist.creator} • ${playlistTracks.size} songs",
                        color = SpotifyWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Actions Row: Download, Shuffle, Large Play FAB
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = "Downloaded",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        IconButton(onClick = onShufflePlaylistClick) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle playlist",
                                tint = if (playbackState.isShuffle) SpotifyGreen else SpotifyWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = onPlayPlaylistClick,
                        shape = CircleShape,
                        color = SpotifyGreen,
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("playlist_play_fab"),
                        shadowElevation = 6.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isPlaylistPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaylistPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Track List Items
            if (playlistTracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "This playlist is empty.\nExplore and add songs from Search or Home!",
                            color = SpotifyTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                itemsIndexed(playlistTracks) { index, track ->
                    val isCurrent = playbackState.currentTrack?.id == track.id
                    TrackListItem(
                        track = track,
                        isPlaying = playbackState.isPlaying,
                        isCurrentTrack = isCurrent,
                        index = index + 1,
                        showCover = true,
                        onTrackClick = { onTrackClick(track, index) },
                        onToggleLike = { onToggleLikeTrack(track.id) },
                        onAddToPlaylist = { onAddToPlaylist(track) },
                        onAddToQueue = { onAddToQueue(track) }
                    )
                }
            }
        }
    }
}
