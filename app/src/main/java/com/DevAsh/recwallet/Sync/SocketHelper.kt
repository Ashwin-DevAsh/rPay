package com.DevAsh.recwallet.Sync

import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Context.RegistrationContext
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_otp.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

object SocketHelper {

    private val url = ApiContext.apiUrl+ ApiContext.syncPort
    lateinit var socket:Socket

    fun connect(){
        try{
            socket = IO.socket(url)
            socket.connect()

            val data = JSONObject()
            data.put("number",DetailsContext.phoneNumber)

            socket.on("connect") {
                println("my id = "+socket.id())
                socket.emit("getInformation",data)
            }

            socket.on("doUpdate"){
                  println("updating ....")
                  getState()
            }
        }catch (e:Exception){
            println(e)
        }
    }

    private fun getState(){
        AndroidNetworking.get(ApiContext.apiUrl + ApiContext.paymentPort + "/getState")
            .addHeaders("jwtToken",DetailsContext.token)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONArray(object:JSONArrayRequestListener{
                override fun onResponse(response: JSONArray?) {
                    println(response)
                }

                override fun onError(anError: ANError?) {
                    println(anError)
                }

            })
    }
}