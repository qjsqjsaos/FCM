package com.example.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {


    //메세지를 수신할 때 호출된다.(메세지를 받을때) remoteMessage는ㄴ 수신한 메세지이다.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(TAG, "From: ${remoteMessage.from}")


        //메시지에 데이터 페이로드가 포함되어 있는지 확인한다. 여기서 페이로드란 전송된 데이터를 의한다.
        //데이터 값이 있는지 없는 지 확인 할때 쓰인다.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            if (true) {
                //데이터를 처리하는데 10초 이상이 걸리면 workManager를 사용한다.
                scheduleJob()
            } else {
                //10초이내에 시작하면 아래 메서드를 실행한다.
                handleNow()
            }
        }

        //메세지에 알림 페이로가 포함되어 있는지 확인한다.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    //FirebaseInstanceIdService는 이제 사라짐. 이제 이걸 사용한다.
    //FCM 등록 토큰이 업데이트되면 호출된다.
    //토큰이 처음 생성될때 여기에서 토큰을 검색할 수 있다.
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        //이 앱 인스턴스에 메시지를 보내려는 경우나 서버 측에서 이 앱 구독을 관리한다면,
        //FCM 등록 토큰을 앱 서버에 추가합니다.

        sendRegistrationToServer(token)
    }

    //메세지 페이로드가 있을 때 실행되는 메서드(10초 이상 걸릴 떄 호출 된다)
    //WorkManager를 사용하여 비동기 작업을 예약한다.
    private fun scheduleJob() {
        // [START dispatch_job]
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance(this).beginWith(work).enqueue()
        // [END dispatch_job]
    }

    //메세지 페이로드가 있을 때 실행되는 메서드(10초 이내로 걸릴 때 호출된다)
    //BroadcastReceivers에 할당 된 시간을 처리합니다.
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    //타사 서비에 토큰을 유지해주는 메서드입니다.
    //사용자의 FCM등록 토큰을 서버 측 계정에 연결하려면 이 방법을 사용합니다.
    //응용 프로그램에서 유지 관리를 합니다.
    //파라미터에 들어있는 토큰은 새로운 토큰입니다.
    private fun sendRegistrationToServer(token: String?) {
        //이 메서드를 구현하여 앱 서버에 토큰을 보냅니다.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }


    //수신 된 FCM 메시지를 포함하는 간단한 알림을 만들고 표시합니다.
    //파라미터에 있는 messageBody에는 FCM 메세지 본문이 담겨져 있습니다.
    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(getString(R.string.fcm_message))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        //안드로이드 오레오 알림채널이 필요하기 때문에 넣음.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}