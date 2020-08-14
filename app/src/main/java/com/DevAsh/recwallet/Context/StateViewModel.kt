package com.DevAsh.recwallet.Context

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.DevAsh.recwallet.Models.BankAccount
import com.DevAsh.recwallet.Models.Contacts
import com.DevAsh.recwallet.Models.Merchant
import com.DevAsh.recwallet.Models.Transaction

class StateViewModel: ViewModel() {
    var currentBalance = MutableLiveData<String>()
    var allTransactions = MutableLiveData<ArrayList<Transaction>>()
    var recentContacts = MutableLiveData<ArrayList<Contacts>>()
    var merchants = MutableLiveData<ArrayList<Merchant>>(ArrayList())
    var bankAccounts = MutableLiveData<ArrayList<BankAccount>>()
}
