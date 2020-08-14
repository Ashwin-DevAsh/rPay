package com.DevAsh.recwallet.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.DevAsh.recwallet.Context.HelperVariables
import com.DevAsh.recwallet.Context.UiContext
import com.DevAsh.recwallet.Helper.MerchantHelper
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.SplashScreen
import com.DevAsh.recwallet.Sync.SocketHelper
import com.DevAsh.recwallet.Sync.SocketService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlin.random.Random


class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        println("new token $p0")
        super.onNewToken(p0)
    }

    override fun onCreate() {
        intent = Intent(this,SocketService::class.java)
        super.onCreate()
    }

    lateinit var intent :Intent

    override fun onMessageReceived(p0: RemoteMessage) {

        println(p0.data["type"])
        if (p0.data["type"]=="awake"){
            stopService(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 startForegroundService(intent)
            } else {
                 startService(intent)
            }
        }else if(p0.data["type"]?.startsWith("updateProfilePicture")!!){
              println("Updated")
              println(p0.data)
              try{
                  val id = p0.data["type"]?.split(",")!![1]
                  Picasso.get().invalidate(UiContext.buildURL(id))
                  Picasso.get().load(UiContext.buildURL(id))
                      .memoryPolicy(MemoryPolicy.NO_CACHE)
                      .networkPolicy(NetworkPolicy.NO_CACHE)
              }catch (e:Throwable){
                  println(e)
              }
        }else if(p0.data["type"]?.startsWith("merchantStatus")!!){
              println("Added New Merchant....")
              MerchantHelper.updateMerchant()
        }
        else{
            HelperVariables.openTransactionPage = true
            val type =  p0.data["type"]!!.split(",")[0]
            val amount = p0.data["type"]!!.split(",")[3]
            val fromName = p0.data["type"]!!.split(",")[1]

            try {
                SocketHelper.getMyState()
            } catch (e:Throwable){

            }

            when (type) {
                "addedMoney" -> {
                    showNotification("Added Money","Your $amount ${HelperVariables.currency}s has been successfully added.")
                }
                "withdraw" -> {
                    showNotification("withdraw","Your $amount ${HelperVariables.currency}s has been successfully withdraw.")
                }
                else -> {
                    showNotification(fromName,"You have received $amount ${HelperVariables.currency}s from $fromName.")
                }
            }


        }
        super.onMessageReceived(p0)
    }

    private fun showNotification(title:String, subTitle:String){
        var notificationID = Random.nextInt(1000000000)
        val intent = Intent(applicationContext,SplashScreen::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "Payment"
            val channelName: CharSequence = "Payment"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(notificationChannel)

            val builder: Notification.Builder = Notification.Builder(this, "Payment")
                .setContentTitle(title)
                .setContentText(subTitle)
                .setSmallIcon(R.drawable.rpay_notification)
                .setContentIntent(
                    PendingIntent.getActivities(this, 1, arrayOf(intent), PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .setAutoCancel(true)
            val notification: Notification = builder.build()
            notification.flags = Notification.FLAG_AUTO_CANCEL
            notificationManager.notify(notificationID,notification)
        } else {
            val builder = NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(subTitle)
                .setSmallIcon(R.drawable.rpay_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(
                    PendingIntent.getActivities(this, 1, arrayOf(intent), PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .setAutoCancel(true)
            val notification: Notification = builder.build()
            notification.flags = Notification.FLAG_AUTO_CANCEL
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(Random.nextInt(1000000000),notification)
        }
    }

}