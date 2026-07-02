package com.billmind.pro.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ReportCalculatorTest {
    @Test
    fun monthlyReport_includesOnlyCurrentMonthInvoices() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 15, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val may = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 31, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val juneInvoice = invoiceAt(
            createdAt = now.timeInMillis,
            paidAmount = 50.0,
            purchasePrice = 100.0,
            sellingPrice = 200.0,
            quantity = 2,
            gstRate = 18.0,
        )
        val oldInvoice = invoiceAt(
            createdAt = may.timeInMillis,
            paidAmount = 0.0,
            purchasePrice = 10.0,
            sellingPrice = 1000.0,
            quantity = 1,
            gstRate = 18.0,
        )

        val report = ReportCalculator.monthlyReport(listOf(juneInvoice, oldInvoice), now)

        assertEquals(472.0, report.sales, 0.001)
        assertEquals(72.0, report.gst, 0.001)
        assertEquals(200.0, report.profit, 0.001)
        assertEquals(422.0, report.due, 0.001)
    }

    private fun invoiceAt(
        createdAt: Long,
        paidAmount: Double,
        purchasePrice: Double,
        sellingPrice: Double,
        quantity: Int,
        gstRate: Double,
    ): InvoiceUi {
        val invoice = InvoiceEntity(
            id = "invoice-$createdAt",
            number = "INV-$createdAt",
            customerId = "customer",
            customerName = "Test Customer",
            createdAt = createdAt,
            paidAmount = paidAmount,
        )
        val line = InvoiceLineEntity(
            invoiceId = invoice.id,
            itemId = "item",
            itemName = "A4 Paper Ream",
            quantity = quantity,
            purchasePrice = purchasePrice,
            price = sellingPrice,
            gstRate = gstRate,
        )
        return InvoiceUi(invoice, listOf(line))
    }
}
