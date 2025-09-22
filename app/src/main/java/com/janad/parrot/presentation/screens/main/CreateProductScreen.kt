package com.janad.parrot.presentation.screens.main


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.janad.parrot.data.models.network.UploadState
import com.janad.parrot.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
@Composable
fun CreateProductScreen(mainViewModel: MainViewModel = hiltViewModel()) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    val uploadState by mainViewModel.uploadState.collectAsState()
    val selectedMedia = remember { mutableStateListOf<MediaItem>() }
    val context = LocalContext.current

    // Gallery picker for images
    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            selectedMedia.add(MediaItem(uri, MediaType.IMAGE))
        }
    }

    // Gallery picker for videos
    val pickVideosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            selectedMedia.add(MediaItem(uri, MediaType.VIDEO))
        }
    }

    // Camera capture for images
    val captureImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val file = copyBitmapToFile(context, bitmap)
            selectedMedia.add(MediaItem(file.toUri(), MediaType.IMAGE))
        }
    }
    // Camera capture for videos
    var currentVideoFile by remember { mutableStateOf<File?>(null) }
    val captureVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("VideoCapture", "Result code: ${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK) {
            currentVideoFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    Log.d("VideoCapture", "Video file created successfully: ${file.absolutePath}, size: ${file.length()}")
                    selectedMedia.add(MediaItem(file.toUri(), MediaType.VIDEO))
                } else {
                    Log.e("VideoCapture", "Video file is empty or doesn't exist")
                }
            } ?: Log.e("VideoCapture", "Current video file is null")
        } else {
            Log.d("VideoCapture", "Video capture canceled or failed")
            // Clean up the temporary file if capture was cancelled
            currentVideoFile?.delete()
        }
        // Reset the current video file reference
        currentVideoFile = null
    }





    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header

                Column(
                    modifier =Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create New Product",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Product Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Product Title") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        },
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                price = newValue
                            }
                        },
                        label = { Text("Price") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Text("$", fontWeight = FontWeight.Bold)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        item {
            // Media Selection Card with fixed video recording
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Media",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    )
                    {
                        item {
                            MediaActionButton(
                                icon = Icons.Default.PhotoLibrary,
                                text = "Photos",
                                onClick = { pickImagesLauncher.launch("image/*") }
                            )
                        }
                        item {
                            MediaActionButton(
                                icon = Icons.Default.VideoLibrary,
                                text = "Videos",
                                onClick = { pickVideosLauncher.launch("video/*") }
                            )
                        }
                        item {
                            MediaActionButton(
                                icon = Icons.Default.CameraAlt,
                                text = "Camera",
                                onClick = { captureImageLauncher.launch(null) }
                            )
                        }
                        item {
                            MediaActionButton(
                                icon = Icons.Default.Videocam,
                                text = "Record",
                                onClick = {
                                    try {
                                        // Create the video file in the files directory instead of cache
                                        val videoFile = createTempVideoFile(context)
                                        currentVideoFile = videoFile

                                        val videoUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            videoFile
                                        )

                                        Log.d("VideoCapture", "Created video file: ${videoFile.absolutePath}")
                                        Log.d("VideoCapture", "Video URI: $videoUri")

                                        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                                            putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                                            putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1) // High quality
                                            putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30) // 30 seconds max
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                        }

                                        captureVideoLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Log.e("VideoCapture", "Error creating video capture intent", e)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (selectedMedia.isNotEmpty()) {
            item {
                // Selected Media Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected Media (${selectedMedia.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            TextButton(
                                onClick = { selectedMedia.clear() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear All")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(selectedMedia.size) { index ->
                                val mediaItem = selectedMedia[index]
                                var isHD by rememberSaveable { mutableStateOf(mediaItem.isHD) }
                               Column {
                                   MediaThumbnail(
                                       mediaItem = mediaItem,
                                       onDelete = { selectedMedia.removeAt(index) }
                                   )
                                   Row(
                                       verticalAlignment = Alignment.CenterVertically,
                                       modifier = Modifier.padding(top = 4.dp)
                                   ) {
                                       Checkbox(
                                           checked = isHD,
                                           onCheckedChange = {
                                               it->
                                               isHD = it
                                               selectedMedia[index].isHD = isHD

                                           }
                                       )
                                       Text(
                                           text = "HD",
                                           modifier = Modifier.padding(start = 4.dp)
                                       )
                                   }
                               }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Submit Button
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank() && price.isNotBlank()) {

                        // Handle submission
                        mainViewModel.postProductWithMedia(title, description, price.toDouble(), selectedMedia, context)
                        // viewModel.postProduct(title, description, price.toDouble(), selectedMedia)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uploadState !is UploadState.Loading && title.isNotBlank() &&
                        description.isNotBlank() && price.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uploadState is UploadState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating Product...")
                } else {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Create Product",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        item {
            // Inside your composable
            when (uploadState) {
                is UploadState.Loading -> {}
                is UploadState.Success -> {
                    AlertDialog(
                        onDismissRequest = { mainViewModel.resetUploadState()},
                        confirmButton = {
                            TextButton(onClick = {  mainViewModel.resetUploadState()}) {
                                Text("OK")
                            }
                        },
                        title = { Text("Success") },
                        text = { Text("Your product has been created successfully!") }
                    )
                }
                is UploadState.Error -> {
                    val errorMessage = (uploadState as UploadState.Error).message
                    AlertDialog(
                        onDismissRequest = { mainViewModel.resetUploadState() },
                        confirmButton = {
                            TextButton(onClick = { mainViewModel.postProductWithMedia(title, description, price.toDouble(), selectedMedia, context) }) {
                                Text("Retry")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                mainViewModel.resetUploadState()
                            }) {
                                Text("Cancel")
                            }
                        },
                        title = { Text("Upload Failed") },
                        text = { Text("Error: $errorMessage") }
                    )
                }
                else -> {}
            }


        }
    }
}

private fun MainViewModel.resetUploadState() {
    _uploadState.value = UploadState.Idle
}


@Composable
private fun MediaActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 14.sp)
    }
}

@Composable
private fun MediaThumbnail(
    mediaItem: MediaItem,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        when (mediaItem.type) {
            MediaType.IMAGE -> {
                AsyncImage(
                    model = mediaItem.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            MediaType.VIDEO -> {
                VideoThumbnail(
                    uri = mediaItem.uri,
                    modifier = Modifier.fillMaxSize()
                )
                // Video play overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Delete button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(28.dp)
                .background(
                    MaterialTheme.colorScheme.error,
                    RoundedCornerShape(14.dp)
                )
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(16.dp)
            )
        }

        // Media type indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (mediaItem.type == MediaType.VIDEO) "VIDEO" else "PHOTO",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun VideoThumbnail(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                isLoading = true
                hasError = false

                val retriever = MediaMetadataRetriever()

                // Try to set data source with the URI
                retriever.setDataSource(context, uri)

                // Get thumbnail from the first frame
                val bitmap = retriever.getFrameAtTime(1000000) // 1 second into video
                retriever.release()

                if (bitmap != null) {
                    thumbnail = bitmap
                    Log.d("VideoThumbnail", "Successfully created thumbnail for: $uri")
                } else {
                    hasError = true
                    Log.e("VideoThumbnail", "Failed to extract thumbnail from: $uri")
                }
            } catch (e: Exception) {
                hasError = true
                Log.e("VideoThumbnail", "Error creating video thumbnail for: $uri", e)
            } finally {
                isLoading = false
            }
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
        hasError -> {
            Box(
                modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.VideoLibrary,
                    contentDescription = "Video",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        thumbnail != null -> {
            Image(
                bitmap = thumbnail!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
    }
}

// Data classes
data class MediaItem(
    val uri: Uri,
    val type: MediaType,
    var isHD: Boolean = false
)

enum class MediaType {
    IMAGE, VIDEO
}

// Utility functions
// Fixed utility function for creating video files
fun createTempVideoFile(context: Context): File {
    val fileName = "video_${System.currentTimeMillis()}.mp4"
    // Use files directory instead of cache directory for better reliability
    val videosDir = File(context.filesDir, "videos")
    if (!videosDir.exists()) {
        videosDir.mkdirs()
    }
    return File(videosDir, fileName)
}

// Enhanced video thumbnail with better error handling

fun getImageName(context: Context, uri: Uri): String {
    val defaultName = "image_${System.currentTimeMillis()}"
    var name: String? = null

    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1) {
                name = it.getString(index)
            }
        }
    }

    return name ?: "$defaultName.jpg"
}

fun getVideoName(context: Context, uri: Uri): String {
    val defaultName = "video_${System.currentTimeMillis()}"
    var name: String? = null

    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1) {
                name = it.getString(index)
            }
        }
    }

    return name ?: "$defaultName.mp4"
}

fun copyUriToFile(context: Context, uri: Uri, mediaType: MediaType): File {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val originalName = when (mediaType) {
        MediaType.IMAGE -> getImageName(context, uri)
        MediaType.VIDEO -> getVideoName(context, uri)
    }

    val file = File(context.cacheDir, originalName)
    val outputStream = FileOutputStream(file)
    inputStream.copyTo(outputStream)
    inputStream.close()
    outputStream.close()
    return file
}

fun copyBitmapToFile(context: Context, bitmap: Bitmap): File {
    val fileName = "camera_${System.currentTimeMillis()}.jpg"
    val file = File(context.cacheDir, fileName)
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
    }
    return file
}