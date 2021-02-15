package com.DevAsh.recbusiness.Home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.DevAsh.recbusiness.Context.ProductContext
import com.DevAsh.recbusiness.Home.Store.ProductsList
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import io.jsonwebtoken.Jwts
import org.json.JSONObject

class DeliveryScanner : AppCompatActivity(),AlertHelper.AlertDialogCallback {
    private lateinit var codeScanner: CodeScanner
    var context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_scanner)
        init()


    }

    override fun onResume() {
        codeScanner?.startPreview()
        super.onResume()
    }



    private fun init(){
        val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                try {
//                    val body = Jwts.parser().setSigningKey("rec@3214-init|bab|AAA|{barath,ash,version}").parseClaimsJws(it.text).body
                    Toast.makeText(this,it.text.toString(),Toast.LENGTH_LONG).show();

                    val id = it.text // body["orderID"].toString();
                    getOrder(id)
                }catch (e:Throwable){
                    Toast.makeText(this,e.message,Toast.LENGTH_LONG).show();
                    println(e)
                }

            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
        codeScanner.startPreview()
    }

    private fun getOrder(id:String){
        AndroidNetworking.get("https://mart.rajalakshmimart.com/getQrToken/${id}")
            .addHeaders("key", ApiContext.martKey)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {

                override fun onResponse(response: JSONObject?) {
                    try{
                        val order :JSONObject =  response?.getJSONArray("order")?.get(0) as JSONObject
                        if(order.getString("status")=="pending"){
                            ProductContext.orderedProducts = order
                            startActivity(Intent(this@DeliveryScanner,ProductsList::class.java))
//                            finish()
                        }else if(order.getString("status")=="invalid token" ){
                             AlertHelper.showNativeAlertDialog(this@DeliveryScanner,"Invalid Token!","This is an invalid Token. Kindly ask the customer to provide correct QR code token.",this@DeliveryScanner)
                        }else if(order.getString("status")=="expired" ){
                            AlertHelper.showNativeAlertDialog(this@DeliveryScanner,"Expired!","This token has already expired.Kindly do not provide any products to the customer.",this@DeliveryScanner)
                        }else if(order.getString("status")=="delivered" ){
                            AlertHelper.showNativeAlertDialog(this@DeliveryScanner,"Already Delivered!","This item has already been delivered. Don't repeat the delivery again.",this@DeliveryScanner)
                        }
                        else{
                            Toast.makeText(this@DeliveryScanner,order.getString("status"),Toast.LENGTH_LONG).show()
                        }

                    }catch (e:Throwable){
                        Toast.makeText(this@DeliveryScanner,"invalid token ${e.message}",Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    Toast.makeText(this@DeliveryScanner,"Something went wrong",Toast.LENGTH_LONG).show()
                    println(anError?.localizedMessage)
                }
            })
    }

    override fun onDismiss() {
        codeScanner?.startPreview()
    }

    override fun onDone() {
        codeScanner?.startPreview()

    }
}