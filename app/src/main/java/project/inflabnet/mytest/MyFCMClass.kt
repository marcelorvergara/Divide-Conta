package project.inflabnet.mytest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*


class MyFCMClass: FirebaseMessagingService() {

    private val TAG = "JSA-FCM"
    private lateinit var notificationManager: NotificationManager
    private val ADMIN_CHANNEL_ID = "InfSocial"

    override fun onNewToken(token: String) {
        Log.i(TAG, "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            // do with Notification payload...
            // remoteMessage.notification.body

            val handler = Handler(Looper.getMainLooper())
            handler.post { Toast.makeText(applicationContext, remoteMessage.notification?.body!!,Toast.LENGTH_SHORT).show() }

            Log.i(TAG, "Title: " + remoteMessage.notification?.title!!)
            Log.i(TAG, "Body: " + remoteMessage.notification?.body!!)
        }

        if (remoteMessage.data.isNotEmpty()) {
            // do with Data payload...
            // remoteMessage.data
            Log.e(TAG, "Data: " + remoteMessage.data)
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private fun setupNotificationChannels() {
//        val adminChannelName = getString(R.string.notifications_admin_channel_name)
//        val adminChannelDescription = getString(R.string.notifications_admin_channel_description)
//
//        val adminChannel: NotificationChannel
//        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW)
//        adminChannel.description = adminChannelDescription
//        adminChannel.enableLights(true)
//        adminChannel.lightColor = Color.RED
//        adminChannel.enableVibration(true)
//        notificationManager.createNotificationChannel(adminChannel)
//    }
}