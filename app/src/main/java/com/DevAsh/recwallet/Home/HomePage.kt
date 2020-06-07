package com.DevAsh.recwallet.Home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Helper.SnackBarHelper
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Home.Transactions.SendMoney
import com.DevAsh.recwallet.Sync.SocketService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_home_page.*


class HomePage : AppCompatActivity() {

    lateinit var context: Context
    lateinit var activityAdapter:RecentActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        context = this

        startService(Intent(this, SocketService::class.java))

        val balanceObserver = Observer<String> { currentBalance ->
             balance.text = currentBalance
        }

        val transactionObserver = Observer<ArrayList<Transaction>> {updatedList->
            activityAdapter.updateList(updatedList)
            activity.smoothScrollToPosition(0)

        }

        StateContext.model.currentBalance.observe(this,balanceObserver)
        StateContext.model.allTranactions.observe(this,transactionObserver)

        allActivities.setOnClickListener {
            startActivity(Intent(context, AllTransactions::class.java))
        }

        sendMoney.setOnClickListener {
            val permissions = arrayOf(android.Manifest.permission.READ_CONTACTS)
            if(packageManager.checkPermission(android.Manifest.permission.READ_CONTACTS,context.packageName)==PackageManager.PERMISSION_GRANTED ){
                startActivity(Intent(context, SendMoney::class.java))
            }else{
                ActivityCompat.requestPermissions(this, permissions,0)
            }

        }

        buyMoney.setOnLongClickListener{
            StateContext.addFakeTransactions()
            return@setOnLongClickListener true
        }

        activity.layoutManager = LinearLayoutManager(context)
        activityAdapter = RecentActivityAdapter(
           StateContext.model.allTranactions.value!!.subList(
                0,
                if( StateContext.model.allTranactions.value!!.size>10) 10
                else StateContext.model.allTranactions.value!!.size
            ).toList(),context)
        activity.adapter = activityAdapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
         if(requestCode==0){
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED ){
                    startActivity(Intent(context, SendMoney::class.java))
                }
        }
    }
}

class RecentActivityAdapter(private var items : List<Transaction>, val context: Context) : RecyclerView.Adapter<RecentActivityViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentActivityViewHolder {
        return RecentActivityViewHolder(LayoutInflater.from(context).inflate(R.layout.widget_listtile, parent, false))
    }

    override fun onBindViewHolder(holder: RecentActivityViewHolder, position: Int) {
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

class RecentActivityViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val title = view.findViewById(R.id.title) as TextView
    val subtitle = view.findViewById(R.id.subtitle) as TextView
    val badge = view.findViewById(R.id.badge) as TextView
    val additionalInfo = view.findViewById(R.id.additionalInfo) as TextView
}
