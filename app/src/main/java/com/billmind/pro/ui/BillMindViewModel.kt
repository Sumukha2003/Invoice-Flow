package com.billmind.pro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billmind.pro.data.BillMindRepository
import com.billmind.pro.data.CustomerEntity
import com.billmind.pro.data.InventoryItemEntity
import com.billmind.pro.data.InvoiceUi
import com.billmind.pro.data.ReportCalculator
import com.billmind.pro.data.ReportSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillMindState(
    val inventory: List<InventoryItemEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val invoices: List<InvoiceUi> = emptyList(),
    val report: ReportSummary = ReportSummary(),
    val darkMode: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class BillMindViewModel @Inject constructor(
    private val repository: BillMindRepository,
) : ViewModel() {
    private val darkMode = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<BillMindState> = combine(
        repository.inventory,
        repository.customers,
        repository.invoices,
        darkMode,
        message,
    ) { inventory, customers, invoices, isDark, currentMessage ->
        viewModelScope.launch { repository.seedIfNeeded(inventory, customers) }
        BillMindState(
            inventory = inventory,
            customers = customers,
            invoices = invoices,
            report = ReportCalculator.monthlyReport(invoices),
            darkMode = isDark,
            message = currentMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BillMindState())

    fun toggleTheme() {
        darkMode.value = !darkMode.value
    }

    fun consumeMessage() {
        message.value = null
    }

    fun addInventory(name: String, sku: String, stock: String, purchase: String, selling: String, gst: String) {
        viewModelScope.launch {
            runCatching {
                repository.addInventory(
                    name,
                    sku,
                    stock.toIntOrNull() ?: 0,
                    purchase.toDoubleOrNull() ?: 0.0,
                    selling.toDoubleOrNull() ?: 0.0,
                    gst.toDoubleOrNull() ?: 18.0,
                )
            }.onFailure { message.value = it.message ?: "Unable to add stock" }
        }
    }

    fun addCustomer(name: String, phone: String, email: String, creditLimit: String) {
        viewModelScope.launch {
            runCatching {
                repository.addCustomer(name, phone, email, creditLimit.toDoubleOrNull() ?: 0.0)
            }.onFailure { message.value = it.message ?: "Unable to add customer" }
        }
    }

    fun createInvoice(customerId: String, itemId: String, quantity: String, paid: String, onCreated: (InvoiceUi) -> Unit) {
        viewModelScope.launch {
            runCatching {
                repository.createInvoice(customerId, itemId, quantity.toIntOrNull() ?: 1, paid.toDoubleOrNull() ?: 0.0)
            }.onSuccess {
                message.value = "GST invoice created"
                onCreated(it)
            }.onFailure {
                message.value = it.message ?: "Unable to create invoice"
            }
        }
    }

    fun markPaid(invoice: InvoiceUi) {
        viewModelScope.launch { repository.markPaid(invoice) }
    }

    fun deleteInvoice(invoice: InvoiceUi) {
        viewModelScope.launch { repository.deleteInvoice(invoice) }
    }

}
