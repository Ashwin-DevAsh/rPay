package com.DevAsh.recwallet.Sync

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Database.Credentials
import io.realm.Realm

class SocketService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        if( DetailsContext.credentials==null){
            Realm.init(this)
            DetailsContext.credentials =  Realm.getDefaultInstance().where(Credentials::class.java).findFirst()
        }
        SocketHelper.connect()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
