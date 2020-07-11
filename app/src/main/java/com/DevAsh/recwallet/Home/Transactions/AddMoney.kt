package com.DevAsh.recwallet.Home.Transactions

import android.app.Activity
import android.os.Bundle
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


class AddMoney : AppCompatActivity() ,PaymentResultListener {
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
                    startPayment(v)
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

    override fun onPaymentError(p0: Int, p1: String?) {
        Toast.makeText(this,"Error in payment",Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onPaymentSuccess(p0: String?) {
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
}