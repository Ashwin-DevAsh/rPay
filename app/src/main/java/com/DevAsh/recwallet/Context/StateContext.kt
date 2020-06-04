package com.DevAsh.recwallet.Context

import com.DevAsh.recwallet.Home.ViewModels.BalanceViewModel
import com.DevAsh.recwallet.Models.Transaction
import org.json.JSONObject

object StateContext {
    var state:JSONObject? = null

    val model: BalanceViewModel = BalanceViewModel()

    fun setBalanceToModel(amount: String){
         model.currentBalance.value = "₿ $amount"
    }

    fun initAllTransaction(updatedList:ArrayList<Transaction>){
        model.allTranactions.value=updatedList
    }

    fun addTransaction(transaction: Transaction){
        val oldTransaction =  model.allTranactions.value
        oldTransaction!!.add(0,transaction)
        model.allTranactions.value = oldTransaction
    }

}