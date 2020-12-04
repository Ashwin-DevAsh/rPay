package com.DevAsh.recbusiness.Registration

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
import com.DevAsh.recwallet.Context.RegistrationContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.Helper.PasswordHashing
import com.DevAsh.recwallet.Helper.TransactionsHelper
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.rBusiness.Home.HomePage
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.jacksonandroidnetworking.JacksonParserFactory
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.cancel
import kotlinx.android.synthetic.main.activity_register.confirmPassword
import kotlinx.android.synthetic.main.activity_register.done
import kotlinx.android.synthetic.main.activity_register.email
import kotlinx.android.synthetic.main.activity_register.mainContent
import kotlinx.android.synthetic.main.activity_register.name
import kotlinx.android.synthetic.main.activity_register.password
import kotlinx.android.synthetic.main.activity_register.phoneNumber
import kotlinx.android.synthetic.rBusiness.activity_register.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.regex.Pattern


class Register : AppCompatActivity() {

    lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        Realm.init(this)
        super.onCreate(savedInstanceState)
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setParserFactory(JacksonParserFactory())
        setContentView(R.layout.activity_register)
        context=this

        phoneNumber.text = "+${RegistrationContext.countryCode}${RegistrationContext.phoneNumber}"
        cancel.setOnClickListener{
           finishAffinity()
        }

        done.setOnClickListener{view->
            val phoneNumber = RegistrationContext.countryCode+RegistrationContext.phoneNumber
            val name = name.text.toString()
            val email = email.text.toString()
            val password = password.text.toString()
            val confirmPassword = confirmPassword.text.toString()
            val storeName = storeName.text.toString()

            if( phoneNumber.length<10
                || name.length<2
                || email.length<8
                || !email.contains("@")
                || !email.contains(".")
                || password.isEmpty()
                || confirmPassword.isEmpty())
            {
                AlertHelper.showError("Invalid Credentials", this)
            }else if( checkUserName(name)){
                AlertHelper.showError("Username should not contain symbols or numbers", this)
            }else if(name.split(" ").size>4){
                AlertHelper.showError("Username should not contain more white space", this)
            }
            else if(password.length<8){
                AlertHelper.showError("Password must contain at least 8 characters", this)
            }else if(password!=confirmPassword){
                AlertHelper.showError("Password not match", this)
            }else{
                hideKeyboardFrom(context,view)
                Handler().postDelayed({
                    mainContent.visibility = INVISIBLE
                },300)
                val jwt = Jwts.builder()
                    .claim("name", storeName)
                    .claim("number", RegistrationContext.countryCode+RegistrationContext.phoneNumber)
                    .claim("email",email)
                    .claim("id", "rbusiness@${RegistrationContext.countryCode+RegistrationContext.phoneNumber}")
                    .signWith(SignatureAlgorithm.HS256, ApiContext.qrKey)
                    .compact()
                    AndroidNetworking.post(ApiContext.profileSubDomain+"/addMerchant")
                        .addBodyParameter("name",name)
                        .addBodyParameter("email",email)
                        .addBodyParameter("number",RegistrationContext.countryCode+RegistrationContext.phoneNumber)
                        .addBodyParameter("password", PasswordHashing.encryptMsg(password))
                        .addBodyParameter("fcmToken","fcmToken")
                        .addBodyParameter("qrCode",jwt)
                        .addBodyParameter("storeName",storeName)
                        .setPriority(Priority.IMMEDIATE)
                        .build()
                        .getAsJSONArray(object:JSONArrayRequestListener {
                            override fun onResponse(response: JSONArray?) {
                               println(response)
                                try{
                                    if( response!!.getJSONObject(0)["message"].toString()=="User already exist"){
                                        mainContent.visibility=VISIBLE
                                        AlertHelper.showError("phone number or email already exist",this@Register)
                                        return
                                    }
                                }catch (e:Throwable){

                                }
                                try{
                                    if( response!!.getJSONObject(0)["message"].toString()=="failed"){
                                        mainContent.visibility=VISIBLE
                                        AlertHelper.showError("Failed !",this@Register)
                                        return
                                    }
                                }catch (e:Throwable){

                                }

                                Realm.getDefaultInstance().executeTransaction { realm ->
                                    realm.delete(Credentials::class.java)
                                    val token =  response!!.getJSONObject(0)["token"].toString()
                                   val credentials = Credentials(
                                        true,
                                    storeName,
                                        name,
                                        phoneNumber,
                                        email,
                                        PasswordHashing.
                                        encryptMsg(password),
                                        token,
                                        "Pending"
                                    )
                                    realm.insert(credentials)
                                    Credentials.credentials =
                                        Realm.getDefaultInstance()
                                            .copyFromRealm(Realm.getDefaultInstance().where(Credentials::class.java).findFirst())



                                    Handler().postDelayed({
                                        AndroidNetworking.get(ApiContext.profileSubDomain + "/init/${Credentials.credentials.id}")
                                            .addHeaders("token",Credentials.credentials.token)
                                            .setPriority(Priority.IMMEDIATE)
                                            .build()
                                            .getAsJSONObject(object:
                                                JSONObjectRequestListener {
                                                override fun onResponse(response: JSONObject?) {
                                                    val balance = response?.getInt("balance")
                                                    StateContext.currentBalance = balance!!
                                                    val formatter = DecimalFormat("##,##,##,##,##,##,##,###")
                                                    StateContext.setBalanceToModel(formatter.format(balance))
                                                    startActivity(Intent(context, HomePage::class.java))
                                                    Handler().postDelayed({
                                                        val transactionObjectArray = response.getJSONArray("transactions")
                                                        val transactions = TransactionsHelper.addTransaction(transactionObjectArray)
                                                        StateContext.initAllTransaction(transactions)
                                                    },0)
                                                    finish()
                                                }
                                                override fun onError(anError: ANError?) {
                                                    AlertHelper.showServerError(this@Register)
                                                }

                                            })
                                    },0)

                                }
                            }
                            override fun onError(error:ANError) {
                                mainContent.visibility=VISIBLE
                                AlertHelper.showServerError(this@Register)

                            }
                    })
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

    private fun checkUserName(name:String):Boolean{
        val p = Pattern.compile("[^A-Za-z_\\s]+")
        val m = p.matcher(name)
        return m.find()
    }


}
