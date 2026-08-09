package com.spk.app

import android.app.Application
import com.spk.app.notification.NotificationHelper
import com.spk.app.worker.SalesCheckWorker

class SpawnPkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        SalesCheckWorker.schedule(this, AppConfig.BACKGROUND_CHECK_INTERVAL_MINUTES)
    }
}
