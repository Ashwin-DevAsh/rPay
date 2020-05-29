package com.DevAsh.recwallet.fcm

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Sync.SocketService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.realm.Realm


class NotificationService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        startService(Intent(this,SocketService::class.java))
        super.onMessageReceived(p0)
    }
}