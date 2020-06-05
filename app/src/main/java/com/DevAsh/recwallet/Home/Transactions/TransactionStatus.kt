package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.DevAsh.recwallet.Context.*
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.R
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
                                    val formatter = DecimalFormat("##,##,##,##,##,##,##,###")
                                    StateContext.setBalanceToModel(formatter.format(response?.get(DetailsContext.phoneNumber!!).toString().toInt()))
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
