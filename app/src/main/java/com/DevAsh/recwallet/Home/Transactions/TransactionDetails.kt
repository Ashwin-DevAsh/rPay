package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.DevAsh.recwallet.Context.*
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_transaction_status.*

class TransactionDetails : AppCompatActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_status)


        amount.text ="${TransactionContext.selectedTransaction?.amount}"
        selectedUserName.text = TransactionContext.selectedTransaction?.name
        type.text = if (TransactionContext.selectedTransaction?.type=="Send") "paid to" else "received from"
        badge.setBackgroundColor(Color.parseColor(TransactionContext.avatarColor))
        badge.text = TransactionContext.selectedTransaction?.name?.substring(0,1)
        badge.text = TransactionContext.selectedUser?.name.toString()[0].toString()

        subText.text = "${TransactionContext.selectedTransaction?.type}  ${TransactionContext.selectedTransaction?.amount} ${TransactionContext.currency}"

        transactionID.text = TransactionContext.selectedTransaction?.transactionId

        if (TransactionContext.selectedTransaction?.name.toString().startsWith("+")) {
           badge.text = TransactionContext.selectedTransaction?.name.toString().subSequence(1, 3)
           badge.textSize = 18F
        }

        context=this



    }
}
