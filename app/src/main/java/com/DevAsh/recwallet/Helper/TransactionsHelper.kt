package com.DevAsh.recwallet.Helper

import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Models.Contacts
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
            val isSend = isSend(Credentials.credentials.id,from.getString("id"))

            val name = if (isSend) to.getString("name") else from.getString("name")
            val number = if (isSend) to.getString("number") else from.getString("number")
            val email = if (isSend) to.getString("email") else from.getString("email")
            val id = if (isSend) to.getString("id") else from.getString("id")

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
                isWithdraw = transactionObjectArray.getJSONObject(i).getBoolean("iswithdraw"),
                timeStamp = transactionObjectArray.getJSONObject(i).getString("timestamp")

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

