package com.DevAsh.recwallet.Context


import android.app.Activity
import com.DevAsh.recwallet.Home.Transactions.TransactionsAdapter

object Cache {
    val singleObjectTransactionCache = HashMap<String,TransactionsAdapter>()
    val socketListnerCache = HashMap<Activity,String>()
}