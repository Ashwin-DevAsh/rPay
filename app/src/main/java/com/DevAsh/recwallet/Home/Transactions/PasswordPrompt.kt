package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_amount_prompt.back
import kotlinx.android.synthetic.main.activity_amount_prompt.done
import kotlinx.android.synthetic.main.activity_password_prompt.*

class PasswordPrompt : AppCompatActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_prompt)

        context=this

        back.setOnClickListener{
            super.onBackPressed()
        }

        done.setOnClickListener{
            startActivity(Intent(context,TransactionStatus::class.java))
            finish()
        }

        cancel.setOnClickListener{
            super.onBackPressed()
        }
    }

    override fun onBackPressed() {

    }
}
