package com.DevAsh.recwallet.rMart

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.DevAsh.recwallet.R
import kotlinx.android.synthetic.main.sheet_mart_access.view.*

class AccessSheet(val callBack: CallBack) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_mart_access, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.deny.setOnClickListener {
            this.dismiss()
            callBack.deny()

        }

        view.allow.setOnClickListener {
            this.dismiss()
            callBack.allow()
        }

    }








}