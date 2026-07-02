package com.billmind.pro

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class BusinessStore(context: Context) {
    private val prefs = context.getSharedPreferences("bill_mind_pro", Context.MODE_PRIVATE)
    val inventory = mutableListOf<InventoryItem>()
    val customers = mutableListOf<Customer>()
    val invoices = mutableListOf<Invoice>()

    init {
        load()
        if (inventory.isEmpty() && customers.isEmpty() && invoices.isEmpty()) seedDemo()
    }

    fun save() {
        val root = JSONObject()
            .put("inventory", JSONArray().also { arr -> inventory.forEach { arr.put(it.toJson()) } })
            .put("customers", JSONArray().also { arr -> customers.forEach { arr.put(it.toJson()) } })
            .put("invoices", JSONArray().also { arr -> invoices.forEach { arr.put(it.toJson()) } })
        prefs.edit().putString("data", root.toString()).apply()
    }

    private fun load() {
        val root = JSONObject(prefs.getString("data", "{}") ?: "{}")
        root.optJSONArray("inventory")?.let { arr ->
            for (i in 0 until arr.length()) inventory.add(arr.getJSONObject(i).toInventoryItem())
        }
        root.optJSONArray("customers")?.let { arr ->
            for (i in 0 until arr.length()) customers.add(arr.getJSONObject(i).toCustomer())
        }
        root.optJSONArray("invoices")?.let { arr ->
            for (i in 0 until arr.length()) invoices.add(arr.getJSONObject(i).toInvoice())
        }
    }

    fun addInventory(item: InventoryItem) {
        inventory.add(0, item)
        save()
    }

    fun addCustomer(customer: Customer) {
        customers.add(0, customer)
        save()
    }

    fun addInvoice(invoice: Invoice) {
        invoices.add(0, invoice)
        invoice.lines.forEach { line ->
            inventory.find { it.id == line.itemId }?.let { it.stock = (it.stock - line.quantity).coerceAtLeast(0) }
        }
        customers.find { it.id == invoice.customerId }?.let { it.outstanding += invoice.due }
        save()
    }

    fun markPaid(invoice: Invoice) {
        val remaining = invoice.due
        invoice.paidAmount = invoice.total
        customers.find { it.id == invoice.customerId }?.let {
            it.outstanding = (it.outstanding - remaining).coerceAtLeast(0.0)
        }
        save()
    }

    fun deleteInvoice(invoice: Invoice) {
        invoices.removeAll { it.id == invoice.id }
        customers.find { it.id == invoice.customerId }?.let {
            it.outstanding = (it.outstanding - invoice.due).coerceAtLeast(0.0)
        }
        save()
    }

    fun totalsForMonth(): ReportSummary {
        val now = java.util.Calendar.getInstance()
        return totalsFor(now.get(java.util.Calendar.MONTH), now.get(java.util.Calendar.YEAR))
    }

    fun totalsFor(month: Int, year: Int): ReportSummary {
        val cal = java.util.Calendar.getInstance()
        val filtered = invoices.filter {
            cal.timeInMillis = it.createdAt
            cal.get(java.util.Calendar.MONTH) == month && cal.get(java.util.Calendar.YEAR) == year
        }
        val sales = filtered.sumOf { it.total }
        val gst = filtered.sumOf { it.gst }
        val profit = filtered.sumOf { invoice ->
            invoice.lines.sumOf { line ->
                val cost = inventory.find { it.id == line.itemId }?.purchasePrice ?: (line.price * 0.72)
                (line.price - cost) * line.quantity
            }
        }
        return ReportSummary(sales, gst, profit, filtered.sumOf { it.due })
    }

    private fun seedDemo() {
        val laptopBag = InventoryItem(name = "Laptop Bag", sku = "BAG-001", stock = 24, purchasePrice = 450.0, sellingPrice = 799.0, gstRate = 18.0)
        val printerPaper = InventoryItem(name = "A4 Paper Ream", sku = "PAPER-A4", stock = 80, purchasePrice = 210.0, sellingPrice = 320.0, gstRate = 12.0)
        inventory.addAll(listOf(laptopBag, printerPaper))
        customers.add(Customer(name = "Dayananda Sagar Academy", phone = "9876543210", email = "accounts@example.com", creditLimit = 150000.0))
        save()
    }
}

data class ReportSummary(
    val sales: Double,
    val gst: Double,
    val profit: Double,
    val due: Double,
)
