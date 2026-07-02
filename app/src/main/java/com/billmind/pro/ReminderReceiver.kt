package com.billmind.pro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val invoice = intent.getStringExtra("invoice") ?: "Invoice"
        val amount = intent.getStringExtra("amount") ?: "payment"
        PaymentReminderWorker.showPaymentReminder(context, invoice, amount)
    }
}
