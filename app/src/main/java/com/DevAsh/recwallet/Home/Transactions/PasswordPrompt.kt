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
import com.DevAsh.recwallet.Database.RealmHelper
import com.DevAsh.recwallet.Database.RecentContacts
import com.DevAsh.recwallet.Helper.PasswordHashing
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.Models.Merchant
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.SplashScreen
import com.DevAsh.recwallet.Sync.SocketHelper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_amount_prompt.back
import kotlinx.android.synthetic.main.activity_amount_prompt.done

import kotlinx.android.synthetic.main.activity_password_prompt.*
import kotlinx.android.synthetic.main.activity_password_prompt.cancel
import kotlinx.android.synthetic.main.activity_password_prompt.password
import org.json.JSONObject
import java.text.DecimalFormat


class PasswordPrompt : AppCompatActivity() {

     var context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_prompt)
        RealmHelper.init(context)

        back.setOnClickListener{
            super.onBackPressed()
        }

        done.setOnClickListener{v->
            if(PasswordHashing.decryptMsg(DetailsContext.password!!)==password.text.toString()){
                needToPay = true
                hideKeyboardFrom(context,v)
                Handler().postDelayed({
                    transaction()
                },500)
            }else{
                println(PasswordHashing.decryptMsg(DetailsContext.password!!)+" actual password")
                println(password.text.toString()+" actual password")
                AlertHelper.showAlertDialog(this,"Incorrect Password !","The password you entered is incorrect, kindly check your password")
//                AlertHelper.showError(v,"Invalid Password")
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
                        transactionSuccessful()
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
                                                    else "Received",
                                                    transactionId =  transactionObjectArray.getJSONObject(i)["TransactionID"].toString()
                                                )
                                            )
                                        }
                                        StateContext.initAllTransaction(transactions)
                                    }
                                    override fun onError(anError: ANError?) {
                                        AlertHelper.showServerError(this@PasswordPrompt)
                                    }

                                })

                        }else{
                            AlertHelper.showAlertDialog(this@PasswordPrompt,
                                "Failed !",
                                "your transaction of ${TransactionContext.amount} ${TransactionContext.currency} is failed. if any amount debited it will refund soon",
                                object: AlertHelper.AlertDialogCallback {
                                    override fun onDismiss() {
                                        loadingScreen.visibility=View.INVISIBLE
                                        onBackPressed()
                                    }

                                    override fun onDone() {
                                        loadingScreen.visibility=View.INVISIBLE
                                        onBackPressed()
                                    }
                                }
                            )
                        }
                    }

                    override fun onError(anError: ANError?) {
                        loadingScreen.visibility= View.VISIBLE
                        AlertHelper.showAlertDialog(this@PasswordPrompt,
                            "Failed !",
                            "your transaction of ${TransactionContext.amount} ${TransactionContext.currency} is failed. if any amount debited it will refund soon",
                            object: AlertHelper.AlertDialogCallback {
                                override fun onDismiss() {
                                    loadingScreen.visibility=View.INVISIBLE
                                    onBackPressed()
                                }

                                override fun onDone() {
                                    loadingScreen.visibility=View.INVISIBLE
                                    onBackPressed()
                                }
                            }
                        )
                    }

                })
        }
    }

    fun transactionSuccessful(){
        val intent = Intent(this,Successful::class.java)
        intent.putExtra("type","transaction")
        intent.putExtra("amount",TransactionContext.amount)
        Handler().postDelayed({
            addRecent()
        },0)
        startActivity(intent)
        finish()
    }

    private fun addRecent(){

        Realm.getDefaultInstance().executeTransaction{realm->
            var freq = 0
            val isExist = realm.where(RecentContacts::class.java)
                .contains("number", TransactionContext.selectedUser?.number!!).findFirst()
            if(isExist==null){
                StateContext.addRecentContact(Merchant(TransactionContext.selectedUser?.name!!,
                    TransactionContext.selectedUser?.number!!,null))
            }else{
                freq = isExist.freq+1
                isExist.deleteFromRealm()
            }
            val recentContacts=RecentContacts(
                TransactionContext.selectedUser?.name,
                TransactionContext.selectedUser?.number,
                freq
            )
            realm.insertOrUpdate(recentContacts)
        }

    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
