package com.billmind.pro.data

import java.util.Calendar

object ReportCalculator {
    fun monthlyReport(
        invoices: List<InvoiceUi>,
        now: Calendar = Calendar.getInstance(),
    ): ReportSummary {
        val invoiceDate = Calendar.getInstance()
        val monthInvoices = invoices.filter {
            invoiceDate.timeInMillis = it.invoice.createdAt
            invoiceDate.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                invoiceDate.get(Calendar.YEAR) == now.get(Calendar.YEAR)
        }
        return ReportSummary(
            sales = monthInvoices.sumOf { it.total },
            gst = monthInvoices.sumOf { it.gst },
            profit = monthInvoices.sumOf { it.profit },
            due = monthInvoices.sumOf { it.due },
        )
    }
}
