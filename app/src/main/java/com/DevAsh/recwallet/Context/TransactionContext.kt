package com.DevAsh.recwallet.Context

import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.Home.Transactions.Contacts

object TransactionContext {
    var allTransactions = ArrayList<Transaction>()
    var allUsers = ArrayList<Contacts>()
    var selectedUser:Contacts?=null
    var amount:String?=null
}