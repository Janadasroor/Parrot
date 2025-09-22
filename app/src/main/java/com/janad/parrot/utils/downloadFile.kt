package com.janad.parrot.utils

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri

fun downloadFile(context: Context, url: String, fileName: String) {
    val request = DownloadManager.Request(url.toUri())
        .setTitle("Downloading $fileName")
        .setDescription("Downloading media file")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
}
