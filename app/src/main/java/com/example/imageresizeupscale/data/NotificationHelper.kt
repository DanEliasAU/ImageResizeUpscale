package com.example.imageresizeupscale.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.example.imageresizeupscale.R

class NotificationHelper(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "image_downloaded"
        private const val ANDROID_MAX_NOTIFICATIONS = 24
    }

    private val notificationManager: NotificationManager =
        context.getSystemService() ?: throw IllegalStateException()

    private var notificationList: MutableList<Int> = mutableListOf()

    fun setupNotificationChannel() {
        val name = context.resources.getString(R.string.channel_name)
        val descriptionText = context.resources.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(title: String, message: String, thumbnail: Bitmap) {
        // Build notification
        val builder =  with (NotificationCompat.Builder(context, CHANNEL_ID)) {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(title)
            setContentText(message)
            setLargeIcon(thumbnail)
        }
        // Remove oldest notification if max notification limit reached
        if (notificationList.size == ANDROID_MAX_NOTIFICATIONS) {
            val oldestNotificationID = notificationList.first()
            notificationManager.cancel(oldestNotificationID)
        }
        // Generate id and create notification
        val id = generateUniqueID()
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
        // Add notification to notification list
        notificationList.add(id)
    }

    private fun generateUniqueID(): Int {
        return System.currentTimeMillis().toInt()
    }
}