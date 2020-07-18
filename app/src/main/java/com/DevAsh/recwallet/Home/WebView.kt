package com.DevAsh.recwallet.Home

import android.graphics.Bitmap
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.DevAsh.recwallet.Context.WebContext
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.activity_web_view.*


class WebView : AppCompatActivity() {

    var page:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        page = intent.getStringExtra("page")

        setContentView(R.layout.activity_web_view)
        webView.getSettings().setJavaScriptEnabled(true)
        webView.webViewClient = object : WebViewClient() {

           override fun onPageFinished(view: WebView?, url: String?) {
                 loading.visibility= View.GONE
            }
        }
        webView.loadUrl(WebContext.url+page)

    }

    override fun onBackPressed() {
        if(!webView.canGoBack()){
            super.onBackPressed()
        }else{
            webView.goBack()

        }
    }
}