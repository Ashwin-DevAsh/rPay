package com.DevAsh.recwallet.Home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Home.Transactions.AddMoney
import com.DevAsh.recwallet.Home.Transactions.Contacts
import com.DevAsh.recwallet.Home.Transactions.SendMoney
import com.DevAsh.recwallet.Home.Transactions.SingleObjectTransaction
import com.DevAsh.recwallet.Models.Merchant
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Sync.SocketService
import kotlinx.android.synthetic.main.activity_home_page.*


class HomePage : AppCompatActivity() {

    var context: Context = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        merchantHolder.layoutManager = GridLayoutManager(context, 3)
        recent.layoutManager = GridLayoutManager(context, 3)

        merchantHolder.adapter = MerchantViewAdapter(arrayListOf(
            Merchant("Hut Cafe","1234567890",R.drawable.hut_cafe),
            Merchant("Tamil cafe","1234567890",R.drawable.tamil_cafe),
            Merchant("Rec Mart","1234567890",R.drawable.rec_mart),
            Merchant("A2Z","1234567890",R.drawable.ug),
            Merchant("CCD","1234567890",R.drawable.ccd),
            Merchant("Rec bill","1234567890",R.drawable.rec_bill),
            Merchant("Fine Payment","1234567890",R.drawable.fine),
            Merchant("zerox","1234567890",R.drawable.xrox),
            Merchant("More","1234567890",R.drawable.more)
            ),context)

        recent.adapter = PeopleViewAdapter(arrayListOf(
            Merchant("Hut Cafe","1234567890"),
            Merchant("Tamil cafe","1234567890"),
            Merchant("Rec Mart","1234567890"),
            Merchant("UG","1234567890"),
            Merchant("CCD","1234567890"),
            Merchant("Rec bill","1234567890"),
            Merchant("Fine Payment","1234567890"),
            Merchant("Exrox","1234567890"),
            Merchant("More","1234567890",R.drawable.more)
        ),context)

        startService(Intent(this, SocketService::class.java))

        val balanceObserver = Observer<String> { currentBalance ->
             balance.text = currentBalance
        }

        greetings.text=("Hii, "+DetailsContext.name)
        StateContext.model.currentBalance.observe(this,balanceObserver)


        sendMoney.setOnClickListener {
//            val permissions = arrayOf(android.Manifest.permission.READ_CONTACTS)
//            if(packageManager.checkPermission(android.Manifest.permission.READ_CONTACTS,context.packageName)==PackageManager.PERMISSION_GRANTED ){
//                startActivity(Intent(context, SendMoney::class.java))
//            }else{
//                ActivityCompat.requestPermissions(this, permissions,0)
//            }
//            startActivity(Intent(context, SendMoney::class.java))
        }

        buyMoney.setOnClickListener{v->
            startActivity(Intent(this,AddMoney::class.java))
        }

        buyMoney.setOnLongClickListener{
//            StateContext.addFakeTransactions()
            return@setOnLongClickListener true
        }

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

class MerchantViewAdapter(private var items : ArrayList<Merchant>, val context: Context) : RecyclerView.Adapter<MerchantViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MerchantViewHolder {
        return MerchantViewHolder(LayoutInflater.from(context).inflate(R.layout.merchant_avatar, parent, false),context)
    }

    override fun onBindViewHolder(holder: MerchantViewHolder, position: Int) {
        holder.title.text = items[position].name
        holder.merchant = items[position]

        if(items[position].image!=null){
            holder.badge.setImageDrawable(context.getDrawable(items[position].image!!))
        }

    }

    fun updateList(updatedList : ArrayList<Merchant>){
        this.items = updatedList
        notifyDataSetChanged()
    }
}

class MerchantViewHolder (view: View, context: Context) : RecyclerView.ViewHolder(view) {
    val title = view.findViewById(R.id.title) as TextView
    val badge = view.findViewById(R.id.avatar) as ImageView
    lateinit var merchant:Merchant

    init {
        view.setOnClickListener{
            if(merchant.name=="More"){
                context.startActivity(Intent(context,AddMoney::class.java))
            }else{
                TransactionContext.selectedUser= Contacts(merchant.name,merchant.phoneNumber)
                context.startActivity(
                    Intent(context, SingleObjectTransaction::class.java)
                )

            }
        }
    }
}



class PeopleViewAdapter(private var items : ArrayList<Merchant>, val context: Context) : RecyclerView.Adapter<PeopleViewHolder>() {

    var colors = arrayListOf(
        "#E68bc34a","#E6f9a825","#E600b0ff","#E6ff1744","#E6512da8","#E600acc1","#E61a237e","#E65d4037","#E6880e4f"
    )

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        return PeopleViewHolder(LayoutInflater.from(context).inflate(R.layout.people_avatar, parent, false),context)
    }

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        holder.title.text = items[position].name
        holder.people = items[position]


            holder.title.text = items[position].name
            holder.badge.text = items[position].name[0].toString()
            holder.badge.setBackgroundColor(Color.parseColor(colors[position%colors.size]))

            if (items[position].name.startsWith("+")) {
                holder.badge.text = items[position].name.subSequence(1, 3)
                holder.badge.textSize = 18F
            }

    }

    fun updateList(updatedList : ArrayList<Merchant>){
        this.items = updatedList
        notifyDataSetChanged()
    }
}

class PeopleViewHolder (view: View, context: Context) : RecyclerView.ViewHolder(view) {
    val title = view.findViewById(R.id.title) as TextView
    val badge = view.findViewById(R.id.badge) as TextView
    val card = view.findViewById(R.id.card) as CardView
    lateinit var people: Merchant

    init {
        view.setOnClickListener{
                TransactionContext.selectedUser= Contacts(people.name,people.phoneNumber)
                context.startActivity(
                    Intent(context, SingleObjectTransaction::class.java)
                )
        }
    }
}
