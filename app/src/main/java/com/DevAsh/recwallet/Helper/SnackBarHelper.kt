package com.DevAsh.recwallet.Helper

import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

object SnackBarHelper {
    fun showError(view:View,text:String){
        val snackBar: Snackbar = Snackbar.make(view,text, Snackbar.LENGTH_SHORT)
        val textView = snackBar.view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setBackgroundColor(Color.BLACK)
        textView.setTextColor(Color.parseColor("#D11F1F"))
        snackBar.view.setBackgroundColor(Color.BLACK)
        snackBar.show()
    }
}