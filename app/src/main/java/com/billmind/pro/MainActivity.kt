package com.billmind.pro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billmind.pro.data.InvoiceUi
import com.billmind.pro.data.asDate
import com.billmind.pro.data.money
import com.billmind.pro.ui.BillMindState
import com.billmind.pro.ui.BillMindViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: BillMindViewModel by viewModels()
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            var loggedIn by remember { mutableStateOf(false) }
            BillMindTheme(dark = state.darkMode) {
                if (loggedIn) {
                    BillMindApp(state, viewModel, onLogout = { loggedIn = false })
                } else {
                    LoginScreen(onLogin = { loggedIn = true })
                }
            }
        }
    }
}

enum class Section(val label: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Default.Home),
    INVOICES("Bills", Icons.AutoMirrored.Filled.ReceiptLong),
    INVENTORY("Stock", Icons.Default.Inventory),
    CUSTOMERS("Credit", Icons.Default.Group),
    REPORTS("Reports", Icons.Default.Assessment),
}

@Composable
private fun BillMindTheme(dark: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dark -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        dark -> darkColorScheme(primary = Color(0xFF52D6A8), secondary = Color(0xFF8DBDFF), tertiary = Color(0xFFFFA383))
        else -> lightColorScheme(primary = Color(0xFF1D9E75), secondary = Color(0xFF2F80ED), tertiary = Color(0xFFD85A30))
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography(), content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillMindApp(state: BillMindState, vm: BillMindViewModel, onLogout: () -> Unit) {
    var section by remember { mutableStateOf(Section.DASHBOARD) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbar.showSnackbar(message)
            vm.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Invoice Flow", fontWeight = FontWeight.Bold)
                        Text("Smart Billing & Inventory", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                    IconButton(onClick = vm::toggleTheme) {
                        Icon(if (state.darkMode) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = "Toggle theme")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Section.entries.forEach {
                    NavigationBarItem(
                        selected = section == it,
                        onClick = { section = it },
                        icon = { Icon(it.icon, contentDescription = it.label) },
                        label = { Text(it.label) },
                    )
                }
            }
        },
    ) { padding ->
        Surface(Modifier.fillMaxSize().padding(padding)) {
            when (section) {
                Section.DASHBOARD -> Dashboard(state)
                Section.INVOICES -> Invoices(state, vm) {
                    scope.launch { snackbar.showSnackbar(it) }
                }
                Section.INVENTORY -> InventoryScreen(state, vm)
                Section.CUSTOMERS -> CustomersScreen(state, vm)
                Section.REPORTS -> ReportsScreen(state)
            }
        }
    }
}

@Composable
private fun LoginScreen(onLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Invoice Flow", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Login to manage billing, stock, and customer credit.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Field("Email or mobile", email) {
                        email = it
                        error = null
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                error = "Enter email/mobile and password"
                            } else {
                                onLogin()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Login")
                    }
                }
            }
        }
    }
}

@Composable
private fun Dashboard(state: BillMindState) {
    var showSalesAmount by remember { mutableStateOf(false) }
    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            HomeHeader()
        }
        item {
            MetricGrid(
                "Monthly Sales" to state.report.sales.money(),
                "GST Collected" to state.report.gst.money(),
                "Profit" to state.report.profit.money(),
                "Open Credit" to state.customers.sumOf { it.outstanding }.money(),
            )
        }
        item { BarChart(state.invoices.take(6).map { it.total }, onClick = { showSalesAmount = true }) }
        item { SectionTitle("Payment due reminders") }
        val dueInvoices = state.invoices.filter { it.due > 0 }.take(5)
        if (dueInvoices.isEmpty()) item { InfoCard("No overdue follow-ups", "All customer credit is currently clear.") }
        items(dueInvoices) {
            InfoCard("Due: ${it.invoice.number}", "${it.invoice.customerName} owes ${it.due.money()} by ${it.invoice.dueAt.asDate()}")
        }
    }
    if (showSalesAmount) {
        AlertDialog(
            onDismissRequest = { showSalesAmount = false },
            title = { Text("Monthly sales analytics") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sales amount: ${state.report.sales.money()}")
                    Text("GST collected: ${state.report.gst.money()}")
                    Text("Profit: ${state.report.profit.money()}")
                    Text("Open due: ${state.report.due.money()}")
                }
            },
            confirmButton = { Button(onClick = { showSalesAmount = false }) { Text("OK") } },
        )
    }
}

@Composable
private fun HomeHeader() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Home", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Track GST invoices, monthly sales, stock, and customer credit in one place.",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun Invoices(state: BillMindState, vm: BillMindViewModel, showMessage: (String) -> Unit) {
    var createOpen by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<InvoiceUi?>(null) }
    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Button(onClick = { createOpen = true }, modifier = Modifier.fillMaxWidth()) { Text("Create GST Invoice") } }
        items(state.invoices) { invoice ->
            InfoCard("${invoice.invoice.number} - ${invoice.invoice.customerName}", "Total ${invoice.total.money()} | GST ${invoice.gst.money()} | Due ${invoice.due.money()}") {
                selected = invoice
            }
        }
    }
    if (createOpen) InvoiceDialog(state, vm, onDismiss = { createOpen = false })
    selected?.let { invoice ->
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(invoice.invoice.number) },
            text = { Text("${invoice.invoice.customerName}\nTotal: ${invoice.total.money()}\nDue: ${invoice.due.money()}\nGST: ${invoice.gst.money()}") },
            confirmButton = {
                Button(onClick = {
                    val legacy = invoice.toLegacyInvoice()
                    InvoicePdf.share(context, InvoicePdf.create(context, legacy, QrGenerator.invoiceQr(legacy)))
                    selected = null
                }) { Text("PDF / Share") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.markPaid(invoice); selected = null }) { Text("Paid") }
                    Button(onClick = { vm.deleteInvoice(invoice); selected = null; showMessage("Invoice deleted") }) { Text("Delete") }
                }
            },
        )
    }
}

@Composable
private fun InventoryScreen(state: BillMindState, vm: BillMindViewModel) {
    var open by remember { mutableStateOf(false) }
    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Button(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) { Text("Add Stock Item") } }
        items(state.inventory) {
            InfoCard("${it.name} (${it.sku})", "Stock ${it.stock} | Buy ${it.purchasePrice.money()} | Sell ${it.sellingPrice.money()} | GST ${it.gstRate.toInt()}%")
        }
    }
    if (open) InventoryDialog(vm) { open = false }
}

@Composable
private fun CustomersScreen(state: BillMindState, vm: BillMindViewModel) {
    var open by remember { mutableStateOf(false) }
    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Button(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) { Text("Add Customer") } }
        items(state.customers) {
            val status = if (it.outstanding > it.creditLimit) "Over credit limit" else "Available ${(it.creditLimit - it.outstanding).money()}"
            InfoCard(it.name, "${it.phone} | Outstanding ${it.outstanding.money()} | $status")
        }
    }
    if (open) CustomerDialog(vm) { open = false }
}

@Composable
private fun ReportsScreen(state: BillMindState) {
    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("Monthly profit/loss report") }
        item {
            MetricGrid(
                "Sales" to state.report.sales.money(),
                "GST" to state.report.gst.money(),
                "Profit" to state.report.profit.money(),
                "Open Due" to state.report.due.money(),
            )
        }
        item { PieChart(sales = state.report.sales, profit = state.report.profit) }
        item { SectionTitle("Low stock") }
        val lowStock = state.inventory.filter { it.stock <= 5 }
        if (lowStock.isEmpty()) item { InfoCard("Healthy inventory", "No low-stock products right now.") }
        items(lowStock) { InfoCard(it.name, "Only ${it.stock} left in stock") }
    }
}

@Composable
private fun InventoryDialog(vm: BillMindViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("10") }
    var purchase by remember { mutableStateOf("100") }
    var selling by remember { mutableStateOf("150") }
    var gst by remember { mutableStateOf("18") }
    FormDialog("Add stock item", onDismiss, {
        vm.addInventory(name, sku, stock, purchase, selling, gst)
        onDismiss()
    }) {
        Field("Product name", name) { name = it }
        Field("SKU", sku) { sku = it }
        Field("Opening stock", stock) { stock = it }
        Field("Purchase price", purchase) { purchase = it }
        Field("Selling price", selling) { selling = it }
        Field("GST %", gst) { gst = it }
    }
}

@Composable
private fun CustomerDialog(vm: BillMindViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var credit by remember { mutableStateOf("100000") }
    FormDialog("Add customer", onDismiss, {
        vm.addCustomer(name, phone, email, credit)
        onDismiss()
    }) {
        Field("Business name", name) { name = it }
        Field("Phone", phone) { phone = it }
        Field("Email", email) { email = it }
        Field("Credit limit", credit) { credit = it }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceDialog(state: BillMindState, vm: BillMindViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selectedCustomerId by remember(state.customers) { mutableStateOf(state.customers.firstOrNull()?.id.orEmpty()) }
    var selectedItemId by remember(state.inventory) { mutableStateOf(state.inventory.firstOrNull()?.id.orEmpty()) }
    val customer = state.customers.firstOrNull { it.id == selectedCustomerId }
    val item = state.inventory.firstOrNull { it.id == selectedItemId }
    var quantity by remember { mutableStateOf("1") }
    var paid by remember { mutableStateOf("0") }
    FormDialog("Create GST invoice", onDismiss, {
        if (customer != null && item != null) {
            vm.createInvoice(customer.id, item.id, quantity, paid) { created ->
                ReminderScheduler.schedule(context, created.toLegacyInvoice())
            }
        }
        onDismiss()
    }) {
        SelectionDropdown(
            label = "Customer",
            selectedText = customer?.name ?: "Add customer first",
            options = state.customers.map { it.id to "${it.name} - Due ${it.outstanding.money()}" },
            onSelected = { selectedCustomerId = it },
        )
        SelectionDropdown(
            label = "Item",
            selectedText = item?.let { "${it.name} (${it.stock} left)" } ?: "Add stock first",
            options = state.inventory.map { it.id to "${it.name} - Stock ${it.stock}" },
            onSelected = { selectedItemId = it },
        )
        Field("Quantity", quantity) { quantity = it }
        Field("Paid amount", paid) { paid = it }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionDropdown(
    label: String,
    selectedText: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (options.isNotEmpty()) expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = options.isNotEmpty()).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun FormDialog(title: String, onDismiss: () -> Unit, onSave: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() } },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun MetricGrid(vararg metrics: Pair<String, String>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        metrics.forEach { InfoCard(it.first, it.second, strong = true) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun InfoCard(title: String, subtitle: String, strong: Boolean = false, onClick: (() -> Unit)? = null) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = if (strong) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyMedium,
                color = if (strong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BarChart(values: List<Double>, onClick: () -> Unit) {
    val max = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Monthly sales analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (values.isEmpty()) {
                Text("Create a GST invoice to populate this dashboard.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Canvas(Modifier.fillMaxWidth().height(180.dp)) {
                    val barWidth = size.width / (values.size.coerceAtLeast(1) * 1.8f)
                    values.forEachIndexed { index, value ->
                        val height = (size.height * (value / max)).toFloat()
                        drawRoundRect(
                            color = Color(0xFF1D9E75),
                            topLeft = Offset(index * barWidth * 1.8f, size.height - height),
                            size = Size(barWidth, height),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PieChart(sales: Double, profit: Double) {
    val total = (sales + profit).coerceAtLeast(1.0)
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Canvas(Modifier.size(128.dp)) {
                drawArc(Color(0xFF2F80ED), -90f, (360 * sales / total).toFloat(), false, style = Stroke(width = 24f, cap = StrokeCap.Round))
                drawArc(Color(0xFF1D9E75), -90f + (360 * sales / total).toFloat(), (360 * profit / total).toFloat(), false, style = Stroke(width = 24f, cap = StrokeCap.Round))
            }
            Column {
                Text("Sales vs Profit", fontWeight = FontWeight.Bold)
                Text("Sales ${sales.money()}")
                Text("Profit ${profit.money()}")
            }
        }
    }
}

private fun InvoiceUi.toLegacyInvoice(): Invoice = Invoice(
    id = invoice.id,
    number = invoice.number,
    customerId = invoice.customerId,
    customerName = invoice.customerName,
    createdAt = invoice.createdAt,
    dueAt = invoice.dueAt,
    paidAmount = invoice.paidAmount,
    lines = lines.map {
        InvoiceLine(it.itemId, it.itemName, it.quantity, it.price, it.gstRate)
    }.toMutableList(),
)
