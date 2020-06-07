package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.DevAsh.recwallet.Context.*
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.SplashScreen.Companion.dateToString
import com.DevAsh.recwallet.Sync.SocketHelper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.jacksonandroidnetworking.JacksonParserFactory
import kotlinx.android.synthetic.main.activity_otp.*
import org.json.JSONObject
import java.text.DecimalFormat

class TransactionStatus : AppCompatActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_status)

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setParserFactory(JacksonParserFactory())

        context=this

        AndroidNetworking.post(ApiContext.apiUrl + ApiContext.paymentPort + "/pay")
            .setContentType("application/json; charset=utf-8")
            .addHeaders("jwtToken", DetailsContext.token)
            .addApplicationJsonBody(object{
                var from = DetailsContext.phoneNumber
                var to = TransactionContext.selectedUser?.number.toString().replace("+","")
                var amount = TransactionContext.amount
                var toName = TransactionContext.selectedUser?.name.toString()
                var fromName = DetailsContext.name
            })
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object :JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    if(response?.get("message")=="done"){
                        AndroidNetworking.get(ApiContext.apiUrl + ApiContext.paymentPort + "/getMyState?number=${DetailsContext.phoneNumber}")
                            .addHeaders("jwtToken",DetailsContext.token)
                            .setPriority(Priority.IMMEDIATE)
                            .build()
                            .getAsJSONObject(object: JSONObjectRequestListener {
                                override fun onResponse(response: JSONObject?) {
                                    val jsonData = JSONObject()
                                    jsonData.put("to",TransactionContext.selectedUser?.number.toString().replace("+",""))
                                    SocketHelper.socket.emit("notifyPayment",jsonData)
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
                                                time = dateToString(
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
                                    Toast.makeText(context,"Done",Toast.LENGTH_LONG).show()

                                }
                                override fun onError(anError: ANError?) {
                                    println(anError)
                                }

                            })

                    }
                }

                override fun onError(anError: ANError?) {

                }

            })


    }
}
