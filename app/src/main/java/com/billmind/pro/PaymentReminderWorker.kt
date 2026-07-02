package com.billmind.pro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class PaymentReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val invoice = inputData.getString(KEY_INVOICE) ?: "Invoice"
        val amount = inputData.getString(KEY_AMOUNT) ?: "payment"
        showPaymentReminder(applicationContext, invoice, amount)
        return Result.success()
    }

    companion object {
        const val KEY_INVOICE = "invoice"
        const val KEY_AMOUNT = "amount"

        fun showPaymentReminder(context: Context, invoice: String, amount: String) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "payment_due"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(
                    NotificationChannel(channelId, "Payment reminders", NotificationManager.IMPORTANCE_DEFAULT)
                )
            }
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Payment due")
                .setContentText("$invoice has $amount due.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            manager.notify(invoice.hashCode(), notification)
        }
    }
}
