package com.DevAsh.recwallet.Sync

import android.widget.Toast
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.SplashScreen
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.logging.Handler

object SocketHelper {

    private val url = ApiContext.apiUrl+ ApiContext.syncPort
    lateinit var socket:Socket

    fun connect(){
        println("Called . . .")
        socket = IO.socket(url)
        socket.connect()

        socket.on("connect") {
            println("connecting ....")
            val data = JSONObject()
            data.put("number",DetailsContext.phoneNumber)
            data.put("fcmToken",DetailsContext.fcmToken)
            socket.emit("getInformation",data)
        }

        socket.on("doUpdate"){
              println("updating ....")
              getState()
        }

        socket.on("disconnect"){
            println("disconnecting...")
        }

        socket.on("receivedPayment"){
            println("received payment")
            getMyState()
        }



    }

   fun getMyState(){
        AndroidNetworking.get(ApiContext.apiUrl + ApiContext.paymentPort + "/getMyState?number=${DetailsContext.phoneNumber}")
            .addHeaders("jwtToken",DetailsContext.token)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val balance = response?.getInt("Balance")
                    val formatter = DecimalFormat("##,##,##,##,##,##,##,###")
                    StateContext.setBalanceToModel(formatter.format(balance))
                    val transactionObjectArray = response?.getJSONArray("Transactions")
                    val transactions = ArrayList<Transaction>()
                    for (i in 0 until transactionObjectArray!!.length()) {
                        transactions.add(
                            0, Transaction(
                                name = if (transactionObjectArray.getJSONObject(i)["From"] == DetailsContext.phoneNumber)
                                    transactionObjectArray.getJSONObject(i)["ToName"].toString()
                                else transactionObjectArray.getJSONObject(i)["FromName"].toString(),
                                number = if (transactionObjectArray.getJSONObject(i)["From"] == DetailsContext.phoneNumber)
                                    transactionObjectArray.getJSONObject(i)["To"].toString()
                                else transactionObjectArray.getJSONObject(i)["From"].toString(),
                                amount = transactionObjectArray.getJSONObject(i)["Amount"].toString(),
                                time = SplashScreen.dateToString(
                                    transactionObjectArray.getJSONObject(
                                        i
                                    )["TransactionTime"].toString()
                                ),
                                type = if (transactionObjectArray.getJSONObject(i)["From"] == DetailsContext.phoneNumber)
                                    "Send"
                                else "Received"
                            )
                        )
                    }
                    StateContext.initAllTransaction(transactions)
                }
                override fun onError(anError: ANError?) {
                    println(anError)
                }

            })

    }



    private fun getState(){
        AndroidNetworking.get(ApiContext.apiUrl + ApiContext.paymentPort + "/getState")
            .addHeaders("jwtToken",DetailsContext.token)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    StateContext.state = response
                }

                override fun onError(anError: ANError?) {
                    socket.disconnect()
                }

            })
    }
}