package com.DevAsh.recwallet.Home.Transactions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.DevAsh.recwallet.R
import com.instamojo.android.Instamojo

class InstamojoActivity : AppCompatActivity(),  Instamojo.InstamojoPaymentCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instamojo)
        Instamojo.getInstance().initialize(this, Instamojo.Environment.PRODUCTION)
        Instamojo.getInstance().initiatePayment(this, "orderID", this)
    }

    override fun onInstamojoPaymentComplete(p0: String?, p1: String?, p2: String?, p3: String?) {
        Toast.makeText(this,"Payment completed",Toast.LENGTH_SHORT).show()

    }

    override fun onPaymentCancelled() {
        Toast.makeText(this,"Payment cancled",Toast.LENGTH_SHORT).show()
    }

    override fun onInitiatePaymentFailure(p0: String?) {
        Toast.makeText(this,"Payment failed",Toast.LENGTH_SHORT).show()
    }
}