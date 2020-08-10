package com.DevAsh.recwallet.Helper

import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Models.Contacts
import com.DevAsh.recwallet.Models.Merchant
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.SplashScreen
import org.json.JSONArray

object TransactionsHelper {
    fun addTransaction(transactionObjectArray:JSONArray):ArrayList<Transaction>{
        val transactions = ArrayList<Transaction>()
        for (i in 0 until transactionObjectArray!!.length()) {
            val name = if (transactionObjectArray.getJSONObject(i)["From"] == DetailsContext.id)
                transactionObjectArray.getJSONObject(i)["ToName"].toString()
            else transactionObjectArray.getJSONObject(i)["FromName"].toString()
            val number = if (transactionObjectArray.getJSONObject(i)["From"] == DetailsContext.id)
                transactionObjectArray.getJSONObject(i)["To"].toString()
            else transactionObjectArray.getJSONObject(i)["From"].toString()

            val contacts = Contacts(name, "+${number.split("@")[number.split("@").size-1]}","$number","")
            val transaction = Transaction(
                name = name,
                id = number,
                amount = transactionObjectArray.getJSONObject(i)["Amount"].toString(),
                time = SplashScreen.dateToString(
                    transactionObjectArray.getJSONObject(
                        i
                    )["TransactionTime"].toString()
                ),
                type = if (transactionObjectArray.getJSONObject(i)["From"] == DetailsContext.id)
                    "Send"
                else "Received",
                transactionId =  transactionObjectArray.getJSONObject(i)["TransactionID"].toString(),
                isGenerated = transactionObjectArray.getJSONObject(i).getBoolean("IsGenerated")
            )
            transactions.add(0, transaction)
            addRecent(transaction,contacts)
        }
        return transactions
    }

    private fun addRecent(transaction: Transaction, contacts: Contacts){
        println("entering "+transaction.id)
        if(!transaction.isGenerated && !transaction.id.contains("rbusiness")){
            println("entered "+transaction.id)
            StateContext.addRecentContact(contacts)
        }
    }




}