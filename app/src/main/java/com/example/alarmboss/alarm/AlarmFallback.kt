package com.example.alarmboss.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.alarmboss.AlarmBossApp
import com.example.alarmboss.R
import com.example.alarmboss.ui.ringing.AlarmRingingActivity

/**
 * The way an alarm still reaches the user when the system refuses to start the ringing service.
 *
 * Android lets an alarm start a foreground service only for a few seconds after it fires. If the
 * receiver runs late (a busy or dozing phone, a slow start), the request is refused with an
 * exception, and an unhandled one crashes the app and loses the alarm entirely. Instead this posts a
 * full-screen notification on a channel that plays the alarm sound itself. Opening it shows the
 * ringing screen, which is on screen and so is allowed to start the service and take over from the
 * notification.
 */
object AlarmFallback {
    const val NOTIFICATION_ID = 43
    private const val REQUEST_CODE_OFFSET = 3_000_000

    fun post(context: Context, alarmId: Long, followUp: String?) {
        val ring = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            followUp?.let { putExtra(AlarmReceiver.EXTRA_FOLLOW_UP, it) }
            putExtra(AlarmRingingActivity.EXTRA_START_SERVICE, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, alarmId.toInt() + REQUEST_CODE_OFFSET, ring,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, AlarmBossApp.CHANNEL_ALARM_FALLBACK)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.ringing_default_label))
            .setContentText(context.getString(R.string.notification_tap_to_open))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pending, true)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
        runCatching {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(NOTIFICATION_ID, notification)
        }
    }

    fun cancel(context: Context) {
        runCatching {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
        }
    }
}
