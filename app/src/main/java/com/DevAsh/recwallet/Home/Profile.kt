package com.DevAsh.recwallet.Home

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Registration.Login
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.WriterException
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_profile.*
import org.json.JSONObject


class Profile : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapUri:Uri
    private lateinit var bitmapPath:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_profile)

        qrName.text = DetailsContext.name
        name.text =DetailsContext.name
        email.text =DetailsContext.email
        phone.text =  "+"+DetailsContext.phoneNumber

        val qrData = JSONObject(mapOf(
            "name" to DetailsContext.name,
            "number" to DetailsContext.phoneNumber
        ))

        val qrgEncoder =
            QRGEncoder(qrData.toString(), null, QRGContents.Type.TEXT, 1000)
        qrgEncoder.colorWhite = getColor(R.color.colorPrimary)
        try {
             bitmap = qrgEncoder.bitmap
            qr.setImageBitmap(bitmap)
        } catch (e: WriterException) {

        }

        Handler().postDelayed({
            bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, bitmap,"title", null)
            bitmapUri = Uri.parse(bitmapPath)
        },0)



        share.setOnClickListener{
              println(qrData.toString())

            val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if(packageManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,packageName)== PackageManager.PERMISSION_GRANTED ){
                Handler().postDelayed({
                    share()
                },0)
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

        logout.setOnClickListener{
            val mBottomSheetDialog = BottomSheetDialog(this)
            val sheetView: View = layoutInflater.inflate(R.layout.confirm_sheet, null)
            val done = sheetView.findViewById<TextView>(R.id.done)
            val cancel = sheetView.findViewById<TextView>(R.id.cancel)
            cancel.setOnClickListener{
                mBottomSheetDialog.cancel()
            }
            done.setOnClickListener{
                mBottomSheetDialog.cancel()
                logOut()
            }
            mBottomSheetDialog.setContentView(sheetView)
            mBottomSheetDialog.show()
        }

    }

    private fun logOut(){
        println("hello")
        Realm.getDefaultInstance().executeTransaction{ realm ->
            realm.where(com.DevAsh.recwallet.Database.Credentials::class.java).findAll().deleteAllFromRealm()
            val intent = Intent(applicationContext, Login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }

    }

    private fun share(){


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