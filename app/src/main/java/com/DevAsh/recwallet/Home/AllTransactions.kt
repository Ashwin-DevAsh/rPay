package com.DevAsh.recwallet.Home


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Home.ViewModels.BalanceViewModel
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_all_transactions.*
import kotlinx.android.synthetic.main.activity_all_transactions.activity
import kotlin.collections.ArrayList


class AllTransactions : AppCompatActivity() {
    lateinit var context: Context
    lateinit var activityAdapter:AllActivityAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_transactions)


        context = this

        back.setOnClickListener{
            super.onBackPressed()
        }

        val transactionObserver = Observer<ArrayList<Transaction>> {updatedList->
         activityAdapter.updateList(updatedList)
        }

        StateContext.model.allTranactions.observe(this,transactionObserver)

        activityAdapter =AllActivityAdapter(StateContext.model.allTranactions.value!!,context)


        Handler().postDelayed({
            activity.layoutManager = LinearLayoutManager(context)
            activity.adapter = activityAdapter
            mainContent.visibility=View.VISIBLE
        },700)





    }

}

class AllActivityAdapter(private var items : ArrayList<Transaction>, val context: Context) : RecyclerView.Adapter<AllActivityViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllActivityViewHolder {
        return AllActivityViewHolder(LayoutInflater.from(context).inflate(R.layout.widget_listtile, parent, false))
    }

    override fun onBindViewHolder(holder: AllActivityViewHolder, position: Int) {
        holder.title.text = items[position].name
        holder.subtitle.text = items[position].time
        holder.badge.text = items[position].name[0].toString()

        if (items[position].name.startsWith("+")) {
            holder.badge.text = items[position].name.subSequence(1, 3)
            holder.badge.textSize = 18F
        }

        if(items[position].type=="Received"){
            holder.additionalInfo.setTextColor(Color.GREEN)
            holder.additionalInfo.text= "+${items[position].amount}"
        }else if(items[position].type=="Send"){
            holder.additionalInfo.setTextColor(Color.RED)
            holder.additionalInfo.text= "-${items[position].amount}"
        }
    }

    fun updateList(updatedList : ArrayList<Transaction>){
        this.items = updatedList
        notifyDataSetChanged()
    }
}

class AllActivityViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val title = view.findViewById(R.id.title) as TextView
    val subtitle = view.findViewById(R.id.subtitle) as TextView
    val badge = view.findViewById(R.id.badge) as TextView
    val additionalInfo = view.findViewById(R.id.additionalInfo) as TextView
}


