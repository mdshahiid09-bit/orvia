package com.orvia.shahid.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.orvia.shahid.app.OrviaApplication
import com.orvia.shahid.app.R
import com.orvia.shahid.app.ui.MainActivity

class AiGenerationService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val taskTitle = intent?.getStringExtra(EXTRA_TASK_TITLE) ?: getString(R.string.app_name)

        if (action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification(taskTitle, "Generating AI response in background…")
        startForeground(OrviaApplication.NOTIFICATION_ID_GENERATION, notification)

        return START_NOT_STICKY
    }

    private fun createNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, OrviaApplication.CHANNEL_ID_GENERATION)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_orb_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "com.orvia.shahid.app.START_GENERATION"
        const val ACTION_STOP = "com.orvia.shahid.app.STOP_GENERATION"
        const val EXTRA_TASK_TITLE = "extra_task_title"

        fun startService(context: Context, taskTitle: String) {
            val intent = Intent(context, AiGenerationService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TASK_TITLE, taskTitle)
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AiGenerationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun notifyCompletion(context: Context, title: String, message: String) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, OrviaApplication.CHANNEL_ID_GENERATION)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_orb_logo)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(1002, notification)
        }
    }
}
