package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackState
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardElevated
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextSecondary
import com.example.ui.theme.SpotifyWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    playbackState: PlaybackState,
    isQueueOpen: Boolean,
    isLyricsExpanded: Boolean,
    onCollapseClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleQueue: () -> Unit,
    onToggleLyrics: () -> Unit,
    onSelectQueueTrack: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playbackState.currentTrack ?: return
    val baseColor = Color(track.gradientColor)

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragSliderValue by remember { mutableFloatStateOf(0f) }

    val currentProgress = if (playbackState.durationMs > 0) {
        playbackState.currentPositionMs.toFloat() / playbackState.durationMs.toFloat()
    } else 0f

    val sliderProgress = if (isDraggingSlider) dragSliderValue else currentProgress.coerceIn(0f, 1f)

    val coverScale by animateFloatAsState(
        targetValue = if (playbackState.isPlaying) 1.0f else 0.92f,
        label = "coverScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.85f),
                        baseColor.copy(alpha = 0.45f),
                        SpotifyBlack,
                        SpotifyBlack
                    )
                )
            )
            .statusBarsPadding()
            .testTag("full_player_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapseClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("player_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse player",
                        tint = SpotifyWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM PLAYLIST",
                        color = SpotifyTextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = track.album,
                        color = SpotifyWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { /* Overflow menu */ },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = SpotifyWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center: Album Art or Synchronized Lyrics
            if (!isLyricsExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    TrackArtwork(
                        track = track,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(1f)
                            .scale(coverScale),
                        cornerRadius = 8.dp
                    )
                }
            } else {
                // Expanded synchronized lyrics view
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = baseColor.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lyrics",
                                color = SpotifyWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(onClick = onToggleLyrics) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close lyrics",
                                    tint = SpotifyWhite
                                )
                            }
                        }

                        val listState = rememberLazyListState()
                        val activeLineIndex = remember(playbackState.currentPositionMs, track.lyrics) {
                            val idx = track.lyrics.indexOfLast { it.timestampMs <= playbackState.currentPositionMs }
                            if (idx >= 0) idx else 0
                        }

                        LaunchedEffect(activeLineIndex) {
                            if (activeLineIndex >= 0 && activeLineIndex < track.lyrics.size) {
                                listState.animateScrollToItem((activeLineIndex - 2).coerceAtLeast(0))
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(track.lyrics) { index, line ->
                                val isCurrentLine = index == activeLineIndex
                                Text(
                                    text = line.text,
                                    color = if (isCurrentLine) SpotifyWhite else SpotifyWhite.copy(alpha = 0.45f),
                                    fontSize = if (isCurrentLine) 20.sp else 16.sp,
                                    fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Medium,
                                    lineHeight = 28.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSeek(line.timestampMs) }
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Track Details & Heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = SpotifyWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.artist,
                        color = SpotifyTextSecondary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { onToggleLike(track.id) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_like")
                ) {
                    Icon(
                        imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (track.isLiked) "Unlike" else "Like",
                        tint = if (track.isLiked) SpotifyGreen else SpotifyWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Seeker Slider
            Slider(
                value = sliderProgress,
                onValueChange = {
                    isDraggingSlider = true
                    dragSliderValue = it
                },
                onValueChangeFinished = {
                    isDraggingSlider = false
                    val targetMs = (dragSliderValue * playbackState.durationMs).toLong()
                    onSeek(targetMs)
                },
                colors = SliderDefaults.colors(
                    thumbColor = SpotifyWhite,
                    activeTrackColor = SpotifyWhite,
                    inactiveTrackColor = SpotifyWhite.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_seek_slider")
            )

            // Timestamps
            val currentPosMs = if (isDraggingSlider) {
                (dragSliderValue * playbackState.durationMs).toLong()
            } else {
                playbackState.currentPositionMs
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(currentPosMs),
                    color = SpotifyTextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = formatDuration(playbackState.durationMs),
                    color = SpotifyTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.size(48.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playbackState.isShuffle) SpotifyGreen else SpotifyWhite,
                            modifier = Modifier.size(24.dp)
                        )
                        if (playbackState.isShuffle) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(SpotifyGreen)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_previous_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Previous",
                        tint = SpotifyWhite,
                        modifier = Modifier
                            .size(34.dp)
                            .scale(scaleX = -1f, scaleY = 1f)
                    )
                }

                // Play / Pause FAB
                Surface(
                    onClick = onPlayPauseClick,
                    shape = CircleShape,
                    color = SpotifyWhite,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("player_play_pause_fab")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Next",
                        tint = SpotifyWhite,
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.size(48.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (playbackState.repeatMode) {
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (playbackState.repeatMode != RepeatMode.OFF) SpotifyGreen else SpotifyWhite,
                            modifier = Modifier.size(24.dp)
                        )
                        if (playbackState.repeatMode != RepeatMode.OFF) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(SpotifyGreen)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Lyrics Preview Card
            if (!isLyricsExpanded && track.lyrics.isNotEmpty()) {
                Surface(
                    onClick = onToggleLyrics,
                    shape = RoundedCornerShape(12.dp),
                    color = baseColor.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("lyrics_preview_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LYRICS",
                                color = SpotifyWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            val currentLine = track.lyrics.findLast { it.timestampMs <= playbackState.currentPositionMs }?.text
                                ?: track.lyrics.firstOrNull()?.text ?: "Lyrics available"
                            Text(
                                text = currentLine,
                                color = SpotifyWhite.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "OPEN",
                            color = SpotifyGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Actions: Speaker, Share, Lyrics toggle, Queue toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* Connect device modal */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Speaker,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Phone speaker",
                        color = SpotifyGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onToggleLyrics,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text(
                            text = "LYRICS",
                            color = if (isLyricsExpanded) SpotifyGreen else SpotifyWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onToggleQueue,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("queue_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = if (isQueueOpen) SpotifyGreen else SpotifyWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Animated Queue Drawer
        AnimatedVisibility(
            visible = isQueueOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            QueueSheet(
                currentTrack = track,
                queue = playbackState.queue,
                isPlaying = playbackState.isPlaying,
                onClose = onToggleQueue,
                onSelectTrack = onSelectQueueTrack
            )
        }
    }
}

@Composable
fun QueueSheet(
    currentTrack: Track,
    queue: List<Track>,
    isPlaying: Boolean,
    onClose: () -> Unit,
    onSelectTrack: (Track) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack.copy(alpha = 0.96f))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Queue",
                    color = SpotifyWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close queue",
                        tint = SpotifyWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Now Playing",
                color = SpotifyTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Currently playing row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SpotifyCardElevated)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrackArtwork(
                    track = currentTrack,
                    modifier = Modifier.size(44.dp),
                    cornerRadius = 4.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTrack.title,
                        color = SpotifyGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = currentTrack.artist,
                        color = SpotifyTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                EqualizerBars(isPlaying = isPlaying)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Next in Queue",
                color = SpotifyTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val upcoming = queue.filter { it.id != currentTrack.id }
            if (upcoming.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Queue is empty. Select songs to add to queue.",
                        color = SpotifyTextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(upcoming) { _, qTrack ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTrack(qTrack) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrackArtwork(
                                track = qTrack,
                                modifier = Modifier.size(42.dp),
                                cornerRadius = 4.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = qTrack.title,
                                    color = SpotifyWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = qTrack.artist,
                                    color = SpotifyTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = formatDuration(qTrack.durationMs),
                                color = SpotifyTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
