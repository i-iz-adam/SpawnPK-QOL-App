package com.spk.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.spk.app.MainActivity
import com.spk.app.util.PriceUtils

object NotificationHelper {
    private const val CHANNEL_ID = "sales_alerts"
    private var nextNotificationId = 1000

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sale alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you when a tracked item sells on one of your accounts"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun notifySale(
        context: Context,
        accountName: String,
        itemName: String,
        unitPrice: Long,
        amount: Int,
        remainingAfter: Int,
        wasCompleted: Boolean
    ) {
        val total = unitPrice * amount
        val formatted = PriceUtils.format(total)
        val qtyText = if (amount > 1) " x$amount" else ""
        val progressText = if (wasCompleted) "all sold — removed from watchlist" else "$remainingAfter left"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Sold: $itemName$qtyText")
            .setContentText("$accountName sold for $formatted gp · $progressText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$accountName sold $itemName$qtyText for $formatted gp · $progressText")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(nextNotificationId++, notification)
        }
    }
}
