package com.billmind.pro.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillMindRepository @Inject constructor(
    private val dao: BillMindDao,
) {
    val inventory: Flow<List<InventoryItemEntity>> = dao.observeInventory()
    val customers: Flow<List<CustomerEntity>> = dao.observeCustomers()

    val invoices: Flow<List<InvoiceUi>> = combine(
        dao.observeInvoices(),
        dao.observeInvoiceLines(),
    ) { invoices, lines ->
        invoices.map { invoice ->
            InvoiceUi(invoice, lines.filter { it.invoiceId == invoice.id })
        }
    }

    suspend fun seedIfNeeded(currentInventory: List<InventoryItemEntity>, currentCustomers: List<CustomerEntity>) {
        if (currentInventory.isNotEmpty() || currentCustomers.isNotEmpty()) return
        listOf(
            InventoryItemEntity(name = "A4 Paper Ream", sku = "PAPER-A4", stock = 80, purchasePrice = 210.0, sellingPrice = 320.0, gstRate = 12.0),
            InventoryItemEntity(name = "Laptop Bag", sku = "BAG-001", stock = 24, purchasePrice = 450.0, sellingPrice = 799.0, gstRate = 18.0),
            InventoryItemEntity(name = "Scientific Calculator", sku = "CALC-FX", stock = 32, purchasePrice = 720.0, sellingPrice = 999.0, gstRate = 18.0),
            InventoryItemEntity(name = "Lab Record Notebook", sku = "LAB-REC", stock = 70, purchasePrice = 65.0, sellingPrice = 120.0, gstRate = 5.0),
            InventoryItemEntity(name = "USB Flash Drive 32GB", sku = "USB-32", stock = 26, purchasePrice = 260.0, sellingPrice = 449.0, gstRate = 18.0),
            InventoryItemEntity(name = "College ID Card Holder", sku = "ID-HOLD", stock = 95, purchasePrice = 18.0, sellingPrice = 45.0, gstRate = 12.0),
        ).forEach { dao.upsertInventory(it) }
        dao.upsertCustomer(
            CustomerEntity(
                name = "Dayananda Sagar Academy",
                phone = "9876543210",
                email = "accounts@example.com",
                creditLimit = 150000.0,
            )
        )
    }

    suspend fun addInventory(name: String, sku: String, stock: Int, purchase: Double, selling: Double, gst: Double) {
        require(name.isNotBlank()) { "Product name is required" }
        require(stock >= 0) { "Stock cannot be negative" }
        require(selling >= purchase) { "Selling price should be greater than purchase price" }
        dao.upsertInventory(
            InventoryItemEntity(
                name = name.trim(),
                sku = sku.ifBlank { "SKU-${System.currentTimeMillis()}" },
                stock = stock,
                purchasePrice = purchase,
                sellingPrice = selling,
                gstRate = gst,
            )
        )
    }

    suspend fun addCustomer(name: String, phone: String, email: String, creditLimit: Double) {
        require(name.isNotBlank()) { "Customer name is required" }
        dao.upsertCustomer(
            CustomerEntity(
                name = name.trim(),
                phone = phone.trim(),
                email = email.trim(),
                creditLimit = creditLimit.coerceAtLeast(0.0),
            )
        )
    }

    suspend fun createInvoice(customerId: String, itemId: String, quantity: Int, paidAmount: Double): InvoiceUi {
        val customer = dao.customerById(customerId) ?: error("Customer not found")
        val item = dao.inventoryById(itemId) ?: error("Stock item not found")
        require(item.stock > 0) { "${item.name} is out of stock" }
        require(quantity in 1..item.stock) { "Only ${item.stock} units available for ${item.name}" }

        val invoice = InvoiceEntity(
            number = "INV-${System.currentTimeMillis().toString().takeLast(6)}",
            customerId = customer.id,
            customerName = customer.name,
            paidAmount = paidAmount.coerceAtLeast(0.0),
        )
        val line = InvoiceLineEntity(
            invoiceId = invoice.id,
            itemId = item.id,
            itemName = item.name,
            quantity = quantity,
            purchasePrice = item.purchasePrice,
            price = item.sellingPrice,
            gstRate = item.gstRate,
        )
        val ui = InvoiceUi(invoice, listOf(line))
        require(customer.outstanding + ui.due <= customer.creditLimit) {
            "Customer credit limit exceeded"
        }

        dao.upsertInvoice(invoice)
        dao.upsertLines(listOf(line))
        dao.updateStock(item.id, item.stock - quantity)
        dao.updateOutstanding(customer.id, customer.outstanding + ui.due)
        return ui
    }

    suspend fun markPaid(invoice: InvoiceUi) {
        dao.updatePaid(invoice.invoice.id, invoice.total)
        val customer = dao.customerById(invoice.invoice.customerId) ?: return
        dao.updateOutstanding(customer.id, (customer.outstanding - invoice.due).coerceAtLeast(0.0))
    }

    suspend fun deleteInvoice(invoice: InvoiceUi) {
        val customer = dao.customerById(invoice.invoice.customerId)
        if (customer != null) {
            dao.updateOutstanding(customer.id, (customer.outstanding - invoice.due).coerceAtLeast(0.0))
        }
        dao.deleteLines(invoice.invoice.id)
        dao.deleteInvoice(invoice.invoice.id)
    }
}
