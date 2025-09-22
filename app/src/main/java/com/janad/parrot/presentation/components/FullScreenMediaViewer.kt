package com.janad.parrot.presentation.components
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.janad.parrot.data.api.NetworkModule.IMAGES_PATH
import com.janad.parrot.data.api.NetworkModule.VIDEOS_PATH
import com.janad.parrot.data.models.network.Media
import com.janad.parrot.utils.downloadFile

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenMediaViewer(
    mediaList: List<Media>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { mediaList.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Media Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val media = mediaList[page]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (media.fileType) {
                        "image" -> {
                            var scale by remember { mutableFloatStateOf(1f) }
                            var offset by remember { mutableStateOf(Offset.Zero) }

                            AsyncImage(
                                model = IMAGES_PATH + media.fileName,
                                contentDescription = "Full size image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    )
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                                            offset = Offset(
                                                x = (offset.x + pan.x),
                                                y = (offset.y + pan.y)
                                            )
                                        }
                                    }
                            )
                        }
                        "video" -> {
                            val videoUrl = VIDEOS_PATH + media.fileName

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {


                                // Actual Video Player
                                VideoPlayer(
                                    videoUrl = videoUrl,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp) // set your preferred height
                                )
                            }
                        }

                    }
                }
            }

            // Top Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .zIndex(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    IconButton(
                        onClick = {
                        val media = mediaList[pagerState.currentPage]
                        val url = when (media.fileType) {
                            "image" -> IMAGES_PATH + media.fileName
                            "video" -> VIDEOS_PATH + media.fileName
                            else -> null
                        }

                        url?.let {
                            // choose a filename (could be media.fileName)
                            downloadFile(context, it, media.fileName ?: "downloaded_file")
                        }
                    },
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { /* Share functionality */ },
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Info
            if (mediaList.size > 1) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${mediaList.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}