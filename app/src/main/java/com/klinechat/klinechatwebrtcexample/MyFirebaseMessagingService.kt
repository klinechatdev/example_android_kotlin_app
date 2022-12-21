package com.klinechat.klinechatwebrtcexample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            // solution 1 : only works while app is in foreground
            var intent = Intent(applicationContext, IncomingCallActivity::class.java)
            intent.putExtra("caller_name", remoteMessage.data.get("caller_name"))
            intent.putExtra("room_token", remoteMessage.data.get("room_token"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)

            // solution 2 : fullscreen intent notification
//            val ctx = applicationContext
//            val intent = Intent(Intent.ACTION_MAIN, null)
//            val fakeIntent = Intent()
//            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            intent.setClass(ctx, IncomingCallActivity::class.java!!)
//            intent.putExtra("caller_name", remoteMessage.data.get("caller_name"))
//            intent.putExtra("room_token", remoteMessage.data.get("room_token"))
//            val pendingIntent = PendingIntent.getActivity(ctx, 1, intent, PendingIntent.FLAG_MUTABLE)
//            val pendingIntent2 = PendingIntent.getActivity(ctx, 1, fakeIntent, PendingIntent.FLAG_MUTABLE)
//            val builder = Notification.Builder(ctx)
//            builder.setOngoing(true)
//            builder.setPriority(Notification.PRIORITY_HIGH)
//
//            // Set notification content intent to take user to fullscreen UI if user taps on the
//            // notification body.
//            builder.setContentIntent(pendingIntent)
//            // Set full screen intent to trigger display of the fullscreen UI when the notification
//            // manager deems it appropriate.
//            builder.setFullScreenIntent(pendingIntent, true)
//
//            // Setup notification content.
//            builder.setSmallIcon(R.mipmap.ic_launcher)
//            builder.setContentTitle("Call from "+remoteMessage.data.get("caller_name"))
//            builder.setContentText("Tap on Accept to start Talking")
//            builder.setAutoCancel(true)
//
//
//            // Use builder.addAction(..) to add buttons to answer or reject the call.
//            val acceptAction = Notification.Action.Builder(Icon.createWithResource(ctx, R.drawable.outline_videocam_24), "Accept", pendingIntent).build()
//            val declineAction = Notification.Action.Builder(Icon.createWithResource(ctx, R.drawable.outline_videocam_24), "Decline", pendingIntent2).build()
//            builder.addAction(acceptAction)
//            builder.addAction(declineAction)
//
//            val notificationManager = ctx.getSystemService(
//                NotificationManager::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel("callnotification", "Incoming Calls", NotificationManager.IMPORTANCE_HIGH)
//                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//                channel.setSound(ringtoneUri, AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .build())
//                notificationManager.createNotificationChannel(channel)
//                builder.setChannelId("callnotification")
//            }
//            val notification = builder.build()
//            notification.flags = Notification.FLAG_INSISTENT
//            val currentNotificationId = Random().nextInt(500)
//            notificationManager.notify("callnotification", currentNotificationId, notification)



        }



        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]
    // [START on_new_token]
    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    // [END on_new_token]
    private fun scheduleJob() {
        // [START dispatch_job]
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .build()
        WorkManager.getInstance(this).beginWith(work).enqueue()
        // [END dispatch_job]
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement this method to send token to your app server.


    }

    class MyWorker(context: Context, workerParams: WorkerParameters) :
        Worker(context, workerParams) {
        override fun doWork(): Result {
            // TODO(developer): add long running task here.
            return Result.success()
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}