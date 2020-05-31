package com.DevAsh.recwallet.Sync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Database.Credentials
import com.androidnetworking.AndroidNetworking
import com.jacksonandroidnetworking.JacksonParserFactory
import io.realm.Realm

class SocketService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
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
        SocketHelper.connect()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
