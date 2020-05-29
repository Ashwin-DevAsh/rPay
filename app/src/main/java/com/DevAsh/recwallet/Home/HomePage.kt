package com.DevAsh.recwallet.Home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Models.Transaction
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Home.Transactions.SendMoney
import com.DevAsh.recwallet.Sync.SocketService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_home_page.*


class HomePage : AppCompatActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        context = this

        startService(Intent(this, SocketService::class.java))


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

        TransactionContext.allTransactions.addAll(
            arrayOf(
                Transaction("+6399419XXXXX", "993088909", "May 27 , 11:52 PM", "7,000", "Send"),
                Transaction("David", "993088909", "May 27 , 3:02 PM", "2,20,000", "Received"),
                Transaction("U.S Dealer", "993088909", "May 23 , 8:02 PM", "1,20,000", "Send"),
                Transaction("App client", "993088909", "May 21 , 11:00 PM", "3,50,000", "Received"),
                Transaction("Bank", "9930 8890 9780 1247", "May 15 , 11:00 PM", "65,000", "Send"),
                Transaction("+6399503XXXXX", "+639950357286", "May 5 , 9:30 PM", "11,750", "Send"),
                Transaction("Flight Book", "+06836547237", "April 15 , 12:30 AM", "39,000", "Send"),
                Transaction("Tobin", "+176814322900", "March 15 , 12:30 AM", "11,000", "Received"),
                Transaction("David", "+819885437880", "March 10 , 12:30 AM", "20,000", "Send")
            )
        )
        activity.layoutManager = LinearLayoutManager(context)
        activity.adapter = RecentActivityAdapter(
            TransactionContext.allTransactions.subList(
                0,
                if(TransactionContext.allTransactions.size>10) 10
                else TransactionContext.allTransactions.size
            ).toList(),context)
    }

    override fun onBackPressed() {
        TransactionContext.allTransactions.clear()
        super.onBackPressed()
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

class RecentActivityAdapter(private val items : List<Transaction>, val context: Context) : RecyclerView.Adapter<RecentActivityViewHolder>() {

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
            holder.additionalInfo.text= "-${items[position].amount}"
        }
    }


}

class RecentActivityViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val title = view.findViewById(R.id.title) as TextView
    val subtitle = view.findViewById(R.id.subtitle) as TextView
    val badge = view.findViewById(R.id.badge) as TextView
    val additionalInfo = view.findViewById(R.id.additionalInfo) as TextView
}
