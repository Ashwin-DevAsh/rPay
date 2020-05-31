package com.DevAsh.recwallet.Registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.RegistrationContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.JsonArray
import com.jacksonandroidnetworking.JacksonParserFactory
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_otp.*
import org.json.JSONArray
import org.json.JSONObject

class Otp : AppCompatActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setParserFactory(JacksonParserFactory())

        context = this

        topNumber.text =
            "Verify +${RegistrationContext.countryCode} ${RegistrationContext.phoneNumber}"
        verifyNumber.text = "+${RegistrationContext.countryCode} ${RegistrationContext.phoneNumber}"

        wrongNumber.setOnClickListener {
            startActivity(Intent(context, Login::class.java))
            finish()
        }

        verify.setOnClickListener { view ->
            if (otp.text.toString().length == 4) {
                mainContent.visibility = INVISIBLE
                AndroidNetworking.post(ApiContext.apiUrl + ApiContext.registrationPort + "/setOtp")
                    .addBodyParameter("otpNumber", otp.text.toString())
                    .addBodyParameter(
                        "number",
                        RegistrationContext.countryCode + RegistrationContext.phoneNumber
                    )
                    .setPriority(Priority.IMMEDIATE)
                    .build()
                    .getAsJSONArray(object : JSONArrayRequestListener {
                        override fun onResponse(response: JSONArray?) {
                            val otpObject = response?.getJSONObject(0)
                            if (otpObject != null && otpObject["message"] == "verified") {
                                try {
                                    println(otpObject["user"])
                                    val user: JSONObject = otpObject["user"] as JSONObject
                                    FirebaseInstanceId.getInstance().instanceId
                                        .addOnCompleteListener(OnCompleteListener { task ->
                                            if (!task.isSuccessful) {
                                                SnackBarHelper.showError(view, "Registration Error")
                                                return@OnCompleteListener
                                            } else {
                                                val fcmToken = task.result?.token
                                                AndroidNetworking.post(ApiContext.apiUrl + ApiContext.registrationPort + "/updateFcmToken")
                                                    .addBodyParameter(
                                                        "fcmToken",
                                                        fcmToken
                                                    )
                                                    .addBodyParameter(
                                                        "number",
                                                        RegistrationContext.countryCode + RegistrationContext.phoneNumber
                                                    ).build()
                                                    .getAsJSONArray(object:JSONArrayRequestListener{
                                                        override fun onResponse(response: JSONArray?) {
                                                            Realm.getDefaultInstance().executeTransaction { realm ->
                                                                realm.delete(Credentials::class.java)
                                                                val credentials = Credentials(
                                                                    user["name"].toString(),
                                                                    user["number"].toString(),
                                                                    user["email"].toString(),
                                                                    user["password"].toString(),
                                                                    otpObject["token"].toString(),
                                                                    true
                                                                )
                                                                realm.insert(credentials)
                                                                DetailsContext.setData(
                                                                    credentials!!.name,
                                                                    credentials.phoneNumber,
                                                                    credentials.email,
                                                                    credentials.password,
                                                                    credentials.token
                                                                )
                                                            }
                                                            startActivity(Intent(context, HomePage::class.java))
                                                            finish()
                                                        }

                                                        override fun onError(anError: ANError?) {
                                                           SnackBarHelper.showError(view,anError.toString())
                                                        }

                                                    })

                                            }
                                        })

                                } catch (e: Exception) {
                                    startActivity(Intent(context, Register::class.java))
                                    finish()
                                }

                            } else {
                                mainContent.visibility = VISIBLE
                                SnackBarHelper.showError(view, "Invalid OTP")
                            }
                        }

                        override fun onError(anError: ANError?) {
                            mainContent.visibility = VISIBLE
                            SnackBarHelper.showError(view, anError.toString())
                        }
                    })
            } else {
                mainContent.visibility = VISIBLE
                SnackBarHelper.showError(view, "Invalid OTP")
            }
        }
    }

    override fun onBackPressed() {

    }
}
