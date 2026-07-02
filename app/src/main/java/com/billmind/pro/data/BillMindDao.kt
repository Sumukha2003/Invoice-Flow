package com.billmind.pro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BillMindDao {
    @Query("SELECT * FROM inventory ORDER BY name")
    fun observeInventory(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM customers ORDER BY name")
    fun observeCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun observeInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoice_lines ORDER BY itemName")
    fun observeInvoiceLines(): Flow<List<InvoiceLineEntity>>

    @Query("SELECT * FROM inventory WHERE id = :id LIMIT 1")
    suspend fun inventoryById(id: String): InventoryItemEntity?

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun customerById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInventory(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLines(lines: List<InvoiceLineEntity>)

    @Query("UPDATE inventory SET stock = :stock WHERE id = :id")
    suspend fun updateStock(id: String, stock: Int)

    @Query("UPDATE customers SET outstanding = :outstanding WHERE id = :id")
    suspend fun updateOutstanding(id: String, outstanding: Double)

    @Query("UPDATE invoices SET paidAmount = :amount WHERE id = :id")
    suspend fun updatePaid(id: String, amount: Double)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoice(id: String)

    @Query("DELETE FROM invoice_lines WHERE invoiceId = :invoiceId")
    suspend fun deleteLines(invoiceId: String)

    @Delete
    suspend fun deleteInventory(item: InventoryItemEntity)
}
