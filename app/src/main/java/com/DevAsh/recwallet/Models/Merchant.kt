package com.DevAsh.recwallet.Models

class Merchant(var name: String, var phoneNumber:String,var image: Int?=null){
    override fun toString(): String {
        return phoneNumber
    }

    override fun hashCode(): Int {
        return phoneNumber.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other as Merchant).phoneNumber==this.phoneNumber
    }
}