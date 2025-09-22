package com.janad.parrot.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl.toUri()))
            prepare()
            playWhenReady = true // autoplay
        }
    }

    // Release the player when the Composable leaves the composition
    DisposableEffect(
        AndroidView(factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = true // Show playback controls
            }
        }, modifier = modifier)
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}
