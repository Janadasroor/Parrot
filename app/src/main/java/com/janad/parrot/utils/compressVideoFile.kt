package com.janad.parrot.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.abedelazizshe.lightcompressorlibrary.config.StorageConfiguration
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * Compresses a video file.
 *
 * @param context The context.
 * @param inputUri The URI of the input video file.
 * @param videoName The name of the output video file. Defaults to "compressed_${System.currentTimeMillis()}.mp4".
 * @return The compressed video file, or null if compression was cancelled.
 */
suspend fun compressVideoFile(
    context: Context,
    inputUri: Uri,
    videoName: String = "compressed_${System.currentTimeMillis()}.mp4"
): File? = suspendCancellableCoroutine { cont ->

    val storageConfig = StorageConfiguration(
        saveAt = Environment.DIRECTORY_MOVIES,
        fileName = videoName
    )

    val config = Configuration(
        quality = VideoQuality.MEDIUM,
        isMinBitrateCheckEnabled = false,
        keepOriginalResolution = false
    )

    VideoCompressor.start(
        context = context,
        uris = listOf(inputUri),
        isStreamable = false,
        storageConfiguration = storageConfig,
        configureWith = config,
        listener = object : CompressionListener {
            override fun onStart(index: Int) {
                // compression started
            }

            override fun onSuccess(index: Int, size: Long, path: String?) {
                // ✅ path = compressed video absolute path
                cont.resume(path?.let { File(it) })
            }

            override fun onFailure(index: Int, failureMessage: String) {
                cont.resumeWithException(Exception(failureMessage))
            }

            override fun onProgress(index: Int, percent: Float) {
                // optional: send progress to a StateFlow or Notification here
            }

            override fun onCancelled(index: Int) {
                cont.resume(null)
            }
        }
    )
}
