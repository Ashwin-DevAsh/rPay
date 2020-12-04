package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.DevAsh.recwallet.Context.*
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.Helper.NotificationObserver
import com.DevAsh.recwallet.Helper.PaymentObserver
import com.DevAsh.recwallet.Helper.TransactionsHelper
import com.DevAsh.recwallet.Models.Contacts
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.SplashScreen
import com.DevAsh.recwallet.Sync.SocketHelper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.activity_single_object_transaction.*

import org.json.JSONObject
import java.lang.Exception
import java.sql.Timestamp

class SingleObjectTransaction : AppCompatActivity() {

    var allActivityAdapter: TransactionsAdapter?=null
    lateinit var smoothScroller:SmoothScroller


    var needToScroll = false
    var transaction = ArrayList<Transaction>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(intent!!.getBooleanExtra("fromNotification",false)){
            HelperVariables.selectedUser = Contacts.fromIntent(intent)
            println( HelperVariables.selectedUser)
        }

        SocketHelper.connect()
        setContentView(R.layout.activity_single_object_transaction)
        avatarContainer.setBackgroundColor(Color.parseColor(HelperVariables.avatarColor))
        Cache.socketListnerCache[this] = HelperVariables.selectedUser!!.id
        smoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_END
            }
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return 250f / displayMetrics.densityDpi
            }
        }

        loadAvatar()



        badge.text = HelperVariables.selectedUser!!.name[0].toString()

        try {
            if(!intent.getBooleanExtra("openSingleObjectTransactions",false)){
                allActivityAdapter = Cache.singleObjectTransactionCache[HelperVariables.selectedUser!!.id]!!
                val layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
                layoutManager.stackFromEnd=true
                transactionContainer.layoutManager = layoutManager
                transactionContainer.adapter = allActivityAdapter
                transaction = allActivityAdapter!!.items
                Handler().postDelayed({
                    getData()
                },0)
                loadingScreen.visibility=View.INVISIBLE
            }else{
                throw Exception()
            }
        }catch (e:Throwable){
            e.printStackTrace()
            Handler().postDelayed({
                getData()
            },0)
        }

        if (HelperVariables.selectedUser!!.name.startsWith("+")) {
            badge.text = HelperVariables.selectedUser!!.name.subSequence(1, 3)
            badge.textSize = 18F
        }
        name.text = HelperVariables.selectedUser!!.name
        number.text = HelperVariables.selectedUser!!.number

        back.setOnClickListener{
            super.onBackPressed()
        }

        sendMessage.setOnClickListener{
            try{
                sendMessage()
            }catch (e:Throwable){
                e.printStackTrace()
            }
        }

        TransactionsHelper.notificationObserver[HelperVariables.selectedUser!!.id] = object:NotificationObserver{
            override fun check() {
                try {
                    runOnUiThread {
                        getData()
                    }
                }catch (e:Throwable){
                    e.printStackTrace()
                }
            }
        }

        pay.setOnClickListener{
            TransactionsHelper.paymentObserver=object : PaymentObserver{
                override fun update(transaction: Transaction) {
                    val objectTransactions=transaction
                    needToScroll=true
                    if(!this@SingleObjectTransaction.transaction.contains(objectTransactions)){
                        this@SingleObjectTransaction.transaction.add(objectTransactions)
                        allActivityAdapter?.updateList(this@SingleObjectTransaction.transaction,transactionContainer)
                    }
                }

            }
            startActivity(Intent(this,AmountPrompt::class.java))
        }
    }

    override fun onResume() {
        if(needToScroll){
            needToScroll=false
            smoothScroller.targetPosition = transaction.size
            (transactionContainer.layoutManager as RecyclerView.LayoutManager).startSmoothScroll(smoothScroller)
        }
        super.onResume()
    }



    private fun loadAvatar(){
        UiContext.loadProfileImage(this,HelperVariables.selectedUser?.id!!,object:LoadProfileCallBack{
            override fun onSuccess() {
                if(!HelperVariables.selectedUser?.id!!.contains("rpay")){
                    profile.setBackgroundColor( this@SingleObjectTransaction.resources.getColor(R.color.textDark))
                    profile.setColorFilter(Color.WHITE,  android.graphics.PorterDuff.Mode.SRC_IN)
                    profile.setPadding(35,35,35,35)
                }
                avatarContainer.visibility=View.GONE
                profile.visibility = View.VISIBLE
            }

            override fun onFailure() {
                avatarContainer.visibility= View.VISIBLE
                profile.visibility = View.GONE
            }
        },profile)
    }




    private fun sendMessage(){
        val messageText = messageEditText.text.toString()
        if (messageText.isNotEmpty()){
            val transactionObject =    Transaction(
                contacts = HelperVariables.selectedUser!!,
                type = "Send",
                time = Timestamp(System.currentTimeMillis()).toString(),
                amount = "0",
                isGenerated = false,
                isWithdraw = false,
                transactionId = "0",
                timeStamp = Timestamp(System.currentTimeMillis()).toString()
            )
            transactionObject.message = messageText
            transaction.add(transactionObject)

            messageEditText.setText("")
            allActivityAdapter?.updateList(transaction,transactionContainer)
            smoothScroller.targetPosition = transaction.size
            (transactionContainer.layoutManager as RecyclerView.LayoutManager).startSmoothScroll(smoothScroller)


            AndroidNetworking.post(ApiContext.paymentSubDomain + "/sendMessage")
                .setContentType("application/json; charset=utf-8")
                .addHeaders("token", Credentials.credentials.token)
                .addApplicationJsonBody(object{
                    var from = object{
                        var id = Credentials.credentials.id
                        var name = Credentials.credentials.accountName
                        var number = Credentials.credentials.phoneNumber
                        var email = Credentials.credentials.email
                    }
                    var to = object {
                        var id =  HelperVariables.selectedUser?.id
                        var name =  HelperVariables.selectedUser?.name
                        var number =  HelperVariables.selectedUser?.number
                        var email =  HelperVariables.selectedUser?.email
                    }
                    var message = messageText
                })
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(object :JSONObjectRequestListener{
                    override fun onResponse(response: JSONObject?) {
                        println(response)
                    }

                    override fun onError(anError: ANError?) {
                        println(anError?.errorCode)
                    }

                })
        }


    }


    private fun getData(){
        Handler().postDelayed({
            AndroidNetworking.get(
               ApiContext.paymentSubDomain + "/getTransactionsBetweenObjects")
                .addHeaders("token", Credentials.credentials.token)
                .addQueryParameter("id1",Credentials.credentials.id)
                .addQueryParameter("id2",HelperVariables.selectedUser!!.id)
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(object: JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if(response?.getString("message")!="done"){
                            throw Exception("Failed")
                        }

                        val transactions = ArrayList<Transaction>()

                        val transactionObjectArray = response.getJSONArray("transactions")
                        for (i in 0 until transactionObjectArray.length()) {
                            val transaction = transactionObjectArray.getJSONObject(i)
                            val from = transaction.getJSONObject("frommetadata")
                            val to = transaction.getJSONObject("tometadata")
                            val isSend = TransactionsHelper.isSend(Credentials.credentials.id, from.getString("id"))
                            val name = if (isSend) to.getString("name") else from.getString("name")
                            val number = if (isSend) to.getString("number") else from.getString("number")
                            val email = if (isSend) to.getString("email") else from.getString("email")
                            val id = if (isSend) to.getString("id") else from.getString("id")
                            val contacts = Contacts(name, number,id,email)
                            val transactionObject =   Transaction(
                                contacts = contacts,
                                amount = transaction["amount"].toString(),
                                time =(if (isSend)
                                    "Paid  "
                                else "Received  ")+ SplashScreen.dateToString(
                                    transaction["transactiontime"].toString()
                                ),
                                type = if (isSend)
                                    "Send"
                                else "Received",
                                transactionId =  transaction["transactionid"].toString(),
                                isGenerated = transaction.getBoolean("isgenerated"),
                                isWithdraw = transaction.getBoolean("iswithdraw"),
                                timeStamp = transaction.getString("timestamp")
                            )
                            transactionObject.message = transaction.getString("message")
                            transactions.add(transactionObject)
                        }

                        if(transactions.size!=allActivityAdapter?.itemCount) {
                            transaction = transactions
                            val layoutManager =  LinearLayoutManager(this@SingleObjectTransaction,LinearLayoutManager.VERTICAL,false)
                            layoutManager.stackFromEnd=true
                            transactionContainer.layoutManager =layoutManager
                            allActivityAdapter = TransactionsAdapter(transaction, this@SingleObjectTransaction)
                            transactionContainer.adapter = allActivityAdapter

                            Cache.singleObjectTransactionCache[HelperVariables.selectedUser!!.id.replace("+","")] = allActivityAdapter!!
                            Handler().postDelayed({
                                loadingScreen.visibility = View.INVISIBLE
                            },300)
                        }
                    }

                    override fun onError(anError: ANError?) {
                        anError?.printStackTrace()
                        AlertHelper.showServerError(this@SingleObjectTransaction)
                    }

                })
        },0)
    }

}

class TransactionsAdapter(var items : ArrayList<Transaction>, val context: Context) : RecyclerView.Adapter<TransactionsViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        return if(items[viewType].amount!="0"){
            TransactionsViewHolder(LayoutInflater.from(context).inflate(R.layout.widget_transactions, parent, false),context)
        }else{
            TransactionsViewHolder(LayoutInflater.from(context).inflate(R.layout.widget_message, parent, false),context)
        }
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {

        println(items[position])

        if(position==0){
            holder.topMargin?.visibility=View.VISIBLE
        }else{
            holder.topMargin?.visibility=View.GONE
        }


        if(items[position].amount!="0"){
            holder.amount?.text = items[position].amount
            holder.time?.text = items[position].time
            holder.item = items[position]
            if(items[position].type=="Received"){
                holder.container?.gravity = Gravity.START
                holder.contentWidget?.background= context.getDrawable(R.drawable.transaction_received_ripple)
            }
        }else{
            holder.message?.text = items[position].message
            if(items[position].type=="Received"){
                holder.container?.gravity = Gravity.START
                holder.contentWidget?.background= context.getDrawable(R.drawable.transaction_received_ripple)
            }
        }

    }

    fun updateList(updatedList : ArrayList<Transaction>,view:RecyclerView){
        this.items = updatedList
        try{
            notifyItemInserted(updatedList.size)
            view.scrollToPosition(updatedList.size+1)
        }catch (e:Throwable){
            e.printStackTrace()
        }
    }
}

class TransactionsViewHolder (view: View,context: Context,var item:Transaction?=null,var color:String?=null) : RecyclerView.ViewHolder(view) {

    var amount:TextView? = null
    var time:TextView? = null
    var message:TextView?=null
    var container:RelativeLayout?=view.findViewById(R.id.container) as RelativeLayout
    var contentWidget:RelativeLayout?=view.findViewById(R.id.contentWidget) as RelativeLayout
    var topMargin:LinearLayout?=view.findViewById(R.id.topMargin)

    init {
        try {
            amount = view.findViewById(R.id.amount) as TextView
            time = view.findViewById(R.id.time) as TextView
        }catch (e:Throwable){
            message = view.findViewById(R.id.message) as TextView
        }
        view.setOnClickListener{
            if (item!=null){
                HelperVariables.selectedTransaction = item
                context.startActivity(Intent(context,TransactionDetails::class.java))
            }
        }
    }
}
