package com.DevAsh.recwallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.Registration.Login
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.jacksonandroidnetworking.JacksonParserFactory
import io.realm.Realm
import org.json.JSONObject
import java.text.DecimalFormat


class SplashScreen : AppCompatActivity() {

    lateinit var context: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        Realm.init(this)
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setParserFactory(JacksonParserFactory())
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)
        context = this

        val credentials:Credentials? =  Realm.getDefaultInstance().where(Credentials::class.java).findFirst()

         Handler().postDelayed(
             {
                 if(credentials?.isLogin==true){

                     DetailsContext.setData(
                         credentials!!.name,
                         credentials.phoneNumber,
                         credentials.email,
                         credentials.password,
                         credentials.token
                     )

                     AndroidNetworking.get(ApiContext.apiUrl + ApiContext.paymentPort + "/getState")
                         .addHeaders("jwtToken",DetailsContext.token)
                         .setPriority(Priority.IMMEDIATE)
                         .build()
                         .getAsJSONObject(object: JSONObjectRequestListener {
                             override fun onResponse(response: JSONObject?) {
                                 val formatter = DecimalFormat("##,##,##,##,##,##,###")
                                 StateContext.setBalanceToModel(formatter.format(response?.get("919551574355").toString().toInt()))
                                 startActivity(Intent(context,HomePage::class.java))
                                 finish()
                             }

                             override fun onError(anError: ANError?) {
                                 println(anError)
                             }

                         })


                 }
                 else{
                     startActivity(Intent(context,Login::class.java))
                     finish()
                 }

             },
             100
         )

    }
}
