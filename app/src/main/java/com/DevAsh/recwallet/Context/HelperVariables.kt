package com.DevAsh.recwallet.Context

import com.DevAsh.recwallet.Models.BankAccount
import com.DevAsh.recwallet.Models.Contacts
import com.DevAsh.recwallet.Models.Transaction

object HelperVariables {
    var allUsers = ArrayList<Contacts>()

    var selectedUser:Contacts?=null

    var amount:String?=null

    var needToPay = false

    var avatarColor = "#035aa6"

    var currency = "RC"

    var selectedTransaction:Transaction?=null

    var openTransactionPage = false

    var selectedAccount:BankAccount?=null
}