package com.DevAsh.recwallet.Context

import androidx.recyclerview.widget.RecyclerView
import com.DevAsh.recwallet.Home.Transactions.AllActivityAdapter
import com.DevAsh.recwallet.Home.Transactions.AllActivityViewHolder

object Cache {
    val singleObjecttransactionCache = HashMap<String,AllActivityAdapter>()
}