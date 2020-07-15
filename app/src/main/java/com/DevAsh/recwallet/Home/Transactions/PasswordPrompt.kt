package com.DevAsh.recwallet.Home.Transactions

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
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Context.TransactionContext.needToPay
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.SplashScreen
import com.DevAsh.recwallet.Sync.SocketHelper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.activity_amount_prompt.back
import kotlinx.android.synthetic.main.activity_amount_prompt.done
import kotlinx.android.synthetic.main.activity_password_prompt.*
import kotlinx.android.synthetic.main.activity_transaction_status.*
import org.json.JSONObject
import java.text.DecimalFormat


class PasswordPrompt : AppCompatActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_prompt)

        context=this

        back.setOnClickListener{
            super.onBackPressed()
        }

        done.setOnClickListener{v->

            if(DetailsContext.password==password.text.toString()){
                needToPay = true
                hideKeyboardFrom(context,v)
                Handler().postDelayed({
                    transaction()
                },500)
            }else{
                SnackBarHelper.showError(v,"Invalid Password")
            }

        }

        cancel.setOnClickListener{
            super.onBackPressed()
        }
    }

    fun transaction(){
        if(needToPay){
            loadingScreen.visibility= View.VISIBLE
            needToPay=false
            AndroidNetworking.post(ApiContext.apiUrl + ApiContext.paymentPort + "/pay")
                .setContentType("application/json; charset=utf-8")
                .addHeaders("jwtToken", DetailsContext.token)
                .addApplicationJsonBody(object{
                    var from = DetailsContext.phoneNumber
                    var to = TransactionContext.selectedUser?.number.toString().replace("+","")
                    var amount = TransactionContext.amount
                    var toName = TransactionContext.selectedUser?.name.toString()
                    var fromName = DetailsContext.name
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
                                        StateContext.currentBalance = balance!!
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
                                        transactionSuccessful()
                                    }
                                    override fun onError(anError: ANError?) {
                                        println(anError)
                                    }

                                })

                        }
                    }

                    override fun onError(anError: ANError?) {

                    }

                })
        }
    }

    fun transactionSuccessful(){
        val intent = Intent(this,Successful::class.java)
        intent.putExtra("type","transaction")
        intent.putExtra("amount",TransactionContext.amount)
        startActivity(intent)
        finish()
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
