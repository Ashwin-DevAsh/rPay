package com.DevAsh.recwallet.Context

import com.DevAsh.recwallet.Home.Transactions.Contacts

object TransactionContext {
    var allUsers = ArrayList<Contacts>()
    var selectedUser:Contacts?=null
    var amount:String?=null
    var needToPay = false
}