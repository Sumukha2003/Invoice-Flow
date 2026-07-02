package com.billmind.pro

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    fun schedule(context: Context, invoice: Invoice) {
        if (invoice.due <= 0) return
        val delayMillis = (invoice.dueAt - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<PaymentReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString(PaymentReminderWorker.KEY_INVOICE, invoice.number)
                    .putString(PaymentReminderWorker.KEY_AMOUNT, invoice.due.money())
                    .build()
            )
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            "payment-reminder-${invoice.id}",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
