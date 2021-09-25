package com.DevAsh.recbusiness.Home.Store

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.DevAsh.recbusiness.Home.DeliveryScanner
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Home.QrScanner
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_my_store.*

class MyStore : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_store)
        onClick()
    }

    private fun onClick(){
        delivery.setOnClickListener {
            val permissions = arrayOf(android.Manifest.permission.CAMERA)
            if(packageManager.checkPermission(android.Manifest.permission.CAMERA,this.packageName)==PackageManager.PERMISSION_GRANTED ){
                startActivity(Intent(this, DeliveryScanner::class.java))
            }else{
                ActivityCompat.requestPermissions(this, permissions,1)
            }
        }

        downloadOrders.setOnClickListener {
            val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if(packageManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,packageName)== PackageManager.PERMISSION_GRANTED ){
                Handler().postDelayed({
                    downloadMyOrders(this)
                },0)
            }else{
                ActivityCompat.requestPermissions(this, permissions,2)
            }
        }

        myProducts.setOnClickListener {
            startActivity(Intent(this, MyProducts::class.java))
        }


    }

    private fun downloadMyOrders(context: Activity){
        val url = "https://mart.rajalakshmimart.com/downloadAllPendingOrders/"
        val downloadManager: DownloadManager? = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val uri = Uri.parse(url)
        val request: DownloadManager.Request = DownloadManager.Request(uri)
        request.addRequestHeader("Key",ApiContext.martKey);
        request.setVisibleInDownloadsUi(true)
        request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Orders.xlsx")
        downloadManager?.enqueue(request)
        Toast.makeText(this,"Downloading...",Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
         if(requestCode==1){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED ){
                startActivity(Intent(this, QrScanner::class.java))
            }
        }

        if(requestCode==2){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED ){
                downloadMyOrders(this)
            }
        }
    }
}