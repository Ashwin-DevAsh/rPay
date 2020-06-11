package com.DevAsh.recwallet.Registration

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.RegistrationContext
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.jacksonandroidnetworking.JacksonParserFactory
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONArray

class Login : AppCompatActivity() {
    lateinit var context:Context
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setParserFactory(JacksonParserFactory())

        context = this

        sendOtp.setOnClickListener{view->

           if(phoneNumber.text.toString().length==10){
               RegistrationContext.phoneNumber = phoneNumber.text.toString()
               AndroidNetworking.get(ApiContext.apiUrl+ApiContext.registrationPort+"/getOtp?number=${RegistrationContext.countryCode+RegistrationContext.phoneNumber}")
                   .setPriority(Priority.IMMEDIATE)
                   .build()
                   .getAsJSONArray(object : JSONArrayRequestListener {
                       override fun onResponse(response: JSONArray?) {
                           startActivity(Intent(context,Otp::class.java))
                           finish()
                       }

                       override fun onError(anError: ANError?) {
                            println(anError)
                            SnackBarHelper.showError(view,anError?.errorDetail.toString())
                       }
                   })

           }else{
               SnackBarHelper.showError(view,"Invalid number")
           }

        }

    }
}

