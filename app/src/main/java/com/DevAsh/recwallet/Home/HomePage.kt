package com.DevAsh.recwallet.Home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Home.Transactions.AddMoney
import com.DevAsh.recwallet.Home.Transactions.SendMoney
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Sync.SocketService
import kotlinx.android.synthetic.main.activity_home_page.*


class HomePage : AppCompatActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        context = this

        startService(Intent(this, SocketService::class.java))

        val balanceObserver = Observer<String> { currentBalance ->
             balance.text = currentBalance
        }

        greetings.setText("Hii, "+DetailsContext.name)


        StateContext.model.currentBalance.observe(this,balanceObserver)


//        allActivities.setOnClickListener {
//            startActivity(Intent(context, AllTransactions::class.java))
//        }

        sendMoney.setOnClickListener {
            val permissions = arrayOf(android.Manifest.permission.READ_CONTACTS)
            if(packageManager.checkPermission(android.Manifest.permission.READ_CONTACTS,context.packageName)==PackageManager.PERMISSION_GRANTED ){
                startActivity(Intent(context, SendMoney::class.java))
            }else{
                ActivityCompat.requestPermissions(this, permissions,0)
            }

        }

        buyMoney.setOnClickListener{
            startActivity(Intent(this,AddMoney::class.java))
        }

        buyMoney.setOnLongClickListener{
//            StateContext.addFakeTransactions()
            return@setOnLongClickListener true
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
         if(requestCode==0){
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED ){
                    startActivity(Intent(context, SendMoney::class.java))
                }
        }
    }

}

