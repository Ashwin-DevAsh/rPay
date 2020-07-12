package com.DevAsh.recwallet.Home.Transactions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.SplashScreen
import com.DevAsh.recwallet.Sync.SocketHelper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.android.synthetic.main.activity_add_money.*
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.activity_transaction_status.*
import org.json.JSONObject
import java.text.DecimalFormat


class AddMoney : AppCompatActivity(){
    var amount:String =""
    var context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_money)
        done.setOnClickListener{v->
            val amountEditText = (findViewById<EditText>(R.id.amount))
            amount = amountEditText.text.toString()
            try{
                if(amount !="" && Integer.parseInt(amount)>0){
                    payUsingUpi(amount,"9840176511@ybl","Barath","R-pay")
                }else{
                    SnackBarHelper.showError(v,"Invalid Amount")
                }
            }catch (e:java.lang.Exception){
                SnackBarHelper.showError(v,"Invalid Amount")
            }

        }
    }

    private fun startPayment(v:View) {

        val activity: Activity = this
        val co = Checkout()
        co.setKeyID("rzp_test_txenFuJupWfNO6")

        try {
            val options = JSONObject()
            options.put("name","cash")
            options.put("description","sambrun37")

            options.put("image","https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("currency","INR")
            options.put("amount",(Integer.parseInt(amount)*100).toString())

            val prefill = JSONObject()
            prefill.put("email",DetailsContext.email)
            prefill.put("contact",DetailsContext.phoneNumber)

            options.put("prefill",prefill)
            co.open(activity,options)
        }catch (e: Exception){

            e.printStackTrace()
        }
    }

    private fun payUsingUpi(
        amount: String?,
        upiId: String?,
        name: String?,
        note: String?
    ) {
        val uri: Uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn", note)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .build()
        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = uri

        // will always show a dialog to user to choose an app
        val chooser = Intent.createChooser(upiPayIntent, "Pay with")

        // check if intent resolves
        if (null != chooser.resolveActivity(packageManager)) {
            startActivityForResult(chooser, 1)
        } else {
            Toast.makeText(
                this,
                "No UPI app found, please install one to continue",
                Toast.LENGTH_SHORT
            ).show()
        }
    }




    private fun onPaymentSuccess(p0: String?) {
        val amount = this.amount
        AndroidNetworking.post(ApiContext.apiUrl + ApiContext.paymentPort + "/addMoney")
            .setContentType("application/json; charset=utf-8")
            .addHeaders("jwtToken", DetailsContext.token)
            .addApplicationJsonBody(object{
                var from = ""
                var to = DetailsContext.phoneNumber
                var amount = amount
                var toName = DetailsContext.name
                var fromName = "Load Money"
            })
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if(response?.get("message")=="done"){
                        AndroidNetworking.get(ApiContext.apiUrl + ApiContext.paymentPort + "/getMyState?number=${DetailsContext.phoneNumber}")
                            .addHeaders("jwtToken",DetailsContext.token)
                            .setPriority(Priority.IMMEDIATE)
                            .build()
                            .getAsJSONObject(object: JSONObjectRequestListener {
                                override fun onResponse(response: JSONObject?) {
                                    val jsonData = JSONObject()
                                    jsonData.put("to",
                                        TransactionContext.selectedUser?.number.toString().replace("+",""))
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
                                                time = SplashScreen.dateToString(
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
                                    context.finish()
                                }
                                override fun onError(anError: ANError?) {
                                    Toast.makeText(context,"",Toast.LENGTH_LONG).show()
                                }

                            })

                    }
                }

                override fun onError(anError: ANError?) {

                }

            })
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
                    val trxt = data.getStringExtra("response")
                    Log.d("UPI", "onActivityResult: $trxt")
                    val dataList: ArrayList<String> = ArrayList()
                    dataList.add(trxt)
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
            for (i in response.indices) {
                val equalStr =
                    response[i].split("=".toRegex()).toTypedArray()
                if (equalStr.size >= 2) {
                    if (equalStr[0].toLowerCase() == "Status".toLowerCase()) {
                        status = equalStr[1].toLowerCase()
                    } else if (equalStr[0]
                            .toLowerCase() == "ApprovalRefNo".toLowerCase() || equalStr[0]
                            .toLowerCase() == "txnRef".toLowerCase()
                    ) {
                        approvalRefNo = equalStr[1]
                    }
                } else {
                    paymentCancel = "Payment cancelled by user."
                }
            }
            when {
                status == "success" -> {
                    Toast.makeText(this, "Transaction successful.", Toast.LENGTH_SHORT)
                        .show()
                    Log.d("UPI", "responseStr: $approvalRefNo")
                    onPaymentSuccess(approvalRefNo)
                }
                "Payment cancelled by user." == paymentCancel -> {
                    Toast.makeText(this, "Payment cancelled by user.", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Transaction failed.Please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                this,
                "Internet connection is not available. Please check and try again",
                Toast.LENGTH_SHORT
            ).show()
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