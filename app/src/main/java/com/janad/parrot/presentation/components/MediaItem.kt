package com.janad.parrot.presentation.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.janad.parrot.data.api.NetworkModule.IMAGES_PATH
import com.janad.parrot.data.api.NetworkModule.VIDEOS_PATH
import com.janad.parrot.data.models.network.Media

@Composable
fun MediaItem(
    media: Media,
    modifier: Modifier = Modifier
) {
    var boxHeight by remember { mutableStateOf(0.dp) }
    val localDensity = androidx.compose.ui.platform.LocalDensity.current
    Box(modifier = modifier.onSizeChanged {
        boxHeight = with(localDensity) { it.height.toDp() }
        Log.d("MediaItem", "Box height: $boxHeight")
    }) {
        when (media.fileType) {
            "image" -> {
                AsyncImage(
                    model = IMAGES_PATH + media.fileName,
                    contentDescription = "Product image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            "video" -> {
                val videoUrl = VIDEOS_PATH + media.fileName

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    VideoPlayer(
                        videoUrl = videoUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(boxHeight)
                    )
                }
            }

        }
    }
}
