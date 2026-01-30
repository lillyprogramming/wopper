package at.uastw.wopper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TimerService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var notificationManager: NotificationManager? = null

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_TIMER = "at.uastw.wopper.ACTION_STOP_TIMER"
        
        private var isRunning = false
        
        fun isServiceRunning() = isRunning

        fun startTimerNotification(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopTimerNotification(context: Context) {
            context.stopService(Intent(context, TimerService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_TIMER) {
            stopTimerAndService()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification())
        startAlarmSound()
        
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timer Notifications"
            val descriptionText = "Notifications for timer completion"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setSound(null, null)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Timer Finished!")
            .setContentText("Your timer has completed")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "OK",
                stopPendingIntent
            )
            .build()
    }

    private fun startAlarmSound() {
        try {
            mediaPlayer?.release()

            val possiblePaths = listOf(
                "themes/sounds/sound.mp3",
                "sounds/sound.mp3"
            )

            for (path in possiblePaths) {
                try {
                    val assetFileDescriptor = assets.openFd(path)
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(
                            assetFileDescriptor.fileDescriptor,
                            assetFileDescriptor.startOffset,
                            assetFileDescriptor.length
                        )
                        prepare()
                        isLooping = true
                        start()
                    }
                    assetFileDescriptor.close()
                    break
                } catch (_: Exception) {
                    continue
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun stopTimerAndService() {
        stopAlarmSound()

        val stopBroadcast = Intent(ACTION_STOP_TIMER)
        sendBroadcast(stopBroadcast)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun stopAlarmSound() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        stopAlarmSound()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
