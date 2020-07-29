package com.DevAsh.recwallet.Context

object DetailsContext {
   var name: String? = null
   var phoneNumber: String? = null
   var email: String? = null
   var password: String? = null
   var token: String? = null
   var fcmToken:String=""
   var id :String=""

   fun setData(name: String, phoneNumber: String, email: String, password: String, token: String) {
      this.name = name
      this.password = password
      this.email = email
      this.phoneNumber = phoneNumber
      this.token = token
      this.id= "rpay@$phoneNumber"
   }

}