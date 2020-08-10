package com.DevAsh.recwallet.Models

class Merchant(var name: String, var phoneNumber:String,var id:String,val email:String,var image: Int?=null){
    override fun toString(): String {
        return id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other as Merchant).id==this.id
    }
}