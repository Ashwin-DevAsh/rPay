package com.DevAsh.recwallet.Home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.JsonReader
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.TransactionContext
import com.DevAsh.recwallet.Home.Transactions.Contacts
import com.DevAsh.recwallet.Home.Transactions.SingleObjectTransaction
import com.DevAsh.recwallet.R
import com.budiyev.android.codescanner.*
import org.json.JSONObject


class QrScanner : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    var context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
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
                    val people = JSONObject(it.text)
                    TransactionContext.selectedUser= Contacts(people.get("name").toString(),people.get("number").toString())
                    startActivity(
                        Intent(context, SingleObjectTransaction::class.java)
                    )
                    finish()
                }catch (e:Throwable){
                    context.onBackPressed()

                }

                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
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
}