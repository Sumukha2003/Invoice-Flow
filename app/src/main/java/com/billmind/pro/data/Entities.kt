package com.billmind.pro.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Entity(tableName = "inventory")
data class InventoryItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sku: String,
    val stock: Int,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val gstRate: Double,
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val email: String,
    val creditLimit: Double,
    val outstanding: Double = 0.0,
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val number: String,
    val customerId: String,
    val customerName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val dueAt: Long = System.currentTimeMillis() + 7L * 24L * 60L * 60L * 1000L,
    val paidAmount: Double = 0.0,
)

@Entity(tableName = "invoice_lines")
data class InvoiceLineEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val invoiceId: String,
    val itemId: String,
    val itemName: String,
    val quantity: Int,
    val purchasePrice: Double,
    val price: Double,
    val gstRate: Double,
)

data class InvoiceUi(
    val invoice: InvoiceEntity,
    val lines: List<InvoiceLineEntity>,
) {
    val subtotal: Double get() = lines.sumOf { it.price * it.quantity }
    val gst: Double get() = lines.sumOf { it.price * it.quantity * it.gstRate / 100.0 }
    val total: Double get() = subtotal + gst
    val due: Double get() = (total - invoice.paidAmount).coerceAtLeast(0.0)
    val profit: Double get() = lines.sumOf { (it.price - it.purchasePrice) * it.quantity }
}

data class ReportSummary(
    val sales: Double = 0.0,
    val gst: Double = 0.0,
    val profit: Double = 0.0,
    val due: Double = 0.0,
)

fun Long.asDate(): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))
fun Double.money(): String = "Rs %.2f".format(this)
