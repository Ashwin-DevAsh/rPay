package com.DevAsh.recwallet.Home

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewParent
import android.view.inputmethod.InputMethodManager
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.RegistrationContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Database.RealmHelper
import com.DevAsh.recwallet.Helper.PasswordHashing
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.DevAsh.recwallet.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_change_password.*
import org.json.JSONObject


class ChangePassword : AppCompatActivity() {
    lateinit var oldPasswordText:String
    lateinit var newPasswordText:String
    var newHashedPassword:String? = null
    lateinit var parentView:View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RealmHelper.init(context = this)
        setContentView(R.layout.activity_change_password)
        parentView = findViewById(R.id.mainContent)
        done.setOnClickListener{v ->
            oldPasswordText = oldPassword.text.toString()
            newPasswordText = password.text.toString()
            if(oldPassword.text.length<8 || password.text.length<8 || confirmPassword.text.length<8){
                SnackBarHelper.showError(v,"Invalid Credentials")
            }else if(oldPassword.text.toString() == password.text.toString()){
                SnackBarHelper.showError(v,"old password must not be new password")
            }else if(oldPassword.text.toString() != PasswordHashing.decryptMsg(DetailsContext.password!!)){
                SnackBarHelper.showError(v,"Invalid old password")
            }else if(password.text.toString() != confirmPassword.text.toString()){
                SnackBarHelper.showError(v,"password not matching")
            }else{
               changePassword(v)
            }
        }

        cancel.setOnClickListener{
            onBackPressed()
        }
    }


    private fun changePassword(view: View){
        hideKeyboardFrom(context = this,view = view)
        Handler().postDelayed({
            mainContent.visibility=View.INVISIBLE
        },500)
        newHashedPassword = PasswordHashing.encryptMsg(newPasswordText)
        AndroidNetworking.post(ApiContext.apiUrl+ ApiContext.registrationPort+"/changePassword")
            .addHeaders("token",DetailsContext.token)
            .addBodyParameter(object{
                val id = DetailsContext.phoneNumber
                val oldPassword = PasswordHashing.encryptMsg(oldPasswordText)
                val newPassword = newHashedPassword
            })
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object:JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    if(response?.getString("message")=="Done"){
                          updateDatabase(newHashedPassword!!)
                        finish()
                        SnackBarHelper.showError(parentView,"Password Changed Successfully")

                    }else{
                        SnackBarHelper.showError(parentView,"Failed")
                    }
                    mainContent.visibility=View.VISIBLE

                }

                override fun onError(anError: ANError?) {
                    mainContent.visibility=View.VISIBLE
                    SnackBarHelper.showError(parentView,"Failed")
                    println(anError)

                }
            })
    }

    fun updateDatabase(newPassword:String){
        Realm.getDefaultInstance().executeTransaction{r->
            val data = r.where(Credentials::class.java).findFirst()
            data?.password = newPassword
        }
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}