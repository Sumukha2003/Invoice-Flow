package com.billmind.pro

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class InventoryItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var sku: String,
    var stock: Int,
    var purchasePrice: Double,
    var sellingPrice: Double,
    var gstRate: Double,
)

data class Customer(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var phone: String,
    var email: String,
    var creditLimit: Double,
    var outstanding: Double = 0.0,
)

data class InvoiceLine(
    val itemId: String,
    val itemName: String,
    val quantity: Int,
    val price: Double,
    val gstRate: Double,
)

data class Invoice(
    val id: String = UUID.randomUUID().toString(),
    val number: String,
    val customerId: String,
    val customerName: String,
    val lines: MutableList<InvoiceLine>,
    val createdAt: Long = System.currentTimeMillis(),
    var dueAt: Long = System.currentTimeMillis() + 7L * 24L * 60L * 60L * 1000L,
    var paidAmount: Double = 0.0,
) {
    val subtotal: Double get() = lines.sumOf { it.price * it.quantity }
    val gst: Double get() = lines.sumOf { it.price * it.quantity * it.gstRate / 100.0 }
    val total: Double get() = subtotal + gst
    val due: Double get() = (total - paidAmount).coerceAtLeast(0.0)
    val profit: Double get() = lines.sumOf { it.price * it.quantity } * 0.28
}

fun Long.asDate(): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))
fun Double.money(): String = "Rs %.2f".format(this)

fun InventoryItem.toJson() = JSONObject()
    .put("id", id)
    .put("name", name)
    .put("sku", sku)
    .put("stock", stock)
    .put("purchasePrice", purchasePrice)
    .put("sellingPrice", sellingPrice)
    .put("gstRate", gstRate)

fun Customer.toJson() = JSONObject()
    .put("id", id)
    .put("name", name)
    .put("phone", phone)
    .put("email", email)
    .put("creditLimit", creditLimit)
    .put("outstanding", outstanding)

fun InvoiceLine.toJson() = JSONObject()
    .put("itemId", itemId)
    .put("itemName", itemName)
    .put("quantity", quantity)
    .put("price", price)
    .put("gstRate", gstRate)

fun Invoice.toJson() = JSONObject()
    .put("id", id)
    .put("number", number)
    .put("customerId", customerId)
    .put("customerName", customerName)
    .put("createdAt", createdAt)
    .put("dueAt", dueAt)
    .put("paidAmount", paidAmount)
    .put("lines", JSONArray().also { arr -> lines.forEach { arr.put(it.toJson()) } })

fun JSONObject.toInventoryItem() = InventoryItem(
    id = getString("id"),
    name = getString("name"),
    sku = optString("sku"),
    stock = optInt("stock"),
    purchasePrice = optDouble("purchasePrice"),
    sellingPrice = optDouble("sellingPrice"),
    gstRate = optDouble("gstRate", 18.0),
)

fun JSONObject.toCustomer() = Customer(
    id = getString("id"),
    name = getString("name"),
    phone = optString("phone"),
    email = optString("email"),
    creditLimit = optDouble("creditLimit"),
    outstanding = optDouble("outstanding"),
)

fun JSONObject.toInvoiceLine() = InvoiceLine(
    itemId = getString("itemId"),
    itemName = getString("itemName"),
    quantity = optInt("quantity"),
    price = optDouble("price"),
    gstRate = optDouble("gstRate"),
)

fun JSONObject.toInvoice() = Invoice(
    id = getString("id"),
    number = getString("number"),
    customerId = getString("customerId"),
    customerName = getString("customerName"),
    createdAt = optLong("createdAt"),
    dueAt = optLong("dueAt"),
    paidAmount = optDouble("paidAmount"),
    lines = mutableListOf<InvoiceLine>().also { list ->
        val arr = optJSONArray("lines") ?: JSONArray()
        for (i in 0 until arr.length()) list.add(arr.getJSONObject(i).toInvoiceLine())
    },
)
