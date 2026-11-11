package com.example.alarmboss

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager

/**
 * The few system calls that exist only on newer Android versions, with the behaviour older versions had
 * before those calls were introduced. Keeping them in one place means the rest of the app never checks
 * the version itself.
 */
object PlatformCompat {

    /** Exact alarms need a user grant from Android 12; before that every app may schedule them. */
    fun canScheduleExactAlarms(alarmManager: AlarmManager): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    /** Full-screen notifications became revocable in Android 14; before that the manifest permission is enough. */
    fun canUseFullScreenIntent(manager: NotificationManager): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE || manager.canUseFullScreenIntent()

    /** The phone's vibrator, through the manager Android 12 introduced or the service older versions use. */
    @Suppress("DEPRECATION")
    fun vibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}
