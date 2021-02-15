package com.DevAsh.recwallet.Context

object ApiContext {
    private var protocal = "http://"
    private var url:String = "rajalakshmipay.com"
    var profileSubDomain= protocal+"profile."+ url
    var syncSubDomain= protocal+ "sync."+ url
    var paymentSubDomain= protocal+ "transactions."+ url
//    var martUrl = protocal +"mart."+url
    var qrKey="DevAsh9551574355"
    var imageURL = "http://profile.rajalakshmipay.com/getProfilePicture/"

    var martURL = "http://www.rajalakshmimart.com/"
    var martToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE2MTI5NDk0NzgsImV4cCI6MTY0NDQ4NTQ3OCwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsImlkIjoicmJ1c2luZXNzQDkxOTg0MDU3MzcwMiJ9.Sjouhs9BR2skAhJmhlJ0oJOIem2Snvn3k4sqIzscPQo";
    var martKey = "rec@3214-init|bab|AAA|{barath,ash,version}"
}