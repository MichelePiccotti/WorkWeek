package com.michelepiccotti.workweek

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val swNotifications = findViewById<SwitchMaterial>(R.id.swNotifications)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val enabled = prefs.getBoolean("notifications_enabled", false)
        swNotifications.isChecked = enabled

        swNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            if (isChecked) {
                NotificationUtils.scheduleDailyNotification(this)
            } else {
                NotificationUtils.cancelDailyNotification(this)
            }
        }
    }
}
