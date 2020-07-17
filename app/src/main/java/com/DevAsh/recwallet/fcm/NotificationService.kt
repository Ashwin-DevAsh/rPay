package com.DevAsh.recwallet.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.SplashScreen
import com.DevAsh.recwallet.Sync.SocketHelper
import com.DevAsh.recwallet.Sync.SocketService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception
import kotlin.random.Random


class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        println("new token $p0")
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {

        if (p0.data["type"]=="awake"){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this,SocketService::class.java))
            } else {
                 startService(Intent(this,SocketService::class.java))
            }
        }else{
            val amount = p0.data["type"]!!.split(",")[3]
            val fromName = p0.data["type"]!!.split(",")[1]

            try {
                SocketHelper.getMyState()
            } catch (e:Throwable){ }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val channelId = "Payment"
                val channelName: CharSequence = "Payment"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val notificationChannel = NotificationChannel(channelId, channelName, importance)
                notificationManager.createNotificationChannel(notificationChannel)
                val builder: Notification.Builder = Notification.Builder(this, "Payment")
                    .setContentTitle("Rec Pay")
                    .setContentText("Received $amount ₿ from $fromName")
                    .setSmallIcon(R.drawable.rpay_notification)
                    .setActions(Notification.Action(R.drawable.coin,"Tap to view",PendingIntent.getActivity(
                        this, 0, Intent(this,SplashScreen::class.java), PendingIntent.FLAG_UPDATE_CURRENT
                    )))
                    .setAutoCancel(true)
                val notification: Notification = builder.build()
                notificationManager.notify(Random.nextInt(1000000000),notification)
            } else {
                val builder = NotificationCompat.Builder(this)
                    .setContentTitle("Rec Pay")
                    .setContentText("Received $amount ₿ from $fromName")
                    .setSmallIcon(R.drawable.rpay_notification)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                val notification: Notification = builder.build()
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(Random.nextInt(1000000000),notification)
            }
        }
        super.onMessageReceived(p0)
    }

}