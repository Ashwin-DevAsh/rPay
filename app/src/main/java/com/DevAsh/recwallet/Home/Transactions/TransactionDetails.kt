package com.DevAsh.recwallet.Home.Transactions

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import com.DevAsh.recwallet.Context.*
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_transaction_status.*

class TransactionDetails : AppCompatActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_status)


        amount.text ="${TransactionContext.selectedTransaction?.amount}"
        selectedUserName.text = TransactionContext.selectedTransaction?.name
        badge.setBackgroundColor(Color.parseColor(TransactionContext.avatarColor))
        badge.text = TransactionContext.selectedTransaction?.name?.substring(0,1)
        badge.text = TransactionContext.selectedUser?.name.toString()[0].toString()


        transactionID.text = String.format("%020d", TransactionContext.selectedTransaction?.transactionId?.toInt())

        if(TransactionContext.selectedTransaction?.isGenerated!!){
            subText.text = "Added  ${TransactionContext.selectedTransaction?.amount} ${TransactionContext.currency}"
            type.text = "Added from"
        }else{
            subText.text =
                "${if (TransactionContext.selectedTransaction?.type=="Send") "Paid" else TransactionContext.selectedTransaction?.type}  ${TransactionContext.selectedTransaction?.amount} ${TransactionContext.currency}"
            type.text = if (TransactionContext.selectedTransaction?.type=="Send") "Paid to" else "Received from"

        }

        if (TransactionContext.selectedTransaction?.type=="Send"){
            toDetails.text = "To: ${TransactionContext.selectedTransaction?.name}"
            toID.text = "+${TransactionContext.selectedTransaction?.number}"

            fromDetails.text = "From: ${DetailsContext.name}"
            fromID.text = "+${DetailsContext.phoneNumber}"
        }else{
            toDetails.text = "To: ${DetailsContext.name}"
            toID.text = "+${DetailsContext.phoneNumber}"
            if(TransactionContext.selectedTransaction?.isGenerated!!){
                fromDetails.text = "${TransactionContext.selectedTransaction?.name} ID"
                fromID.text = "${TransactionContext.selectedTransaction?.number}"
            }else{
                fromDetails.text = "From: ${TransactionContext.selectedTransaction?.name}"
                fromID.text = "+${TransactionContext.selectedTransaction?.number}".trim()
            }
        }

        share.setOnClickListener{
            share()
        }

        cancel.setOnClickListener{
            onBackPressed()
        }

        back.setOnClickListener{
            onBackPressed()
        }




        if (TransactionContext.selectedTransaction?.name.toString().startsWith("+")) {
           badge.text = TransactionContext.selectedTransaction?.name.toString().subSequence(1, 3)
           badge.textSize = 18F
        }

        context=this



    }

    private fun share(){
        val view = findViewById<View>(R.id.mainContent)
        val bitmap2 = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap2)
        view.draw(canvas)
        val bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, bitmap2,"title", null)
        val bitmapUri = Uri.parse(bitmapPath)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
    }

}
