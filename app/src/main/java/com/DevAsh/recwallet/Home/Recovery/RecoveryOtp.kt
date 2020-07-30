package com.DevAsh.recwallet.Home.Recovery

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.RegistrationContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.Models.Merchant
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Registration.Register
import com.DevAsh.recwallet.SplashScreen
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_otp.*
import kotlinx.android.synthetic.main.activity_recovery_otp.*
import kotlinx.android.synthetic.main.activity_recovery_otp.errorMessage
import kotlinx.android.synthetic.main.activity_recovery_otp.mainContent
import kotlinx.android.synthetic.main.activity_recovery_otp.otp
import kotlinx.android.synthetic.main.activity_recovery_otp.verify
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat

class RecoveryOtp : AppCompatActivity() {
    var otpText=""
    var context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery_otp)

        verify.setOnClickListener {
            otpText = otp.text.toString()

            if(otpText.length==4){
                hideKeyboardFrom(context,it)
                Handler().postDelayed({
                    mainContent.visibility = View.INVISIBLE
                },300)
            AndroidNetworking.post(ApiContext.apiUrl + ApiContext.registrationPort + "/setRecoveryOtp")
                .addHeaders("token",DetailsContext.token)
                .addBodyParameter("otpNumber", otpText)
                .addBodyParameter(
                    "emailID",
                     DetailsContext.email
                )
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray?) {
                        val otpObject = response?.getJSONObject(0)
                        if (otpObject != null && otpObject["message"] == "done") {
                            startActivity(Intent(this@RecoveryOtp,NewPassword::class.java))
                            finish()
                        } else {
                            Handler().postDelayed({
                                mainContent.visibility = View.VISIBLE
                                AlertHelper.showError("Invalid Otp",this@RecoveryOtp)
                            },500)
                        }
                    }

                    override fun onError(anError: ANError?) {
                        Handler().postDelayed({
                            AlertHelper.showServerError(this@RecoveryOtp)
                            errorMessage.visibility= View.VISIBLE
                        },500)
                    }
                })
            }
        }

    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}