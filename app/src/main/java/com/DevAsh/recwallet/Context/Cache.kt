package com.DevAsh.recwallet.Context


import com.DevAsh.recwallet.Home.Transactions.TransactionsAdapter

object Cache {
    val singleObjectTransactionCache = HashMap<String,TransactionsAdapter>()
}