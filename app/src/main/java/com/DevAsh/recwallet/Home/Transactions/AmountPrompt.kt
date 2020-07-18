package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_amount_prompt.*

class AmountPrompt : AppCompatActivity() {

    lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amount_prompt)

        context=this

        paymentText.text = "Paying ${TransactionContext.selectedUser?.name}"
        paymentBrief.text = "Your transaction to ${TransactionContext.selectedUser?.number} will verify"

        back.setOnClickListener{
            super.onBackPressed()
        }

        cancel.setOnClickListener{
            super.onBackPressed()
        }

        done.setOnClickListener{v->

            TransactionContext.amount = amount.text.toString()
            if(TransactionContext.amount==""){
                return@setOnClickListener
            }
            if(TransactionContext.amount!!.toInt()>StateContext.currentBalance){
                AlertHelper.showError("Insufficient Balance !", this)
                return@setOnClickListener
            }

            try {
               if(TransactionContext.amount!!.toInt()>0){
                    startActivity(Intent(context,PasswordPrompt::class.java))
                    finish()
                }else{
                   AlertHelper.showError("Invalid Amount", this)
                }
            }catch (e:Throwable){
                AlertHelper.showError("Invalid Amount", this)
            }

        }
    }
}
