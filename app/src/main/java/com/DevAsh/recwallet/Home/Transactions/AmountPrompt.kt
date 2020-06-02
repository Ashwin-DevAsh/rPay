package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_amount_prompt.*

class AmountPrompt : AppCompatActivity() {

    lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amount_prompt)

        context=this

        paymentText.text = "Paying ${TransactionContext.selectedUser?.name?.replace(" ","")}"
        paymentBrief.text = "Your transaction to ${TransactionContext.selectedUser?.number} will verify"

        back.setOnClickListener{
            super.onBackPressed()
        }

        done.setOnClickListener{
            TransactionContext.amount = amount.text.toString()
            startActivity(Intent(context,PasswordPrompt::class.java))
        }
    }
}
