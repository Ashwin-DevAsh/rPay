package com.DevAsh.recwallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.Registration.Login
import io.realm.Realm


class SplashScreen : AppCompatActivity() {

    lateinit var context: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        Realm.init(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)
        context = this

        val credentials:Credentials? =  Realm.getDefaultInstance().where(Credentials::class.java).findFirst()
        DetailsContext.credentials = credentials


         Handler().postDelayed(
             {
                 if(credentials?.isLogin==true)
                     startActivity(Intent(context,HomePage::class.java))
                 else
                     startActivity(Intent(context,Login::class.java))
                 finish()
             },
             2000
         )

    }
}
