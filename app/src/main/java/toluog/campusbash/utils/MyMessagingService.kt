package toluog.campusbash.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import toluog.campusbash.R
import io.fabric.sdk.android.services.settings.IconRequest.build
import androidx.core.app.NotificationManagerCompat
import android.util.Log


class MyMessagingService: FirebaseMessagingService() {

    private val TAG = MyMessagingService::class.java.simpleName

    override fun onMessageReceived(message: RemoteMessage?) {
        Log.d(TAG, "message received")
        val payload = message?.notification
        val data = message?.data


        if(payload != null && data != null && data.isNotEmpty()) {
            Log.d(TAG, "$payload")
            val channelId = data["type"] ?: ""

            val mBuilder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(payload.title)
                    .setContentText(payload.body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager = NotificationManagerCompat.from(this)

            val notificationId = if(channelId == "ticket_purchase") {
                AppContract.PURCHASE_NOTIFICATION
            } else {
                AppContract.UNKNOWN_NOTIFICATION
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel(notificationManager, notificationId, channelId)
            }

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(notificationId, mBuilder.build())
        } else {
            Log.d(TAG, "Message is null")
        }

    }

    @TargetApi(26)
    private fun createChannel(manager: NotificationManagerCompat, id: Int, channelId: String) {
        val name = if(id == 1) {
            "Ticket purchases"
        } else {
            "General"
        }

        val description = if(id == 1) {
            "This notification is displayed when your ticket purchase is confirmed"
        } else {
            "General notifications"
        }
        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = description


    }
}