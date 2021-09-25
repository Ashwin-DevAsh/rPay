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
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.UiContext
import com.DevAsh.recwallet.Models.BankAccount
import com.DevAsh.recwallet.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.activity_get_access.*
import kotlinx.android.synthetic.main.activity_products_list.*
import kotlinx.android.synthetic.main.my_product_listtile.view.*
import kotlinx.android.synthetic.main.widget_accounts.view.*
import kotlinx.android.synthetic.main.widget_accounts.view.accountName
import kotlinx.android.synthetic.main.widget_accounts.view.avatar
import org.json.JSONArray
import org.json.JSONObject

class MyProducts : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_products)
        getMyProducts()
    }

    private fun getMyProducts(){
        AndroidNetworking.get("https://mart.rajalakshmimart.com/getMyProducts")
            .addHeaders("key", ApiContext.martKey)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {

                override fun onResponse(response: JSONObject?) {
//                  Toast.makeText(this@MyProducts,response.toString(),Toast.LENGTH_LONG).show()
                    val allProducts = response?.getJSONArray("allProducts")
                    loadView(allProducts!!)

                }

                override fun onError(anError: ANError?) {

                }
            })
    }

    private fun loadView(products: JSONArray){
        productsContainer.layoutManager = LinearLayoutManager(this)
        productsContainer.adapter = MyProductsViewAdapter(products, this)
        mainContent.visibility = View.VISIBLE
    }
}

class MyProductsViewAdapter(private var items : JSONArray, val context: Context) : RecyclerView.Adapter<MyProductsViewHolder>() {

    override fun getItemCount(): Int {
        return items.length()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyProductsViewHolder {
        return MyProductsViewHolder(LayoutInflater.from(context).inflate(R.layout.my_product_listtile, parent, false))
    }

    override fun onBindViewHolder(holder: MyProductsViewHolder, position: Int) {
        val product = items.getJSONObject(position)
        holder.accountName.text = product.getString("productname")
        holder.isAvailable.isChecked = product.getBoolean("isavaliable")
        UiContext.loadProductImage(context,"https://mart.rajalakshmimart.com/getProductPictures/"+product.getString("imageurl"),holder.image)
        holder.isAvailable.setOnCheckedChangeListener{_,isChecked->
            AndroidNetworking.post("https://mart.rajalakshmimart.com/updateProduct")
                .addHeaders("key", ApiContext.martKey)
                .setPriority(Priority.IMMEDIATE)
                .addBodyParameter(
                    object{
                        var productID = product.getString("productid")
                        var productName= product.getString("productname")
                        var ownerID= product.getString("ownerid")
                        var discription = product.getString("discription")
                        var category = product.getString("category")
                        var price = product.getString("price")
                        var discount = product.getString("discount")
                        var isavailable = isChecked
                        var quantity= product.getString("quantity")
                        var imageUrl= product.getString("imageurl")
                        var availableOn= product.getString("availableon")
                    }
                )
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if(response?.getString("message")=="success")
                          Toast.makeText(context, "Updated Successfully",Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(context, "Failed",Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(anError: ANError?) {
                    }
                })
        }

    }
}

class MyProductsViewHolder(view: View) : RecyclerView.ViewHolder(view){
    var account: BankAccount?=null
    val accountName = view.accountName
    val accountNumber = view.accountNumber
    var image = view.avatar
    var isAvailable = view.productSwitch;
}

