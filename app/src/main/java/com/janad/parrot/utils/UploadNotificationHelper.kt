package com.janad.parrot.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class UploadNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        // Create channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "upload_channel",
                "Uploads",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Shows upload progress"
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showProgressNotification(notificationId: Int, fileName: String, progress: Int) {
        val builder = NotificationCompat.Builder(context, "upload_channel")
            .setContentTitle("Uploading $fileName")
            .setContentText("$progress%")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, false)
            .setOngoing(true)

        notificationManager.notify(notificationId, builder.build())
    }

    fun showCompletedNotification(notificationId: Int, fileName: String) {
        val builder = NotificationCompat.Builder(context, "upload_channel")
            .setContentTitle("Uploaded $fileName")
            .setContentText("Upload complete")
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setProgress(0, 0, false)
            .setOngoing(false)

        notificationManager.notify(notificationId, builder.build())
    }

    fun showFailedNotification(notificationId: Int, fileName: String) {
        val builder = NotificationCompat.Builder(context, "upload_channel")
            .setContentTitle("Upload failed")
            .setContentText(fileName)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setProgress(0, 0, false)
            .setOngoing(false)

        notificationManager.notify(notificationId, builder.build())
    }
}
