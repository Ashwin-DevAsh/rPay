package com.DevAsh.recwallet.Context

import com.DevAsh.recwallet.Home.ViewModels.BalanceViewModel
import org.json.JSONObject

object StateContext {
    var state:JSONObject? = null
    val model: BalanceViewModel = BalanceViewModel()

    fun setBalanceToModel(amount: String){
         model.currentBalance.value = "₿ $amount"
    }

}