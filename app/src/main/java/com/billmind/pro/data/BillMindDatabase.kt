package com.billmind.pro.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        InventoryItemEntity::class,
        CustomerEntity::class,
        InvoiceEntity::class,
        InvoiceLineEntity::class,
    ],
    version = 1,
)
abstract class BillMindDatabase : RoomDatabase() {
    abstract fun dao(): BillMindDao
}
