package com.DevAsh.recwallet.Helper

import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.StateContext
import com.DevAsh.recwallet.Models.Merchant
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import org.json.JSONArray

object MerchantHelper {
    fun updateMerchant(){
        try {
            println("updating merchants....")
            AndroidNetworking.get(ApiContext.apiUrl+ ApiContext.profilePort+"/getMerchants")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray?) {
                        println(response)
                        if(response!=null){
                            var merchantTemp = ArrayList<Merchant>()
                            for(i in 0 until response.length()){
                                val user = Merchant(
                                    response.getJSONObject(i)["storename"].toString()
                                    ,"+"+response.getJSONObject(i)["number"].toString()
                                    ,response.getJSONObject(i)["id"].toString()
                                    ,response.getJSONObject(i)["email"].toString()
                                )
                                merchantTemp.add(user)
                            }
                            StateContext.initMerchant(merchantTemp)

                        }

                    }
                    override fun onError(anError: ANError?) {

                    }
                })
        }catch (e:Throwable){
            println("Updating merchant error $e")
        }

    }
}