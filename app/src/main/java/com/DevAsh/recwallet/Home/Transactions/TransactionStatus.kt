package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.RegistrationContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.jacksonandroidnetworking.JacksonParserFactory
import kotlinx.android.synthetic.main.activity_otp.*
import org.json.JSONObject

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
                var from = RegistrationContext.countryCode+RegistrationContext.phoneNumber
                var to = TransactionContext.selectedUser?.number
                var amount = TransactionContext.amount
            })
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object :JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    if(response?.get("message")=="done"){
                        Toast.makeText(context,"Done",Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {

                }

            })


    }
}
