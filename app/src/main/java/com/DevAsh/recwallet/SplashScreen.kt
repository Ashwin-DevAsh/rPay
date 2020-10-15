package com.DevAsh.recwallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.HelperVariables
import com.DevAsh.recwallet.Database.BankAccount
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Database.RealmHelper
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.Helper.TransactionsHelper
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.Models.Merchant
import com.DevAsh.recwallet.Registration.Login
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.firebase.messaging.FirebaseMessaging
import com.jacksonandroidnetworking.JacksonParserFactory
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat


class SplashScreen : AppCompatActivity() {

    lateinit var context: Context
    lateinit var parentLayout: View


    override fun onCreate(savedInstanceState: Bundle?) {

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setParserFactory(JacksonParserFactory())
        FirebaseMessaging.getInstance().subscribeToTopic("all")

        super.onCreate(savedInstanceState)
        RealmHelper.init(this)

        setContentView(R.layout.activity_splash_screen)
        context = this
        parentLayout = findViewById(android.R.id.content)


        val credentials:Credentials? =  Realm.getDefaultInstance().where(Credentials::class.java).findFirst()


        if(credentials!=null && credentials.isLogin==true){
            StateContext.initRecentContact(arrayListOf())

            Handler().postDelayed({
                val bankAccounts= Realm.getDefaultInstance().where(BankAccount::class.java).findAll()
                val bankAccountsTemp = ArrayList<com.DevAsh.recwallet.Models.BankAccount>()
                for(i in bankAccounts){
                bankAccountsTemp.add(
                        com.DevAsh.recwallet.Models.BankAccount(
                            holderName = i.holderName,
                            bankName = i.bankName,
                            IFSC = i.IFSC,
                            accountNumber = i.accountNumber
                        )
                    )
                }
                StateContext.initBankAccounts(bankAccountsTemp)
            },0)


            Handler().postDelayed({
                  try {
                      DetailsContext.setData(
                          credentials.name,
                          credentials.phoneNumber,
                          credentials.email,
                          credentials.password,
                          credentials.token
                      )
                  }catch (e:Throwable){
                      Handler().postDelayed({
                          startActivity(Intent(context,Login::class.java))
                          finish()
                      },2000)
                      return@postDelayed
                  }
                  AndroidNetworking.get(ApiContext.apiUrl + ApiContext.profilePort + "/init/${DetailsContext.id}")
                        .addHeaders("token",DetailsContext.token)
                        .setPriority(Priority.IMMEDIATE)
                        .build()
                        .getAsJSONObject(object: JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject?) {
                                val balance = response?.getInt("balance")
                                val merchants = response?.getJSONArray("merchants")!!
                                StateContext.currentBalance = balance!!
                                val formatter = DecimalFormat("##,##,##,##,##,##,##,###")
                                StateContext.setBalanceToModel(formatter.format(balance))
                                Handler().postDelayed({
                                    getMerchants(merchants)
                                },0)
                                Handler().postDelayed({
                                    val transactionObjectArray = response.getJSONArray("transactions")
                                    println(transactionObjectArray)
                                    StateContext.initAllTransaction(TransactionsHelper.addTransaction(transactionObjectArray))
                                },0)

                            }
                            override fun onError(anError: ANError?) {
                                  AlertHelper.showServerError(this@SplashScreen)
                            }
                        })
            },0)
        }else{
            Handler().postDelayed({
                startActivity(Intent(context,Login::class.java))
                finish()
            },2000)
        }


    }

    fun getMerchants(response:JSONArray){
        val merchantTemp = ArrayList<Merchant>()
        for(i in 0 until response.length()){
            val user = Merchant(
                response.getJSONObject(i)["storename"].toString()
                ,"+"+response.getJSONObject(i)["number"].toString()
                ,response.getJSONObject(i)["id"].toString()
                ,response.getJSONObject(i)["email"].toString()
            )
            merchantTemp.add(user)
        }
        StateContext.initMerchant(merchantTemp)
        startActivity(Intent(context, HomePage::class.java))
        finish()
    }

    companion object{
        fun dateToString(timeStamp:String):String{
            val time = timeStamp.subSequence(0,timeStamp.length-3).split(" ")[1]
            try {
                val format = SimpleDateFormat("MM-dd-yyyy")
                val df2 = SimpleDateFormat("MMM dd")
                return df2.format(format.parse(timeStamp))+" "+time
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return timeStamp
        }
    }


}
