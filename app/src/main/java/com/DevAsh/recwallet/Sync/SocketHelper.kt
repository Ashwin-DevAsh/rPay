package com.DevAsh.recwallet.Sync

import android.content.Context
import android.content.Intent
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Helper.TransactionsHelper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONObject
import java.text.DecimalFormat


object SocketHelper {

    private val url = ApiContext.apiUrl+ ApiContext.syncPort
    var socket:Socket? = null
    var newUser:Boolean = false
    var context:Context?=null
    var socketIntent : Intent?=null
    fun connect(){
        socket = IO.socket(url)
        socket?.connect()

        socket?.on("connect") {
            println("connecting ....")
            val data = JSONObject(
               mapOf(
                   "id"  to DetailsContext.id,
                   "fcmToken" to DetailsContext.fcmToken
               )
            )
            socket?.emit("getInformation",data)

        }

        socket?.on("doUpdate"){

        }

        socket?.on("disconnect"){
             context?.stopService(socketIntent)
             println("disconnecting...")
        }

        socket?.on("receivedPayment"){
            println("received payment")
            getMyState()
        }

    }

    fun updateProfilePicture(){
        socket?.emit("updateProfilePicture",JSONObject( mapOf(
            "id" to DetailsContext.id
        )))
    }

   fun getMyState(){
        AndroidNetworking.get(ApiContext.apiUrl + ApiContext.profilePort + "/init/${DetailsContext.id}")
            .addHeaders("token",DetailsContext.token)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val balance = response?.getInt("balance")
                    StateContext.currentBalance = balance!!
                    val formatter = DecimalFormat("##,##,##,##,##,##,##,###")
                    StateContext.setBalanceToModel(formatter.format(balance))
                    StateContext.currentBalance= balance
                    val transactionObjectArray = response.getJSONArray("transactions")
                    StateContext.initAllTransaction(TransactionsHelper.addTransaction(transactionObjectArray))
                }
                override fun onError(anError: ANError?) {
                    println(anError)
                }

            })

    }

}