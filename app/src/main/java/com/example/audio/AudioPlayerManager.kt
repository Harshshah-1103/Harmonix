package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.model.PlaybackState
import com.example.model.RepeatMode
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioPlayerManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var tickerJob: Job? = null
    private var synthJob: Job? = null
    private var isUsingSynth = false

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var originalQueue: List<Track> = emptyList()

    fun setQueueAndPlay(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        originalQueue = tracks
        val activeQueue = if (_playbackState.value.isShuffle) {
            val selected = tracks[startIndex]
            listOf(selected) + (tracks - selected).shuffled()
        } else {
            tracks.drop(startIndex)
        }

        _playbackState.update {
            it.copy(queue = activeQueue)
        }
        playTrack(activeQueue.first())
    }

    fun playTrack(track: Track) {
        stopPlayback()
        _playbackState.update {
            it.copy(
                currentTrack = track,
                isPlaying = true,
                currentPositionMs = 0L,
                durationMs = track.durationMs,
                isBuffering = true
            )
        }

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(track.audioUrl))
                setOnPreparedListener { mp ->
                    _playbackState.update { it.copy(isBuffering = false, durationMs = mp.duration.toLong().coerceAtLeast(track.durationMs)) }
                    mp.start()
                    startTicker()
                }
                setOnCompletionListener {
                    handleTrackCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    Log.w("AudioPlayerManager", "MediaPlayer error: what=$what, extra=$extra. Falling back to ambient audio synth.")
                    startSynthFallback(track)
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Failed to initialize MediaPlayer, fallback to synth", e)
            startSynthFallback(track)
        }
    }

    private fun startSynthFallback(track: Track) {
        stopPlayback()
        isUsingSynth = true
        _playbackState.update {
            it.copy(
                currentTrack = track,
                isPlaying = true,
                isBuffering = false,
                durationMs = track.durationMs
            )
        }

        // Generate gentle soothing melody using AudioTrack in background
        synthJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioTrack = try {
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize.coerceAtLeast( sampleRate * 2 ),
                    AudioTrack.MODE_STREAM
                )
            } catch (e: Exception) {
                null
            }

            audioTrack?.play()
            val notes = listOf(261.63, 329.63, 392.00, 523.25, 440.00, 392.00, 329.63, 293.66)
            var noteIndex = 0

            try {
                while (isActive && isUsingSynth) {
                    val freq = notes[noteIndex % notes.size]
                    noteIndex++
                    val durationSeconds = 0.45
                    val totalSamples = (durationSeconds * sampleRate).toInt()
                    val buffer = ShortArray(totalSamples)

                    for (i in 0 until totalSamples) {
                        val t = i.toDouble() / sampleRate
                        val envelope = if (i < 2000) i / 2000.0 else (totalSamples - i).toDouble() / totalSamples
                        val sample = sin(2.0 * Math.PI * freq * t) * envelope * 0.25 * Short.MAX_VALUE
                        buffer[i] = sample.toInt().toShort()
                    }

                    audioTrack?.write(buffer, 0, buffer.size)
                    delay((durationSeconds * 1000).toLong())
                }
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (e: Exception) {
                    // Ignore release errors
                }
            }
        }
        startTicker()
    }

    fun togglePlayPause() {
        val currentState = _playbackState.value
        if (currentState.currentTrack == null) return

        if (currentState.isPlaying) {
            if (isUsingSynth) {
                synthJob?.cancel()
            } else {
                mediaPlayer?.pause()
            }
            tickerJob?.cancel()
            _playbackState.update { it.copy(isPlaying = false) }
        } else {
            if (isUsingSynth) {
                currentState.currentTrack.let { startSynthFallback(it) }
            } else {
                mediaPlayer?.start()
                startTicker()
            }
            _playbackState.update { it.copy(isPlaying = true) }
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _playbackState.value.durationMs)
        if (!isUsingSynth) {
            mediaPlayer?.seekTo(clamped.toInt())
        }
        _playbackState.update { it.copy(currentPositionMs = clamped) }
    }

    fun skipNext() {
        val currentState = _playbackState.value
        val queue = currentState.queue
        if (queue.isEmpty()) return

        val currentIndex = queue.indexOfFirst { it.id == currentState.currentTrack?.id }
        if (currentIndex != -1 && currentIndex + 1 < queue.size) {
            playTrack(queue[currentIndex + 1])
        } else if (currentState.repeatMode == RepeatMode.ALL && queue.isNotEmpty()) {
            playTrack(queue.first())
        } else {
            // Reached end of queue
            stopPlayback()
            _playbackState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
        }
    }

    fun skipPrevious() {
        val currentState = _playbackState.value
        if (currentState.currentPositionMs > 3000L) {
            seekTo(0L)
            return
        }
        val queue = currentState.queue
        val currentIndex = queue.indexOfFirst { it.id == currentState.currentTrack?.id }
        if (currentIndex > 0) {
            playTrack(queue[currentIndex - 1])
        } else {
            seekTo(0L)
        }
    }

    fun toggleShuffle() {
        _playbackState.update { state ->
            val newShuffle = !state.isShuffle
            val current = state.currentTrack
            val newQueue = if (newShuffle && current != null) {
                listOf(current) + (originalQueue - current).shuffled()
            } else {
                originalQueue
            }
            state.copy(isShuffle = newShuffle, queue = newQueue)
        }
    }

    fun toggleRepeat() {
        _playbackState.update { state ->
            val nextMode = when (state.repeatMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            state.copy(repeatMode = nextMode)
        }
    }

    fun addToQueue(track: Track) {
        _playbackState.update { state ->
            state.copy(queue = state.queue + track)
        }
    }

    fun removeFromQueue(trackId: String) {
        _playbackState.update { state ->
            state.copy(queue = state.queue.filter { it.id != trackId })
        }
    }

    private fun handleTrackCompletion() {
        val currentState = _playbackState.value
        when (currentState.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0L)
                mediaPlayer?.start()
                startTicker()
            }
            else -> skipNext()
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(300L)
                if (_playbackState.value.isPlaying) {
                    val pos = if (isUsingSynth) {
                        (_playbackState.value.currentPositionMs + 300L).coerceAtMost(_playbackState.value.durationMs)
                    } else {
                        mediaPlayer?.currentPosition?.toLong() ?: _playbackState.value.currentPositionMs
                    }
                    _playbackState.update { it.copy(currentPositionMs = pos) }
                    if (isUsingSynth && pos >= _playbackState.value.durationMs) {
                        handleTrackCompletion()
                    }
                }
            }
        }
    }

    private fun stopPlayback() {
        tickerJob?.cancel()
        synthJob?.cancel()
        isUsingSynth = false
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
    }

    fun release() {
        stopPlayback()
    }
}
