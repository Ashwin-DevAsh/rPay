package com.DevAsh.recwallet.Home.Transactions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.HelperVariables
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.Helper.TransactionsHelper
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Sync.SocketHelper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.android.synthetic.main.activity_adding_options.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*
import java.util.UUID.*
import kotlin.collections.ArrayList

class AddingOptions : AppCompatActivity(), PaymentResultListener {
    lateinit var amount:String
    var context = this
    var addingOption:String? = "Upi transaction"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding_options)

        amount = intent.getStringExtra("amount")!!

        upi.setOnClickListener{
            addingOption = "Upi transaction"
            loadingScreen.visibility = View.VISIBLE
            payUsingUpi(amount, "reccollege@icici", "Version", "rPay")
        }

        razorpay.setOnClickListener{

//            startActivity(Intent(this,InstamojoActivity::class.java))
            addingOption = "Gateway transaction"
            loadingScreen.visibility = View.VISIBLE
            Handler().postDelayed({
                startPayment()
            }, 1000)
        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        loadingScreen.visibility = View.GONE
    }

    override fun onPaymentSuccess(p0: String?) {
        val amount = this.amount
        Handler().postDelayed({
            loadingScreen.visibility = View.VISIBLE
        }, 500)
        AndroidNetworking.post(ApiContext.paymentSubDomain + "/addMoney")
            .setContentType("application/json; charset=utf-8")
            .addHeaders("token", Credentials.credentials.token)
            .addApplicationJsonBody(object {
                var amount = amount
                var to = object {
                    var id = Credentials.credentials.id
                    var name = Credentials.credentials.accountName
                    var number = Credentials.credentials.phoneNumber
                    var email = Credentials.credentials.email
                }
                var from = object {
                    var id = p0
                    var name = addingOption
                    var number = "None"
                    var email = "None"
                }
            })
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    loadingScreen.visibility = View.VISIBLE
                    println(response)
                    if (response?.get("message") == "done") {
                        AndroidNetworking.get(ApiContext.profileSubDomain + "/init/${Credentials.credentials.id}")
                            .addHeaders("token", Credentials.credentials.token)
                            .setPriority(Priority.IMMEDIATE)
                            .build()
                            .getAsJSONObject(object : JSONObjectRequestListener {
                                override fun onResponse(response: JSONObject?) {
                                    val jsonData = JSONObject()
                                    jsonData.put(
                                        "to",
                                        HelperVariables.selectedUser?.number.toString().replace(
                                            "+",
                                            ""
                                        )
                                    )
                                    SocketHelper.socket?.emit("notifyPayment", jsonData)
                                    val balance = response?.getInt("balance")
                                    StateContext.currentBalance = balance!!
                                    val formatter = DecimalFormat("##,##,##,##,##,##,##,###")
                                    StateContext.setBalanceToModel(formatter.format(balance))
                                    val transactionObjectArray =
                                        response.getJSONArray("transactions")
                                    StateContext.initAllTransaction(TransactionsHelper.addTransaction(
                                            transactionObjectArray
                                        )
                                    )
                                    val intent = Intent(context, Successful::class.java)
                                    intent.putExtra("type", "addMoney")
                                    intent.putExtra("amount", amount)
                                    startActivity(intent)
                                    context.finish()
                                }

                                override fun onError(anError: ANError?) {
                                    AlertHelper.showAlertDialog(this@AddingOptions,
                                        "Failed !",
                                        "your transaction of $amount ${HelperVariables.currency} is failed. if any amount debited it will refund soon",
                                        object : AlertHelper.AlertDialogCallback {
                                            override fun onDismiss() {
                                                loadingScreen.visibility = View.INVISIBLE
                                                onBackPressed()
                                            }

                                            override fun onDone() {
                                                loadingScreen.visibility = View.INVISIBLE
                                                onBackPressed()
                                            }
                                        }
                                    )
                                }
                            })
                    } else {
                        AlertHelper.showAlertDialog(this@AddingOptions,
                            "Failed !",
                            "your transaction of $amount ${HelperVariables.currency} is failed. if any amount debited it will refund soon",
                            object : AlertHelper.AlertDialogCallback {
                                override fun onDismiss() {
                                    loadingScreen.visibility = View.INVISIBLE
                                    onBackPressed()
                                }

                                override fun onDone() {
                                    loadingScreen.visibility = View.INVISIBLE
                                    onBackPressed()
                                }
                            }
                        )
                    }
                }

                override fun onError(anError: ANError?) {

                }
            })
    }


    override fun onResume() {
        loadingScreen.visibility = View.GONE
        super.onResume()
    }

    private fun payUsingUpi(
        amount: String?,
        upiId: String?,
        name: String?,
        note: String
    ) {

        val transactionID = randomUUID().toString()

        println("transaction id = $transactionID")

        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn", note)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tr",transactionID)
            .build()

        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = uri

        // will always show a dialog to user to choose an app
        val chooser = Intent.createChooser(upiPayIntent, "Pay with")
        if (null != chooser.resolveActivity(packageManager)) {
           startActivityForResult(chooser, 1)
        } else {
          AlertHelper.showError(
              "No UPI app found, please install one to continue",
              this@AddingOptions
          )
        }
    }

    private fun getUPIString(
        payeeAddress: String,
        payeeName: String,
        payeeMCC: String,
        trxnID: String,
        trxnRefId: String,
        trxnNote: String,
        payeeAmount: String,
        currencyCode: String,
        refUrl: String
    ): String? {
        val UPI = ("upi://pay?pa=" + payeeAddress + "&pn=" + payeeName
                + "&mc=" + payeeMCC + "&tid=" + trxnID + "&tr=" + trxnRefId
                + "&tn=" + trxnNote + "&am=" + payeeAmount + "&cu=" + currencyCode
                + "&refUrl=" + refUrl)
        return UPI.replace(" ", "+")
    }

    private fun startPayment() {


        val activity: Activity = this
        val co = Checkout()
        co.setKeyID("rzp_test_xkmYhXXE5iOTRu")
        try {
            val options = JSONObject()
            options.put("name", "Adding Money")
            options.put("description", "This process require a 2% commission")
            options.put("currency", "INR")
            options.put(
                "amount",
                ((Integer.parseInt(amount) * 100) + ((Integer.parseInt(amount) * 100) * 0.02
                        )).toString()
            )
            val prefill = JSONObject()
            prefill.put("email", Credentials.credentials.email)
            prefill.put("contact", Credentials.credentials.phoneNumber)
            options.put("prefill", prefill)
            co.open(activity, options)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> if (Activity.RESULT_OK == resultCode || resultCode == 11) {
                if (data != null) {
                    println("data = $data")
                    val trxt = data.getStringExtra("response")
                    Log.d("UPI", "onActivityResult: $trxt")
                    val dataList: ArrayList<String> = ArrayList()
                    dataList.add(trxt!!)
                    upiPaymentDataOperation(dataList)
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null")
                    val dataList: ArrayList<String> = ArrayList()
                    dataList.add("nothing")
                    upiPaymentDataOperation(dataList)
                }
            }
        }
    }

    private fun upiPaymentDataOperation(data: ArrayList<String>) {
        if (isConnectionAvailable(this)) {
            var str = data[0]
            var paymentCancel = ""
            if (str == null) str = "discard"
            var status = ""
            var approvalRefNo = ""
            val response = str.split("&".toRegex()).toTypedArray()
            println("responseString = $str \n")
            println("response = $response")
            for (i in response.indices) {
                val equalStr = response[i].split("=".toRegex()).toTypedArray()
                if (equalStr.size >= 2) {
                    if (equalStr[0].equals("Status", ignoreCase = true)) {
                        status = equalStr[1].toLowerCase(Locale.ROOT)
                    } else if (equalStr[0].equals("ApprovalRefNo", ignoreCase = true) || equalStr[0].equals(
                            "txnRef", ignoreCase = true)
                    ) {
                        approvalRefNo = equalStr[1]
                        println(equalStr)
                    }
                } else {
                    paymentCancel = "Payment cancelled by user."
                }
            }
            when {
                status == "success" -> {
                    onPaymentSuccess(approvalRefNo)
                }
                "Payment cancelled by user." == paymentCancel -> {
                    loadingScreen.visibility = View.GONE
                    AlertHelper.showError("Payment cancelled by user.", this)
                }
                else -> {
                    loadingScreen.visibility = View.GONE
                    AlertHelper.showError(
                        "Transaction failed.Please try again",
                        this
                    )
                }
            }
        } else {
            AlertHelper.showError(
                "Internet connection is not available. Please check and try again",
                this
            )
        }
    }

    private fun isConnectionAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val netInfo = connectivityManager.activeNetworkInfo
            if (netInfo != null && netInfo.isConnected
                && netInfo.isConnectedOrConnecting
                && netInfo.isAvailable
            ) {
                return true
            }
        }
        return false
    }
}

