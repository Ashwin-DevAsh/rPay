package com.DevAsh.recwallet.Registration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.RegistrationContext
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.DevAsh.recwallet.R
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

        cancel.setOnClickListener{
            onBackPressed()
        }

        sendOtp.setOnClickListener{view->

           if(phoneNumber.text.toString().length==10){
               hideKeyboardFrom(context,view)
               Handler().postDelayed({
                   mainContent.visibility= View.GONE
               },500)
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

    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

