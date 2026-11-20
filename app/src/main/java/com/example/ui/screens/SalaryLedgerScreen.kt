package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.AppSettings
import com.example.data.local.BackupManager
import com.example.data.local.ClockFormat
import com.example.data.local.PaymentMethod
import com.example.data.local.SalaryRecord
import com.example.domain.LocalizationHelper
import com.example.ui.components.IconBadge3D
import com.example.ui.components.NotebookPageCard
import com.example.ui.components.monthAccentColor
import com.example.ui.theme.JetBrainsMonoFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalaryLedgerScreen(
    records: List<SalaryRecord>,
    allYears: List<Int>,
    searchQuery: String,
    filterYear: Int?,
    filterMethod: PaymentMethod?,
    settings: AppSettings,
    onSearchChange: (String) -> Unit,
    onYearFilterChange: (Int?) -> Unit,
    onMethodFilterChange: (PaymentMethod?) -> Unit,
    onAddNew: () -> Unit,
    onEdit: (SalaryRecord) -> Unit,
    onDuplicate: (SalaryRecord) -> Unit,
    onDeleteConfirmed: (SalaryRecord) -> Unit
) {
    val context = LocalContext.current
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits
    var recordToDelete by remember { mutableStateOf<SalaryRecord?>(null) }

    val filteredTotal = remember(records) { records.sumOf { it.amount } }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            // Search & Filters Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                if (isBn) "তারিখ, মাস, টাকা, ফোন, TrxID, মন্তব্য খুঁজুন..."
                                else "Search date, amount, phone, TrxID, note..."
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = if (isBn) "মুছুন" else "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ledger_search_input")
                    )
                    Button(
                        onClick = onAddNew,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("ledger_add_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (isBn) "যোগ" else "Add")
                    }
                }

                // Year Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = filterYear == null,
                        onClick = { onYearFilterChange(null) },
                        label = { Text(if (isBn) "সব বছর" else "All Years") }
                    )
                    allYears.forEach { yr ->
                        FilterChip(
                            selected = filterYear == yr,
                            onClick = { onYearFilterChange(if (filterYear == yr) null else yr) },
                            label = { Text(LocalizationHelper.formatInt(yr, useBnDigits)) }
                        )
                    }
                }

                // Payment Method Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = filterMethod == null,
                        onClick = { onMethodFilterChange(null) },
                        label = { Text(if (isBn) "সব মাধ্যম" else "All Methods") }
                    )
                    PaymentMethod.entries.forEach { method ->
                        val selected = filterMethod == method
                        FilterChip(
                            selected = selected,
                            onClick = { onMethodFilterChange(if (selected) null else method) },
                            label = { Text(method.label(isBn)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = method.accentColor.copy(alpha = 0.16f),
                                selectedLabelColor = method.accentColor
                            )
                        )
                    }
                }

                // Filtered Summary Strip
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${if (isBn) "মোট রেকর্ড:" else "Records:"} ${LocalizationHelper.formatInt(records.size, useBnDigits)}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "${if (isBn) "মোট:" else "Total:"} ${LocalizationHelper.formatCurrency(filteredTotal, useBnDigits, currency = settings.currency)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconBadge3D(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null,
                            size = 68.dp
                        )
                        Text(
                            text = if (isBn) "কোনো বেতন রেকর্ড পাওয়া যায়নি" else "No salary records found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "নতুন বেতন যুক্ত করতে উপরের 'যোগ' বাটনে চাপ দিন অথবা সার্চ ফিল্টার পরিবর্তন করুন।"
                            else "Tap 'Add' above to create a salary entry or adjust your search filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(records, key = { it.id }) { rec ->
                        SalaryNotebookRecordCard(
                            record = rec,
                            settings = settings,
                            onEdit = { onEdit(rec) },
                            onDuplicate = { onDuplicate(rec) },
                            onDelete = { recordToDelete = rec },
                            onShare = {
                                val text = BackupManager.formatShareText(rec, settings)
                                BackupManager.shareText(
                                    context,
                                    text,
                                    if (isBn) "AmarNote বেতন রসিদ" else "AmarNote Salary Receipt"
                                )
                            },
                            onPrint = {
                                BackupManager.printRecord(context, rec, settings)
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    recordToDelete?.let { rec ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text(if (isBn) "রেকর্ড মুছে ফেলবেন?" else "Delete Salary Record?") },
            text = {
                val amt = LocalizationHelper.formatCurrency(rec.amount, useBnDigits, currency = settings.currency)
                val date = LocalizationHelper.formatDate(rec.year, rec.month, rec.day, isBn, useBnDigits, style = settings.dateFormat)
                Text(
                    if (isBn) "$date তারিখের $amt বেতন রেকর্ডটি স্থায়ীভাবে মুছে ফেলতে চান?"
                    else "Permanently delete the salary record of $amt on $date?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteConfirmed(rec)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "মুছে ফেলুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalaryNotebookRecordCard(
    record: SalaryRecord,
    settings: AppSettings,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit
) {
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits
    val is12Hour = settings.clockFormat == ClockFormat.HOUR_12

    val dateStr = LocalizationHelper.formatDate(record.year, record.month, record.day, isBn, useBnDigits, style = settings.dateFormat)
    val banglaCalStr = LocalizationHelper.formatBanglaCalendarDate(record.year, record.month, record.day, isBn, useBnDigits)
    val weekdayStr = LocalizationHelper.weekdayName(record.dayOfWeek, isBn)
    val timeStr = LocalizationHelper.formatTime(record.hour, record.minute, record.second, is12Hour, isBn, useBnDigits)
    val monthAccent = monthAccentColor(record.month)

    NotebookPageCard(accentColor = monthAccent) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LocalizationHelper.formatCurrency(record.amount, useBnDigits, currency = settings.currency),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$dateStr • $weekdayStr",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$banglaCalStr • $timeStr",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = record.methodEnum.accentColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = record.methodEnum.label(isBn),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = record.methodEnum.accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Optional Transaction / Bank / Phone details
            val details = buildList {
                if (record.receiverNumber.isNotBlank()) {
                    add((if (isBn) "প্রাপক:" else "To:") + " ${record.receiverNumber}")
                }
                if (record.senderNumber.isNotBlank()) {
                    add((if (isBn) "প্রেরক:" else "From:") + " ${record.senderNumber}")
                }
                if (record.bankName.isNotBlank()) {
                    add((if (isBn) "ব্যাংক:" else "Bank:") + " ${record.bankName}")
                }
                if (record.accountLastDigits.isNotBlank()) {
                    add((if (isBn) "অ্যাকাউন্ট:" else "A/C:") + " ****${record.accountLastDigits}")
                }
                if (record.transactionId.isNotBlank()) {
                    add("TrxID: ${record.transactionId}")
                }
            }

            if (details.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    details.forEach { d ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = d,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (record.comment.isNotBlank()) {
                Text(
                    text = "${if (isBn) "মন্তব্য:" else "Comment:"} ${record.comment}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            if (record.extraNote.isNotBlank()) {
                Text(
                    text = "${if (isBn) "নোট:" else "Note:"} ${record.extraNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // 5 Action Buttons: Edit, Duplicate, Share, Print, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = if (isBn) "সম্পাদনা" else "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDuplicate) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = if (isBn) "ডুপ্লিকেট" else "Duplicate",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = if (isBn) "শেয়ার" else "Share",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                IconButton(onClick = onPrint) {
                    Icon(
                        Icons.Filled.Print,
                        contentDescription = if (isBn) "প্রিন্ট" else "Print",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = if (isBn) "মুছুন" else "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
