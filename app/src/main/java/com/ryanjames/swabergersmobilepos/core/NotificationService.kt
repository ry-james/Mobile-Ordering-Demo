package com.ryanjames.swabergersmobilepos.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.feature.splash.SplashScreenActivity
import kotlinx.android.parcel.Parcelize
import kotlin.random.Random

private const val CHANNEL_ID = "mobile_ordering"

class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, SplashScreenActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        intent.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, message.getNotificationType())
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.ic_empty_bag)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        // Send new token to server here
    }

    private fun RemoteMessage.getNotificationType(): NotificationType? {
        return when (this.data["type"]) {
            "PRODUCT_NOTIFICATION" -> NotificationType.ProductDetailNotification(this.data["productId"] ?: "")
            "ORDER_NOTIFICATION" -> NotificationType.OrderDetailNotification(this.data["orderId"] ?: "")
            else -> null
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channel_name"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                description = "My channel description"
                enableLights(true)
                lightColor = Color.GREEN
            }
        }

        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_NOTIFICATION_TYPE = "extra.notification.type"

        fun getNotificationFromIntent(intent: Intent): NotificationType? {
            return intent.getParcelableExtra(EXTRA_NOTIFICATION_TYPE)
        }
    }

    sealed class NotificationType : Parcelable {

        @Parcelize
        class ProductDetailNotification(val productId: String) : NotificationType(), Parcelable

        @Parcelize
        class OrderDetailNotification(val orderId: String) : NotificationType(), Parcelable
    }

}