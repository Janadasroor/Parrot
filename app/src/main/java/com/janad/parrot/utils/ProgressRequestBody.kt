package com.janad.parrot.utils

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File

/**
 A custom RequestBody that monitors the progress of file uploads
 and updates a notification accordingly.
**/
class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val notificationHelper: UploadNotificationHelper,
    private val notificationId: Int
) : RequestBody() {

    override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()
    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        file.inputStream().use { input ->
            var uploaded: Long = 0
            var read: Int
            val source = input.buffered()
            while (source.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read
                val progress = ((uploaded * 100) / totalBytes).toInt()

                // update notification
                notificationHelper.showProgressNotification(
                    notificationId,
                    file.name,
                    progress
                )
            }
        }
    }
}
