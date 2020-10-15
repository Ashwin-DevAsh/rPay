package com.DevAsh.recwallet.Helper

import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Models.Contacts
import com.DevAsh.recwallet.Models.Merchant
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.SplashScreen
import org.json.JSONArray

object TransactionsHelper {

    var paymentObserver:PaymentObserver?=null
    var notificationObserver = HashMap<String,NotificationObserver>()

    fun addTransaction(transactionObjectArray:JSONArray):ArrayList<Transaction>{
        val transactions = ArrayList<Transaction>()
        for (i in 0 until transactionObjectArray.length()) {

            val from = transactionObjectArray.getJSONObject(i).getJSONObject("frommetadata")
            val to = transactionObjectArray.getJSONObject(i).getJSONObject("tometadata")
            val isSend = isSend(DetailsContext.id,from.getString("Id"))

            val name = if (isSend) to.getString("Name") else from.getString("Name")
            val number = if (isSend) to.getString("Number") else from.getString("Number")
            val email = if (isSend) to.getString("Email") else from.getString("Email")
            val id = if (isSend) to.getString("Id") else from.getString("Id")

            val contacts = Contacts(name, number,id,email)
            val transaction = Transaction(
                contacts=contacts,
                amount = transactionObjectArray.getJSONObject(i)["amount"].toString(),
                time = SplashScreen.dateToString(
                    transactionObjectArray.getJSONObject(
                        i
                    )["transactiontime"].toString()
                ),
                type = if (isSend)
                    "Send"
                else "Received",
                transactionId =  transactionObjectArray.getJSONObject(i)["transactionid"].toString(),
                isGenerated = transactionObjectArray.getJSONObject(i).getBoolean("isgenerated"),
                isWithdraw = transactionObjectArray.getJSONObject(i).getBoolean("iswithdraw")
            )
            transactions.add(0, transaction)
            addRecent(transaction,contacts)
        }

        return transactions
    }

    private fun addRecent(transaction: Transaction, contacts: Contacts){
        if(!transaction.isGenerated && !transaction.isWithdraw
            && !transaction.contacts.id.contains("rmart")
            && !transaction.contacts.id.contains("rbusiness")){
            StateContext.addRecentContact(contacts)
        }
    }

    fun isSend(myId:String,fromId:String):Boolean = myId == fromId
}

interface NotificationObserver{
    fun check()
}

interface PaymentObserver{
    fun update(transaction: Transaction)
}

