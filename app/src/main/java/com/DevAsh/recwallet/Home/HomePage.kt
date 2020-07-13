package com.DevAsh.recwallet.Home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Context.UiContext.colors
import com.DevAsh.recwallet.Home.Transactions.AddMoney
import com.DevAsh.recwallet.Home.Transactions.Contacts
import com.DevAsh.recwallet.Home.Transactions.SendMoney
import com.DevAsh.recwallet.Home.Transactions.SingleObjectTransaction
import com.DevAsh.recwallet.Models.Merchant
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Sync.SocketService
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_home_page.*


class HomePage : AppCompatActivity() {

    var context: Context = this

    lateinit var carouselView: CarouselView
    var sampleImages = intArrayOf(
        R.drawable.banner_1,
        R.drawable.banner_2
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        merchantHolder.layoutManager = GridLayoutManager(context, 3)
        recent.layoutManager = GridLayoutManager(context, 3)

        val bottomDown: Animation = AnimationUtils.loadAnimation(
            context,
            R.anim.button_down
        )
        val bottomUp: Animation = AnimationUtils.loadAnimation(
            context,
            R.anim.button_up
        )

        val hiddenPanel = findViewById<CardView>(R.id.scanContainer)


        scroller.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY >500) {
                if(hiddenPanel.visibility==View.VISIBLE){
                    hiddenPanel.startAnimation(bottomDown)
                    hiddenPanel.visibility=View.INVISIBLE
                }

            }
            if(scrollY < 500){
                if(hiddenPanel.visibility==View.INVISIBLE){
                    hiddenPanel.visibility=View.VISIBLE
                    hiddenPanel.startAnimation(bottomUp)
                }
            }
        }

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

        val imageListener =
            ImageListener { position, imageView ->
                imageView.setImageResource(sampleImages[position])
                imageView.scaleType=ImageView.ScaleType.CENTER_INSIDE
                imageView.adjustViewBounds=true
                imageView.setBackgroundColor(Color.WHITE)
            }

        carouselView = findViewById(R.id.carouselView)
        carouselView.pageCount = sampleImages.size
        carouselView.setImageListener(imageListener)

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
            startActivity(Intent(context, SendMoney::class.java))
        }

        buyMoney.setOnClickListener{v->
            startActivity(Intent(this,AddMoney::class.java))
        }

        buyMoney.setOnLongClickListener{
//            StateContext.addFakeTransactions()
            return@setOnLongClickListener true
        }

        profile.setOnClickListener{
            startActivity(Intent(context,Profile::class.java))
        }

        scan.setOnClickListener{
            val permissions = arrayOf(android.Manifest.permission.CAMERA)
            if(packageManager.checkPermission(android.Manifest.permission.CAMERA,context.packageName)==PackageManager.PERMISSION_GRANTED ){
                startActivity(Intent(context,
                    QrScanner::class.java))
            }else{
                ActivityCompat.requestPermissions(this, permissions,1)
            }

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
        }else if(requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED ){
                startActivity(Intent(context, QrScanner::class.java))
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
                TransactionContext.avatarColor = "#035aa6"
                context.startActivity(
                    Intent(context, SingleObjectTransaction::class.java)
                )

            }
        }
    }
}



class PeopleViewAdapter(private var items : ArrayList<Merchant>, val context: Context) : RecyclerView.Adapter<PeopleViewHolder>() {


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
            holder.color = colors[position%colors.size]

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
    lateinit var color: String
    lateinit var people: Merchant

    init {
        view.setOnClickListener{
                TransactionContext.selectedUser= Contacts(people.name,people.phoneNumber)
                TransactionContext.avatarColor = color
                context.startActivity(
                    Intent(context, SingleObjectTransaction::class.java)
                )
        }
    }
}
