package com.DevAsh.recwallet.Sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Database.Credentials
import com.androidnetworking.AndroidNetworking
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.jacksonandroidnetworking.JacksonParserFactory
import io.realm.Realm


class SocketService : Service() {


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        println("creating ....")

        if (DetailsContext.name == null) {
            Realm.init(this)
            AndroidNetworking.initialize(applicationContext)
            AndroidNetworking.setParserFactory(JacksonParserFactory())
            val credentials = Realm.getDefaultInstance().where(Credentials::class.java).findFirst()
            DetailsContext.setData(
                credentials!!.name,
                credentials.phoneNumber,
                credentials.email,
                credentials.password,
                credentials.token
            )

        }
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    println("Failed . . . .")
                    return@OnCompleteListener
                } else {
                    println("success . . . .")
                    DetailsContext.fcmToken = task.result?.token!!
                    SocketHelper.connect()
                }
            })

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "DevAsh"
            val channelName: CharSequence = "Mining"
            val importance = NotificationManager.IMPORTANCE_LOW
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(notificationChannel)
            val builder: Notification.Builder = Notification.Builder(this, "DevAsh")
                .setContentTitle("Mining coins . . .")
                .setAutoCancel(true)
            val notification: Notification = builder.build()
            startForeground(1, notification)
        } else {
            val builder = NotificationCompat.Builder(this)
                .setContentTitle("Mining coins . . .")
                .setContentText("text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
            val notification: Notification = builder.build()
            startForeground(1, notification)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        println("destroying ....")
        super.onDestroy()
    }
}
