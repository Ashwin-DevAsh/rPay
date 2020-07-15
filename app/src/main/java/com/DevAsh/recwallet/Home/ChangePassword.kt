package com.DevAsh.recwallet.Home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_change_password.*


class ChangePassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        done.setOnClickListener{v ->
            if(oldPassword.text.length<8 || password.text.length<8 || confirmPassword.text.length<8){
                SnackBarHelper.showError(v,"Invalid Credentials")
            }else if(oldPassword.text == password.text){
                SnackBarHelper.showError(v,"old password must not be new password")
            }else if(password.text != confirmPassword.text){
                SnackBarHelper.showError(v,"password not matching")
            }else{

            }
        }

        cancel.setOnClickListener{
            onBackPressed()
        }
    }
}