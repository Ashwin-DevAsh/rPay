package com.DevAsh.recwallet.Home.Transactions

import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Home.HomePage
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_successfull.*
import kotlinx.android.synthetic.main.activity_transaction_status.*
import kotlinx.android.synthetic.main.confirm_sheet.*
import kotlinx.android.synthetic.main.confirm_sheet.done


class Successful : AppCompatActivity() {
    lateinit var type:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_successfull)

        type = intent.getStringExtra("type")!!
        val amount = intent.getStringExtra("amount")
        if(type=="addMoney"){
            message.text = "The amount $amount ${TransactionContext.currency} was successfully added in your wallet"
        }else{
            image.setImageDrawable(getDrawable(R.drawable.transaction_successful))
            message.text = "Your transaction of $amount ${TransactionContext.currency} was successfully completed"
        }


        val ring: MediaPlayer = MediaPlayer.create(this, R.raw.success)
        ring.start()

        done.setOnClickListener{
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if(type=="addMoney"){
            startActivity(Intent(this,HomePage::class.java))
            finish()
        }else{
            super.onBackPressed()
        }

    }
}