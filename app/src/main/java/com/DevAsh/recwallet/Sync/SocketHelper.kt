package com.DevAsh.recwallet.Sync

import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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
            data.put("number",DetailsContext.credentials?.phoneNumber)

            socket.on("connect") {
                println("my id = "+socket.id())
                socket.emit("getInformation",data)
            }
        }catch (e:Exception){
            println(e)
        }
    }
}