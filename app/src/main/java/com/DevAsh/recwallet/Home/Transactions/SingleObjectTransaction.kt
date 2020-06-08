package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.QuickContactBadge
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_single_object_transaction.*

class SingleObjectTransaction : AppCompatActivity() {

    private lateinit var allActivityAdapter: AllActivityAdapter
    private lateinit var badge: TextView
    lateinit var context: Context
    var transaction = ArrayList<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_object_transaction)

        context=this

        badge = findViewById(R.id.badge)




        val transactionObserver = Observer<ArrayList<Transaction>> {updatedList->
            try{
                getData()
            }catch (e:Throwable){ }

        }

        StateContext.model.allTranactions.observe(this,transactionObserver)




        badge.text = TransactionContext.selectedUser!!.name[0].toString()

        if (TransactionContext.selectedUser!!.name.startsWith("+")) {
           badge.text = TransactionContext.selectedUser!!.name.subSequence(1, 3)
           badge.textSize = 18F
        }

        name.text = TransactionContext.selectedUser!!.name
        number.text = TransactionContext.selectedUser!!.number

        back.setOnClickListener{
            super.onBackPressed()
        }


        pay.setOnClickListener{
            startActivity(Intent(context,AmountPrompt::class.java))
        }
    }

    private fun getData(){
        transaction.clear()
        Handler().postDelayed({
            val allTransaction = StateContext.model.allTranactions.value!!
            for (i in allTransaction){
                if(i.number==TransactionContext.selectedUser!!.number.replace("+","")){
                    transaction.add(0,i)
                }
            }
            transactionContainer.layoutManager = LinearLayoutManager(this)
            allActivityAdapter = AllActivityAdapter(transaction,this)
            transactionContainer.adapter = allActivityAdapter

            scrollContainer.post {
                scrollContainer.fullScroll(View.FOCUS_DOWN)
                Handler().postDelayed({
                    loadingScreen.visibility = View.INVISIBLE
                    scrollContainer.visibility = View.VISIBLE
                },500)
            }
        },0)
    }
}

class AllActivityAdapter(private val items : ArrayList<Transaction>, val context: Context) : RecyclerView.Adapter<AllActivityViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllActivityViewHolder {
        return AllActivityViewHolder(LayoutInflater.from(context).inflate(R.layout.widget_transaction, parent, false))
    }

    override fun onBindViewHolder(holder: AllActivityViewHolder, position: Int) {
        holder.amount.text = "₿ ${items[position].amount}"
        holder.time.text = items[position].time
        if(items[position].type=="Received"){
            holder.container.gravity = Gravity.START
            holder.contentWidget.background= context.getDrawable(R.drawable.transaction_received_ripple)
        }
    }
}

class AllActivityViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val amount = view.findViewById(R.id.amount) as TextView
    val time = view.findViewById(R.id.time) as TextView
    val container = view.findViewById(R.id.container) as RelativeLayout
    val contentWidget = view.findViewById(R.id.contentWidget) as RelativeLayout
}



