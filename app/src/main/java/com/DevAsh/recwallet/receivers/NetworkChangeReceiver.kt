package com.DevAsh.recwallet.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat.finishAffinity
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.util.NetworkUtil
import com.DevAsh.recwallet.util.NetworkUtil.getConnectivityStatusString
import kotlin.system.exitProcess


class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        try {
            val status = getConnectivityStatusString(context!!)
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                AlertHelper.showAlertDialog(context,"Network error","No internet connection available in this device",object:
                    AlertHelper.AlertDialogCallback{
                    override fun onDismiss() {
                        exitProcess(0)
                    }

                    override fun onDone() {
                        exitProcess(0)

                    }
                })
            }
        }catch(e:Throwable){}

    }
}