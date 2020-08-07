package com.DevAsh.recwallet.Home.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Models.Merchant
import com.DevAsh.recwallet.Models.Transaction

class BalanceViewModel: ViewModel() {

    var currentBalance = MutableLiveData<String>()
    var allTransactions = MutableLiveData<ArrayList<Transaction>>()
    var recentContacts = MutableLiveData<ArrayList<Merchant>>()
    var merchants = MutableLiveData<ArrayList<Merchant>>(ArrayList())
    var isMerchantLoaded = MutableLiveData<Boolean>(false)

}
