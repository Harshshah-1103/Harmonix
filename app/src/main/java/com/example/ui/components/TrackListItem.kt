package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextSecondary
import com.example.ui.theme.SpotifyWhite

@Composable
fun TrackListItem(
    track: Track,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    onTrackClick: () -> Unit,
    onToggleLike: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onAddToQueue: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int? = null,
    showCover: Boolean = true
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTrackClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("track_item_${track.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (index != null) {
            Box(
                modifier = Modifier.width(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isCurrentTrack) {
                    EqualizerBars(isPlaying = isPlaying)
                } else {
                    Text(
                        text = "$index",
                        color = SpotifyTextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (showCover) {
            TrackArtwork(
                track = track,
                modifier = Modifier.size(48.dp),
                cornerRadius = 4.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                color = if (isCurrentTrack) SpotifyGreen else SpotifyWhite,
                fontSize = 15.sp,
                fontWeight = if (isCurrentTrack) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${track.artist} • ${formatDuration(track.durationMs)}",
                    color = SpotifyTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = onToggleLike,
            modifier = Modifier
                .size(40.dp)
                .testTag("like_track_${track.id}")
        ) {
            Icon(
                imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (track.isLiked) "Unlike" else "Like",
                tint = if (track.isLiked) SpotifyGreen else SpotifyTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("track_menu_${track.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Track options",
                    tint = SpotifyTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Add to playlist") },
                    onClick = {
                        menuExpanded = false
                        onAddToPlaylist()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to queue") },
                    onClick = {
                        menuExpanded = false
                        onAddToQueue()
                    }
                )
            }
        }
    }
}
