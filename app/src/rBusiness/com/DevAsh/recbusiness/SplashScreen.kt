package com.DevAsh.recwallet.rBusiness

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recbusiness.Home.Store.MyStore
import com.DevAsh.recbusiness.Registration.Login
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Database.BankAccount
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Database.RealmHelper
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.Helper.TransactionsHelper
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.rBusiness.Home.HomePage
import com.DevAsh.recwallet.util.NetworkUtil
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.firebase.messaging.FirebaseMessaging
import com.jacksonandroidnetworking.JacksonParserFactory
import io.realm.Realm
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import kotlin.system.exitProcess


class SplashScreen : AppCompatActivity() {

    lateinit var context: Context
    lateinit var parentLayout: View

    override fun onNewIntent(intent: Intent) {
        val extras = intent.extras

        super.onNewIntent(intent)

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setParserFactory(JacksonParserFactory())
        FirebaseMessaging.getInstance().subscribeToTopic("all")

        super.onCreate(savedInstanceState)
        context=this

        RealmHelper.init(this)
        setContentView(R.layout.activity_splash_screen)

        networkCallback()


        Handler().postDelayed({
            val networkStatus = NetworkUtil.getConnectivityStatusString(this)
            if (networkStatus == 0) {
                AlertHelper.showAlertDialog(
                    this,
                    "Network error",
                    "No internet connection available in this device",
                    object : AlertHelper.AlertDialogCallback {
                        override fun onDismiss() {
                            finishAffinity()
                        }

                        override fun onDone() {
                            finishAffinity()
                        }
                    })

            } else {
                startActivity(Intent(this, MyStore::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }

        }, 1500)

        return



        context = this
        parentLayout = findViewById(android.R.id.content)
        val credentials: Credentials? =  Realm.getDefaultInstance().where(Credentials::class.java).findFirst()
        if(credentials!=null && credentials.isLogin==true){
            StateContext.initRecentContact(arrayListOf())

            Handler().postDelayed({
                val bankAccounts =
                    Realm.getDefaultInstance().where(BankAccount::class.java).findAll()
                val bankAccountsTemp = ArrayList<com.DevAsh.recwallet.Models.BankAccount>()
                for (i in bankAccounts) {
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
            }, 0)

            Handler().postDelayed({
                try {

                    if (credentials == null) {
                        throw Exception()
                    }
                    Credentials.credentials = Realm.getDefaultInstance().copyFromRealm(credentials)
                } catch (e: Throwable) {
                    Handler().postDelayed({
                        startActivity(Intent(context, Login::class.java))
                        finish()
                    }, 2000)
                    return@postDelayed
                }

                Handler().postDelayed({
                    getStatus()
                }, 0)
                AndroidNetworking.get(ApiContext.profileSubDomain + "/init/${Credentials.credentials.id}")
                    .addHeaders("token", Credentials.credentials.token)
                    .setPriority(Priority.IMMEDIATE)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            val balance = response?.getInt("balance")
                            StateContext.currentBalance = balance!!
                            val formatter = DecimalFormat("##,##,##,##,##,##,##,###")
                            StateContext.setBalanceToModel(formatter.format(balance))
                            startActivity(Intent(context, HomePage::class.java))
                            Handler().postDelayed({
                                val transactionObjectArray =
                                    response.getJSONArray("transactions")
                                val transactions = TransactionsHelper.addTransaction(
                                    transactionObjectArray
                                )
                                StateContext.initAllTransaction(transactions)

                            }, 0)
                            finish()
                        }

                        override fun onError(anError: ANError?) {
                            AlertHelper.showServerError(this@SplashScreen)
                        }
                    })
            }, 0)
        }else{
            Handler().postDelayed({
                startActivity(Intent(context, Login::class.java))
                finish()
            }, 2000)
        }


    }

    companion object{
        fun dateToString(timeStamp: String):String{
            val time = timeStamp.subSequence(0, timeStamp.length - 3).split(" ")[1]
            try {
                val format = SimpleDateFormat("MM-dd-yyyy")
                val df2 = SimpleDateFormat("MMM dd")
                return df2.format(format.parse(timeStamp))+" "+time
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return timeStamp
        }

     fun getStatus(){
        AndroidNetworking.get(ApiContext.profileSubDomain + "/getMerchant?id=${Credentials.credentials.id}")
            .addHeaders("jwtToken", Credentials.credentials.token)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    try {
                        Credentials.credentials.isVerified =
                            response?.getString("status") == "active"
                    } catch (e: Throwable) {

                    }
                }

                override fun onError(anError: ANError?) {
                    println("err")
                    println(anError?.localizedMessage)
                }
            })
     }




    fun getDeliveredOrders(){
        AndroidNetworking.get(ApiContext.profileSubDomain + "/getMerchant?id=${Credentials.credentials.id}")
            .addHeaders("jwtToken", Credentials.credentials.token)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    try {
                        Credentials.credentials.isVerified =
                            response?.getString("status") == "active"
                    } catch (e: Throwable) {

                    }
                }

                override fun onError(anError: ANError?) {
                    println("err")
                    println(anError?.localizedMessage)
                }
            })
    }




    }


    fun networkCallback(){
        val networkCallback: NetworkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                // network available
            }

            override fun onLost(network: Network) {
                exitProcess(0)
//
//                AlertHelper.showAlertDialog(
//                    this@SplashScreen,
//                    "Network error",
//                    "No internet connection available in this device",
//                    object : AlertHelper.AlertDialogCallback {
//                        override fun onDismiss() {
//                            finishAffinity()
//                        }
//
//                        override fun onDone() {
//                        }
//                    })
            }
        }

        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }

    }

}
