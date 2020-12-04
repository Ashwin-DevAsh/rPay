package com.DevAsh.recbusiness.Home

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.DevAsh.recbusiness.Registration.Login
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.LoadProfileCallBack
import com.DevAsh.recwallet.Context.UiContext
import com.DevAsh.recwallet.Database.Credentials
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.Home.ChangePassword
import com.DevAsh.recwallet.Home.Transactions.AllTransactions
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Sync.SocketHelper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.zxing.WriterException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.back
import kotlinx.android.synthetic.main.activity_profile.changePassword
import kotlinx.android.synthetic.main.activity_profile.email
import kotlinx.android.synthetic.main.activity_profile.logout
import kotlinx.android.synthetic.main.activity_profile.name
import kotlinx.android.synthetic.main.activity_profile.phone
import kotlinx.android.synthetic.main.activity_profile.profilePicture
import kotlinx.android.synthetic.main.activity_profile.qr
import kotlinx.android.synthetic.main.activity_profile.qrName
import kotlinx.android.synthetic.main.activity_profile.share
import kotlinx.android.synthetic.main.activity_profile.transactions
import kotlinx.android.synthetic.rBusiness.activity_profile.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class Profile : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapUri:Uri
    private lateinit var bitmapPath:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_profile)

        qrName.text = Credentials.credentials.accountName
        name.text =Credentials.credentials.accountName
        email.text =Credentials.credentials.email
        phone.text = Credentials.credentials.phoneNumber

        if(!Credentials.credentials.isVerified){
            status.visibility=View.VISIBLE
        }


        val jwt = Jwts.builder().claim("name", Credentials.credentials.accountName)
            .claim("number", Credentials.credentials.phoneNumber)
            .claim("id",Credentials.credentials.id)
            .claim("email",Credentials.credentials.email)
            .signWith(SignatureAlgorithm.HS256, ApiContext.qrKey)
            .compact()

        val qrgEncoder =
            QRGEncoder(jwt.toString(), null, QRGContents.Type.TEXT, 1000)
        qrgEncoder.colorWhite = getColor(R.color.colorPrimary)
        try {
             bitmap = qrgEncoder.bitmap
             qr.setImageBitmap(bitmap)
        } catch (e: WriterException) {

        }

        loadProfilePicture()


        changePassword.setOnClickListener{
            startActivity(Intent(this, ChangePassword::class.java))
        }

        share.setOnClickListener{
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
            startActivity(Intent(this,
                AllTransactions::class.java))
        }

        profilePicture.setOnClickListener{
            Handler().postDelayed({
                UiContext.removeFromCache(Credentials.credentials.id)
            },0)
            val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if(packageManager.checkPermission(android.Manifest.permission.READ_CONTACTS,this.packageName)==PackageManager.PERMISSION_GRANTED ){
                startActivity(Intent(this,
                    SelectLogo::class.java))
            }else{
                ActivityCompat.requestPermissions(this, permissions,1)
            }
        }

        logout.setOnClickListener{
            val mBottomSheetDialog = AlertDialog.Builder(this)
            val sheetView: View = layoutInflater.inflate(R.layout.confirm_sheet, null)
            val done = sheetView.findViewById<TextView>(R.id.done)
            val cancel = sheetView.findViewById<TextView>(R.id.cancel)
            mBottomSheetDialog.setView(sheetView)
            val dialog = mBottomSheetDialog.show()
            cancel.setOnClickListener{
                dialog.dismiss()
            }
            done.setOnClickListener{
                dialog.dismiss()
                logOut()
            }

        }

    }

    private fun logOut(){
        Realm.getDefaultInstance().executeTransaction{ realm ->
            realm.deleteAll()
            val intent = Intent(applicationContext, Login::class.java)
            finishAffinity()
            startActivity(intent)
        }
    }

    private fun share(){
        val view = findViewById<View>(R.id.qrContent)
        val bitmap2 = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap2)
        view.draw(canvas)
        bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, bitmap2,"title", null)
        bitmapUri = Uri.parse(bitmapPath)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri )
        intent.putExtra(Intent.EXTRA_TEXT,"Hey! this is R-Pay QrCode")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
    }

    private fun uploadImage(file:File,newImage:Bitmap){
        AndroidNetworking.upload(ApiContext.profileSubDomain+"/addProfilePicture/"+Credentials.credentials.id)
            .addHeaders("token", Credentials.credentials.token)
            .addMultipartFile("profilePicture",file)
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener {
                    bytesUploaded, totalBytes -> println("$bytesUploaded $totalBytes")
            }.getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    println(response)
                    if(response?.getString("message")=="done"){
                        AlertHelper.showToast("Successfully Changed!",this@Profile)
                        loadProfileNoCache()
                        UiContext.isProfilePictureChanged = true
                        UiContext.newProfile = newImage
                        SocketHelper.updateProfilePicture()
                    }else{
                        AlertHelper.showError("Error while uploading",this@Profile)
                    }
                }

                override fun onError(anError: ANError?) {
                    println(anError)
                }

            })
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
        }else if(requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED ){
                startActivity(Intent(this,
                    SelectLogo::class.java))
            }
        }
    }


    private fun loadProfilePicture(){
        UiContext.loadProfileImage(
            this,
            Credentials.credentials.id,
            object : LoadProfileCallBack {
                override fun onSuccess() {
                    profilePicture.background=resources.getDrawable(R.drawable.image_avatar)
                    profilePicture.setPadding(35,35,35,35)
                }
                override fun onFailure() {

                }
            },
            profilePicture,
            R.drawable.profile
        )
    }

    private fun loadProfileNoCache(){
        profilePicture.setPadding(0,0,0,0)
        UiContext.loadProfileNoCache(
            Credentials.credentials.id,
            object : LoadProfileCallBack {
                override fun onSuccess() {
                    profilePicture.background=resources.getDrawable(R.drawable.image_avatar)
                    profilePicture.setPadding(35,35,35,35)

                }
                override fun onFailure() {

                }
            },
            profilePicture,
            R.drawable.profile
        )
    }

    override fun onResume() {
        if(bitmapImage!=null){
            val logo = convertToFile(bitmapImage!!)
            if(logo!=null){
                uploadImage(logo, bitmapImage!!)
                bitmapImage=null
            }else{
                AlertHelper.showError("Error while uploading",this@Profile)
            }
        }
        super.onResume()
    }

    private fun convertToFile(bitmap: Bitmap):File?{
        var file: File? = null
        return try {
            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + "logo.jpg")
            file.createNewFile()
            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
            val bitmapdata = bos.toByteArray()
            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file
        }
    }


    companion object{
        var bitmapImage:Bitmap? = null
    }


}