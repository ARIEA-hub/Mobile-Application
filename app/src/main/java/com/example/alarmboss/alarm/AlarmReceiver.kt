package com.example.alarmboss.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START
            putExtra(EXTRA_ALARM_ID, alarmId)
            // Absent for a normal ring; names the FollowUp kind when this is one.
            intent.getStringExtra(EXTRA_FOLLOW_UP)?.let { putExtra(EXTRA_FOLLOW_UP, it) }
        }
        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: IllegalStateException) {
            // ForegroundServiceStartNotAllowedException: this receiver ran too long after the alarm
            // fired. Letting it propagate would crash the app and lose the alarm.
            AlarmFallback.post(context, alarmId, intent.getStringExtra(EXTRA_FOLLOW_UP))
        } catch (e: SecurityException) {
            AlarmFallback.post(context, alarmId, intent.getStringExtra(EXTRA_FOLLOW_UP))
        }
    }

    companion object {
        const val ACTION_ALARM_TRIGGER = "com.example.alarmboss.ACTION_ALARM_TRIGGER"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_FOLLOW_UP = "extra_follow_up"
    }
}
