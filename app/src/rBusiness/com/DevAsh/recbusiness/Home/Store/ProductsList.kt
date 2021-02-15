package com.DevAsh.recbusiness.Home.Store

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.DevAsh.recbusiness.Context.ProductContext
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.UiContext
import com.DevAsh.recwallet.Models.BankAccount
import com.DevAsh.recwallet.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.activity_products_list.*
import kotlinx.android.synthetic.main.widget_accounts.view.*
import org.json.JSONArray
import org.json.JSONObject

class ProductsList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products_list)
        onClick()
        loadView()
    }


    private fun onClick(){
        done.setOnClickListener{
           makeDelivery(ProductContext.orderedProducts?.getString("qrtoken")!!)
        }

        cancel.setOnClickListener{
            finish()
        }
    }

    private fun loadView(){
        productsContainer.layoutManager = LinearLayoutManager(this)
        productsContainer.adapter = ProductsViewAdapter(
           ProductContext.orderedProducts?.getJSONArray("products")!! ,
            this
        )
    }



    private fun makeDelivery(id:String){
        AndroidNetworking.post("https://mart.rajalakshmimart.com/makeDelivery/")
            .addHeaders("key", ApiContext.martKey)
            .addBodyParameter("id",id)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?){
                    try{
                        val message = response?.getString("message")
                        if(message=="success"){
                            Toast.makeText(this@ProductsList,"Successfully delivered !", Toast.LENGTH_LONG).show()
                            finish()
                        }else{
                            Toast.makeText(this@ProductsList,"Something went wrong",Toast.LENGTH_LONG).show()
                        }
                    }catch (e:Throwable){
                        Toast.makeText(this@ProductsList,"Invalid token", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    Toast.makeText(this@ProductsList,"Something went wrong", Toast.LENGTH_LONG).show()
                    println(anError?.localizedMessage)
                }
            })
    }
}


class ProductsViewAdapter(private var items : JSONArray, val context: Context) : RecyclerView.Adapter<ProductsViewHolder>() {

    override fun getItemCount(): Int {
        return items.length()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        return ProductsViewHolder(LayoutInflater.from(context).inflate(R.layout.product_list_tile, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
//        holder.account= items[position]
        val product = items.getJSONObject(position).getJSONObject("product")
        holder.accountName.text = product.getString("productName")
//        Toast.makeText(context,product.getString("imageURL"),Toast.LENGTH_LONG).show()
        UiContext.loadProductImage(context,product.getString("imageURL"),holder.image)
        val count = items.getJSONObject(position).getInt("count")
        if(count ==1){
            holder.accountNumber.text = "$count item"
        }else{
            holder.accountNumber.text = "$count items"
        }
    }
}

class ProductsViewHolder(view: View) : RecyclerView.ViewHolder(view){
    var account: BankAccount?=null
    val accountName = view.accountName
    val accountNumber = view.accountNumber
    var image = view.avatar
}

