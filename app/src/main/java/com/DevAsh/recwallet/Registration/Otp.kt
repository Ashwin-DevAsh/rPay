package com.DevAsh.recwallet.Registration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
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
import com.DevAsh.recwallet.SplashScreen
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.jacksonandroidnetworking.JacksonParserFactory
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_otp.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat


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

        cancel.setOnClickListener{
            startActivity(Intent(context, Login::class.java))
            finish()
        }

        verify.setOnClickListener { view ->
            StateContext.initRecentContact(arrayListOf())
            errorMessage.visibility= INVISIBLE
            if (otp.text.toString().length == 4) {
                hideKeyboardFrom(context,view)
                Handler().postDelayed({
                    mainContent.visibility = INVISIBLE

                },300)
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
                                            Handler().postDelayed({
                                                AndroidNetworking.get(ApiContext.apiUrl + ApiContext.paymentPort + "/getMyState?number=${DetailsContext.phoneNumber}")
                                                    .addHeaders("jwtToken",DetailsContext.token)
                                                    .setPriority(Priority.IMMEDIATE)
                                                    .build()
                                                    .getAsJSONObject(object: JSONObjectRequestListener {
                                                        override fun onResponse(response: JSONObject?) {
                                                            val balance = response?.getInt("Balance")
                                                            StateContext.currentBalance = balance!!
                                                            val formatter = DecimalFormat("##,##,##,##,##,##,##,###")
                                                            StateContext.setBalanceToModel(formatter.format(balance))
                                                            val transactionObjectArray = response?.getJSONArray("Transactions")
                                                            val transactions = ArrayList<Transaction>()
                                                            for (i in 0 until transactionObjectArray!!.length()) {
                                                                val name = if (transactionObjectArray.getJSONObject(i)["From"] == DetailsContext.phoneNumber)
                                                                    transactionObjectArray.getJSONObject(i)["ToName"].toString()
                                                                else transactionObjectArray.getJSONObject(i)["FromName"].toString()
                                                                val number = if (transactionObjectArray.getJSONObject(i)["From"] == DetailsContext.phoneNumber)
                                                                    transactionObjectArray.getJSONObject(i)["To"].toString()
                                                                else transactionObjectArray.getJSONObject(i)["From"].toString()

                                                                val merchant = Merchant(name,number)
                                                                if(!transactionObjectArray.getJSONObject(i).getBoolean("IsGenerated"))
                                                                     StateContext.addRecentContact(merchant)
                                                                transactions.add(
                                                                    0, Transaction(
                                                                        name = name,
                                                                        number = number,
                                                                        amount = transactionObjectArray.getJSONObject(i)["Amount"].toString(),
                                                                        time = SplashScreen.dateToString(
                                                                            transactionObjectArray.getJSONObject(
                                                                                i
                                                                            )["TransactionTime"].toString()
                                                                        ),
                                                                        type = if (transactionObjectArray.getJSONObject(i)["From"] == DetailsContext.phoneNumber)
                                                                            "Send"
                                                                        else "Received",
                                                                        transactionId =  transactionObjectArray.getJSONObject(i)["TransactionID"].toString(),
                                                                        isGenerated = transactionObjectArray.getJSONObject(i).getBoolean("IsGenerated")
                                                                    )
                                                                )
                                                            }

                                                            StateContext.initAllTransaction(transactions)
                                                            startActivity(Intent(context, HomePage::class.java))
                                                            finish()
                                                        }

                                                        override fun onError(anError: ANError?) {
                                                            AlertHelper.showServerError(this@Otp)
                                                        }

                                                    })
                                            },0)
                                        }

                                } catch (e: Exception) {
                                    startActivity(Intent(context, Register::class.java))
                                    finish()
                                }

                            } else {
                                mainContent.visibility = VISIBLE
                                AlertHelper.showError("Invalid Otp",this@Otp)
                            }
                        }

                        override fun onError(anError: ANError?) {
                            AlertHelper.showServerError(this@Otp)
                            errorMessage.visibility=VISIBLE
                        }
                    })
            } else {
                mainContent.visibility = VISIBLE
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
