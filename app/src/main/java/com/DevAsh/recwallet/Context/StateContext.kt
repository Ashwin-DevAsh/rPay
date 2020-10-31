package com.DevAsh.recwallet.Context

import com.DevAsh.recwallet.Models.BankAccount
import com.DevAsh.recwallet.Models.Contacts
import com.DevAsh.recwallet.Models.Merchant
import com.DevAsh.recwallet.Models.Transaction

object StateContext {

    val model: StateViewModel = StateViewModel()
    var currentBalance = 0
    var timeIndex=0


    init {
        model.allTransactions.value = ArrayList()
    }

    fun setBalanceToModel(amount: String){
         model.currentBalance.value = "$amount ${HelperVariables.currency}"
    }

    fun initAllTransaction(initList:ArrayList<Transaction>){
        model.allTransactions.value = ArrayList(initList)
    }

    fun initRecentContact(arrayList:ArrayList<Contacts>){
        model.recentContacts.value=arrayList
    }

    fun initBankAccounts(arrayList: ArrayList<BankAccount>){
        model.bankAccounts.value = arrayList
    }

    fun addBankAccounts(bankAccount: BankAccount){
        if(model.bankAccounts.value!=null){
            model.bankAccounts.value?.add(bankAccount)
        }else{
            model.bankAccounts.value= arrayListOf(bankAccount)
        }
    }

    fun addRecentContact(contact: Contacts){
        val temp:ArrayList<Contacts> = if(model.recentContacts.value!=null) model.recentContacts.value!! else ArrayList()
        if(!temp.contains(contact)){
            temp.add(0,contact)
            model.recentContacts.value=temp
        }else{
            temp.remove(contact)
            temp.add(0,contact)
            model.recentContacts.value=temp
        }
    }

    fun initMerchant(arrayList: ArrayList<Merchant>){
        model.merchants.value=arrayList
    }
}