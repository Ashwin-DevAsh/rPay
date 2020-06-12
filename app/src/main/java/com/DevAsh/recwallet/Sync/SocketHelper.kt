package com.DevAsh.recwallet.Sync

import android.widget.Toast
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Database.CheckPoint
import com.DevAsh.recwallet.Database.StateLedger
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
import io.realm.Realm
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.logging.Handler

object SocketHelper {

    private val url = ApiContext.apiUrl+ ApiContext.syncPort
    lateinit var socket:Socket
    var newUser:Boolean = false

    fun connect(){
        socket = IO.socket(url)
        socket.connect()

        socket.on("connect") {
            println("connecting ....")
            val data = JSONObject(
               mapOf(
                   "number"  to DetailsContext.phoneNumber,
                   "fcmToken" to DetailsContext.fcmToken
               )
            )
            socket.emit("getInformation",data)

        }

        socket.on("doUpdate"){

        }


        socket.on("disconnect"){
            println("disconnecting...")
        }

        socket.on("receivedPayment"){
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

}