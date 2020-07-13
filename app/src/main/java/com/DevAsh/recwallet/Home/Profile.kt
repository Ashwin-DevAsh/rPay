package com.DevAsh.recwallet.Home

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Home.Transactions.SendMoney
import com.DevAsh.recwallet.R
import com.google.zxing.WriterException
import kotlinx.android.synthetic.main.activity_profile.*



class Profile : AppCompatActivity() {
    lateinit var bitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        qrName.text = DetailsContext.name
        name.text =DetailsContext.name
        email.text =DetailsContext.email
        phone.text =  "+"+DetailsContext.phoneNumber

        val qrgEncoder =
            QRGEncoder("inputValue", null, QRGContents.Type.TEXT, 1000)
        qrgEncoder.colorWhite = getColor(R.color.colorPrimary)
        try {
             bitmap = qrgEncoder.bitmap
            qr.setImageBitmap(bitmap)
        } catch (e: WriterException) {

        }

        share.setOnClickListener{

            val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if(packageManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,packageName)== PackageManager.PERMISSION_GRANTED ){
                share()
            }else{
                ActivityCompat.requestPermissions(this, permissions,0)
            }
        }

        back.setOnClickListener{
            onBackPressed()
        }

        transactions.setOnClickListener{
            startActivity(Intent(this,AllTransactions::class.java))
        }

    }

    private fun share(){
        val bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, bitmap,"title", null)
        val bitmapUri = Uri.parse(bitmapPath)

        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri )
        intent.putExtra(Intent.EXTRA_TEXT,"Hey! this is R-Pay QrCode")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED ){
               share()
            }
        }
    }
}