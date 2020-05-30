package com.DevAsh.recwallet.Home.Transactions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_amount_prompt.*

class PasswordPrompt : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_prompt)

        back.setOnClickListener{
            super.onBackPressed()
        }
    }
}
