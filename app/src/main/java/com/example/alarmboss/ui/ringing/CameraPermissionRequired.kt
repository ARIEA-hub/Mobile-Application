package com.example.alarmboss.ui.ringing

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Shown in place of the camera preview when permission is missing. The ringing screen
 * suppresses back/home, so if the user has permanently denied camera access ("don't ask
 * again"), the system permission dialog won't reappear -- this points them to App Settings
 * instead of leaving them stuck on an alarm they can't dismiss.
 */
@Composable
fun CameraPermissionRequired(
    message: String,
    permanentlyDenied: Boolean,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        if (permanentlyDenied) {
            Text(
                "Camera access was denied. Open Settings to allow it for AlarmBoss.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }) { Text("Open Settings") }
        } else {
            Button(onClick = onRequestPermission) { Text("Grant camera permission") }
        }
    }
}
