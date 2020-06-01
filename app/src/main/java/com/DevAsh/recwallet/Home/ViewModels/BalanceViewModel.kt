package com.DevAsh.recwallet.Home.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext

class BalanceViewModel: ViewModel() {

    var currentBalance = MutableLiveData<String>()

    fun setBalance(){
        currentBalance.value = StateContext.state?.getString(DetailsContext.phoneNumber!!)
    }

}
