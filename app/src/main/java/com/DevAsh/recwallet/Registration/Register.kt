package com.DevAsh.recwallet.Registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.RegistrationContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.jacksonandroidnetworking.JacksonParserFactory
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONArray


class Register : AppCompatActivity() {

    lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        Realm.init(this)
        super.onCreate(savedInstanceState)

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setParserFactory(JacksonParserFactory())

        setContentView(R.layout.activity_register)
        context=this

        phoneNumber.setText("+${RegistrationContext.countryCode}${RegistrationContext.phoneNumber}")

        done.setOnClickListener{view->
            val phoneNumebr = RegistrationContext.countryCode+RegistrationContext.phoneNumber
            val name = name.text.toString()
            val email = email.text.toString()
            val password = password.text.toString()
            val confirmPassword = confirmPassword.text.toString()

            if( phoneNumebr.length<10
                || name.isEmpty()
                || !email.contains("@")
                || !email.contains(".")
                || password.isEmpty()
                || confirmPassword.isEmpty())
            {
                SnackBarHelper.showError(view,"Invalid Credentials")
            }else if(password.length<8){
                SnackBarHelper.showError(view,"Password must contain at least 8 characters")
            }else if(password!=confirmPassword){
                SnackBarHelper.showError(view,"Password not match")
            }else{
                mainContent.visibility=INVISIBLE
                FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            SnackBarHelper.showError(view,"Registration Error")
                            return@OnCompleteListener
                        }else{
                            val fcmToken = task.result?.token
                            println(fcmToken)
                            AndroidNetworking.post(ApiContext.apiUrl+ ApiContext.registrationPort+"/addUser")
                                .addBodyParameter("name",name)
                                .addBodyParameter("email",email)
                                .addBodyParameter("number",RegistrationContext.countryCode+RegistrationContext.phoneNumber)
                                .addBodyParameter("password",password)
                                .addBodyParameter("fcmToken",fcmToken)
                                .setPriority(Priority.IMMEDIATE)
                                .build()
                                .getAsJSONArray(object:JSONArrayRequestListener {
                                    override fun onResponse(response: JSONArray?) {
                                        Realm.getDefaultInstance().executeTransaction { realm ->
                                            realm.delete(Credentials::class.java)
                                            val token =  response!!.getJSONObject(0)["token"].toString()
                                            val credentials = Credentials(name,phoneNumebr,email,password,token,true)
                                            realm.insert(credentials)
                                            DetailsContext.setData(
                                                credentials.name,
                                                credentials.phoneNumber,
                                                credentials.email,
                                                credentials.password,
                                                credentials.token
                                            )
                                            startActivity(Intent(context,HomePage::class.java))
                                            finish()
                                        }
                                    }
                                    override fun onError(error:ANError) {
                                        mainContent.visibility=VISIBLE
                                        Log.d("Auth",error.toString())
                                        SnackBarHelper.showError(view,error.errorDetail)
                                }
                            })
                        }
                    })



            }
        }
    }
}
