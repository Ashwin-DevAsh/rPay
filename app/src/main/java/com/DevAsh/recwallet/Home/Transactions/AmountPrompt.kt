package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_amount_prompt.*
import java.text.DecimalFormat

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

        done.setOnClickListener{v->
            TransactionContext.amount = amount.text.toString()
            try {
                if(TransactionContext.amount!!.toInt()>0){
                    startActivity(Intent(context,PasswordPrompt::class.java))
                    finish()
                }else{
                    SnackBarHelper.showError(v,"Invalid Amount")
                }
            }catch (e:Throwable){
                SnackBarHelper.showError(v,"Invalid Amount")
            }

        }
    }
}
