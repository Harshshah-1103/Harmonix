package com.example.ui.components

import coil.compose.AsyncImage

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.Track
import com.example.ui.theme.SpotifyCardElevated
import com.example.ui.theme.SpotifyGreen
import java.util.Locale

fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

@Composable
fun TrackArtwork(
    track: Track?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(SpotifyCardElevated),
        contentAlignment = Alignment.Center
    ) {
        if (!track?.coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = track?.coverUrl,
                contentDescription = "${track?.title} album art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (track?.coverResId != null) {
            Image(
                painter = painterResource(id = track.coverResId),
                contentDescription = "${track.title} album art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = SpotifyGreen
) {
    val transition = rememberInfiniteTransition(label = "equalizer")

    val h1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val h2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val h3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val bar1Height = if (isPlaying) 16.dp * h1 else 6.dp
        val bar2Height = if (isPlaying) 16.dp * h2 else 12.dp
        val bar3Height = if (isPlaying) 16.dp * h3 else 8.dp

        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar1Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar2Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar3Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}
