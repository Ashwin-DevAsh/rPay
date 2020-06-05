package com.DevAsh.recwallet.fcm

import android.content.Intent
import android.os.Build
import com.DevAsh.recwallet.Sync.SocketService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        println("new token $p0")
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        println("Notification Received")
        println(p0.data)
        if (p0.data["type"]=="awake"){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this,SocketService::class.java))
            } else {
                 startService(Intent(this,SocketService::class.java))
            }
        }
        super.onMessageReceived(p0)
    }
}