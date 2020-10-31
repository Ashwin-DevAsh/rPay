package com.DevAsh.recwallet.rMart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Database.RealmHelper
import com.DevAsh.recwallet.R
import io.realm.Realm


class GetAccess : AppCompatActivity() {

    companion object{
        var amount:String?=null
    }

    private var accessSheet = AccessSheet(
        object : CallBack {
            override fun deny() {
                Handler().postDelayed({
                    finish()
                }, 1000)
            }

            override fun allow() {
                Handler().postDelayed({
                    sendAccessResult()
                }, 1000)
            }
        }
    )

    private var passwordSheet = PasswordSheet(object : CallBack {
        override fun deny() {
            Handler().postDelayed({
                finish()
            }, 1000)
        }

        override fun allow() {
            Handler().postDelayed({
                sendPasswordResult()
            }, 1000)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RealmHelper.init(context = this)
        loadData()
        setContentView(R.layout.activity_get_access)
        val getAccess = intent.getBooleanExtra("getAccess",false)
        if(getAccess){
            openAccessSheet()
        }else{
            amount = intent.getStringExtra("amount")!!
            openPasswordSheet()
        }
    }

    private fun loadData(){
        try {
            Credentials.credentials = Realm.getDefaultInstance().where(Credentials::class.java).findFirst()!!
        }catch (e:Throwable){
              finish()
        }

    }

    private fun openAccessSheet(){
        Handler().postDelayed({
            accessSheet.show(supportFragmentManager, "tag")
        }, 1000)
    }

    private fun openPasswordSheet(){
        Handler().postDelayed({
            passwordSheet.show(supportFragmentManager, "tag")
        }, 1000)
    }

    fun sendAccessResult(){
        val resultIntent = Intent()
        resultIntent.putExtra("result", true)
        resultIntent.putExtra("name", Credentials.credentials.accountName)
        resultIntent.putExtra("email", Credentials.credentials.email)
        resultIntent.putExtra("phoneNumber", Credentials.credentials.phoneNumber)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun sendPasswordResult(){
        val resultIntent = Intent()
        resultIntent.putExtra("result", true)
        resultIntent.putExtra("name", Credentials.credentials.accountName)
        resultIntent.putExtra("email", Credentials.credentials.email)
        resultIntent.putExtra("phoneNumber", Credentials.credentials.phoneNumber)
        resultIntent.putExtra("token",Credentials.credentials.token)
        resultIntent.putExtra("password",Credentials.credentials.password)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}

interface CallBack{
    fun deny()
    fun allow()
}