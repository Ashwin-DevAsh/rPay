package com.DevAsh.recwallet.Context

object ApiContext {
    private var protocal = "http://"
    private var url:String = "rajalakshmipay.com"
    var profileSubDomain= protocal+"profile."+ url
    var syncSubDomain= protocal+ "sync."+ url
    var paymentSubDomain= protocal+ "transactions."+ url
    var qrKey="DevAsh9551574355"
    var imageURL = "http://profile.rajalakshmipay.com/getProfilePicture/"
    var martKey = "rec@3214-init|bab|AAA|{barath,ash,version}"
}