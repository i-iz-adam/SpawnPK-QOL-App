package com.spk.app.worker

import android.content.Context
import androidx.work.*
import com.spk.app.data.repository.SalesRepository
import com.spk.app.notification.NotificationHelper
import java.util.concurrent.TimeUnit

class SalesCheckWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val repo = SalesRepository.getInstance(applicationContext)
            val matches = repo.checkForNewSales()
            NotificationHelper.ensureChannel(applicationContext)
            matches.forEach { match ->
                NotificationHelper.notifySale(
                    context = applicationContext,
                    accountName = match.accountName,
                    itemName = match.itemName,
                    unitPrice = match.unitPrice,
                    amount = match.amount,
                    remainingAfter = match.remainingAfter,
                    wasCompleted = match.wasCompleted
                )
            }
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "sales_check_periodic"

        fun schedule(context: Context, intervalMinutes: Long) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SalesCheckWorker>(
                intervalMinutes, TimeUnit.MINUTES
            ).setConstraints(constraints).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /** Call after adding a new watched item / account so a check happens right away too. */
        fun runOnceNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<SalesCheckWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
