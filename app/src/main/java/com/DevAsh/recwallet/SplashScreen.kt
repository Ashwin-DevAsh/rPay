package com.DevAsh.recwallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.Registration.Login
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.jacksonandroidnetworking.JacksonParserFactory
import io.realm.Realm
import org.json.JSONObject
import java.text.DecimalFormat


class SplashScreen : AppCompatActivity() {

    lateinit var context: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        Realm.init(this)
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setParserFactory(JacksonParserFactory())
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)
        context = this

        val credentials:Credentials? =  Realm.getDefaultInstance().where(Credentials::class.java).findFirst()

        if(credentials?.isLogin==true){
            Handler().postDelayed({
                    DetailsContext.setData(
                        credentials!!.name,
                        credentials.phoneNumber,
                        credentials.email,
                        credentials.password,
                        credentials.token
                    )

                    println("token = "+credentials.token)

                    AndroidNetworking.get(ApiContext.apiUrl + ApiContext.paymentPort + "/getMyState?number=${DetailsContext.phoneNumber}")
                        .addHeaders("jwtToken",DetailsContext.token)
                        .setPriority(Priority.IMMEDIATE)
                        .build()
                        .getAsJSONObject(object: JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject?) {
                                val formatter = DecimalFormat("##,##,##,##,##,##,###")
                                StateContext.setBalanceToModel(formatter.format(response?.get(DetailsContext.phoneNumber).toString().toInt()))
                                TransactionContext.allTransactions.addAll(
                                    arrayOf(
                                        Transaction("USA Project", "993088909", "Jun 4 , 12:15 AM", "20,00,000", "Received"),
                                        Transaction("+6397567XXXXX", "993088909", "May 30 , 3:24 AM", "28,000", "Send"),
                                        Transaction("Food order", "993088909", "May 31 , 1:12 PM", "900", "Send"),
                                        Transaction("+6397570XXXXX", "993088909", "May 31 , 3:35 AM", "11,000", "Send"),
                                        Transaction("+6391830XXXXX", "993088909", "May 30 , 1:18 PM", "17,500", "Send"),
                                        Transaction("+6399419XXXXX", "993088909", "May 27 , 11:52 PM", "7,000", "Send"),
                                        Transaction("David", "993088909", "May 27 , 3:02 PM", "2,20,000", "Received"),
                                        Transaction("U.S Dealer", "993088909", "May 23 , 8:02 PM", "1,20,000", "Send"),
                                        Transaction("App client", "993088909", "May 21 , 11:00 PM", "3,50,000", "Received"),
                                        Transaction("Bank", "9930 8890 9780 1247", "May 15 , 11:00 PM", "65,000", "Send"),
                                        Transaction("+6399503XXXXX", "+639950357286", "May 5 , 9:30 PM", "11,750", "Send"),
                                        Transaction("Flight Book", "+06836547237", "April 15 , 12:30 AM", "39,000", "Send"),
                                        Transaction("Tobin", "+176814322900", "March 15 , 12:30 AM", "11,000", "Received"),
                                        Transaction("David", "+819885437880", "March 10 , 12:30 AM", "20,000", "Send")
                                    )
                                )
                                StateContext.initAllTransaction(TransactionContext.allTransactions)
                                startActivity(Intent(context,HomePage::class.java))
                                finish()
                            }

                            override fun onError(anError: ANError?) {
                                println(anError)
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
}
