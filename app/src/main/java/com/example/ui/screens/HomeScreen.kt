package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.theme.SpotifyCard
import com.example.ui.theme.SpotifyCardElevated
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextSecondary
import com.example.ui.theme.SpotifyWhite
import java.util.Calendar

@Composable
fun HomeScreen(
    greeting: String,
    userInitial: String = "U",
    selectedFilterChip: String,
    playlists: List<Playlist>,
    tracks: List<Track>,
    onSelectFilterChip: (String) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onQuickPlayPlaylist: (Playlist) -> Unit,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitial.uppercase(),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = greeting,
                        color = SpotifyWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = SpotifyWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { /* History */ }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Listening history",
                            tint = SpotifyWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SpotifyWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Filter chips: All, Music, Podcasts
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Music", "Podcasts").forEach { chip ->
                    val isSelected = chip == selectedFilterChip
                    AssistChip(
                        onClick = { onSelectFilterChip(chip) },
                        label = {
                            Text(
                                text = chip,
                                color = if (isSelected) Color.Black else SpotifyWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) SpotifyGreen else SpotifyCardElevated
                        ),
                        border = null,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("filter_chip_$chip")
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Quick Play 6-pack Grid
        item {
            val quickItems = playlists.take(6)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in quickItems.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (playlist in row) {
                            QuickPlayCard(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist) },
                                onPlayClick = { onQuickPlayPlaylist(playlist) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recently Played
        item {
            SectionHeader(title = "Recently played")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(playlists) { playlist ->
                    MediaCard(
                        title = playlist.name,
                        subtitle = playlist.description,
                        coverResId = playlist.coverResId,
                        coverUrl = playlist.coverUrl,
                        onClick = { onPlaylistClick(playlist) },
                        onPlayClick = { onQuickPlayPlaylist(playlist) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Made For You / Global Trending Tracks
        item {
            SectionHeader(title = "Made For You • Global Hits")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tracks) { track ->
                    MediaCard(
                        title = track.title,
                        subtitle = track.artist,
                        coverResId = track.coverResId,
                        coverUrl = track.coverUrl,
                        onClick = { onTrackClick(track) },
                        onPlayClick = { onTrackClick(track) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Popular Albums & Compilations
        item {
            SectionHeader(title = "Popular and Trending")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(playlists.reversed()) { playlist ->
                    MediaCard(
                        title = playlist.name,
                        subtitle = "By ${playlist.creator}",
                        coverResId = playlist.coverResId,
                        coverUrl = playlist.coverUrl,
                        onClick = { onPlaylistClick(playlist) },
                        onPlayClick = { onQuickPlayPlaylist(playlist) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = SpotifyWhite,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun QuickPlayCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .testTag("quick_card_${playlist.id}"),
        colors = CardDefaults.cardColors(containerColor = SpotifyCardElevated)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!playlist.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = playlist.coverUrl,
                    contentDescription = playlist.name,
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Crop
                )
            } else if (playlist.coverResId != null) {
                Image(
                    painter = painterResource(id = playlist.coverResId),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(playlist.colorHex))
                )
            }

            Text(
                text = playlist.name,
                color = SpotifyWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // Spotify-style round mini play button
            Surface(
                onClick = onPlayClick,
                shape = CircleShape,
                color = SpotifyGreen,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaCard(
    title: String,
    subtitle: String,
    coverResId: Int?,
    coverUrl: String? = null,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SpotifyCard)
        ) {
            if (!coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (coverResId != null) {
                Image(
                    painter = painterResource(id = coverResId),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Quick Play Button overlaid on bottom-right of card
            Surface(
                onClick = onPlayClick,
                shape = CircleShape,
                color = SpotifyGreen,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(36.dp),
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            color = SpotifyWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = subtitle,
            color = SpotifyTextSecondary,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}
