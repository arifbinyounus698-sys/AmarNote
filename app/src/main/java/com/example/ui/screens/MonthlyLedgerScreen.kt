package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.viewmodel.MonthlySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthlyLedgerScreen(
    selectedYear: Int,
    selectedMonth: Int,
    availableYears: List<Int>,
    monthSummary: MonthlySummary,
    allSalaries: List<SalaryRecord>,
    settings: AppSettings,
    onSelectYear: (Int) -> Unit,
    onSelectMonth: (Int) -> Unit,
    onEditRecord: (SalaryRecord) -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits
    val monthColor = monthAccentColor(selectedMonth)
    val monthTitle = LocalizationHelper.gregorianMonthName(selectedMonth, isBn)
    val yearTitle = LocalizationHelper.formatInt(selectedYear, useBnDigits)

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            BackupManager.writePdfReport(
                                outputStream = out,
                                title = "$monthTitle $yearTitle",
                                subtitle = if (isBn) "মাসিক ডিজিটাল বেতন খাতার রিপোর্ট" else "Monthly Digital Salary Ledger Report",
                                records = monthSummary.records,
                                settings = settings
                            )
                        }
                    }
                    onMessage(if (isBn) "মাসিক PDF রিপোর্ট সংরক্ষিত হয়েছে" else "Monthly PDF report saved")
                } catch (e: Exception) {
                    onMessage(e.localizedMessage ?: "PDF Error")
                }
            }
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val csv = withContext(Dispatchers.IO) {
                        BackupManager.buildCsv(monthSummary.records, isBn)
                    }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                    }
                    onMessage(if (isBn) "মাসিক CSV ফাইল সংরক্ষিত হয়েছে" else "Monthly CSV saved")
                } catch (e: Exception) {
                    onMessage(e.localizedMessage ?: "CSV Error")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Year Selector Strip (10+ Years)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isBn) "বছর নির্বাচন করুন (১০+ বছর সংরক্ষণ)" else "Select Year (10+ Year Archive)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableYears.forEach { yr ->
                            FilterChip(
                                selected = yr == selectedYear,
                                onClick = { onSelectYear(yr) },
                                label = { Text(LocalizationHelper.formatInt(yr, useBnDigits)) }
                            )
                        }
                    }
                }
            }

            // 12 Colorful Month Pages Grid (4x3)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (rowIdx in 0..2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (colIdx in 0..3) {
                                val m = rowIdx * 4 + colIdx + 1
                                val mColor = monthAccentColor(m)
                                val isSelected = m == selectedMonth
                                val mTotal = allSalaries.filter { it.year == selectedYear && it.month == m }.sumOf { it.amount }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) mColor else mColor.copy(alpha = 0.12f)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) mColor else mColor.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onSelectMonth(m) }
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = LocalizationHelper.gregorianMonthName(m, isBn),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else mColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (mTotal > 0) LocalizationHelper.formatCurrency(mTotal, useBnDigits, currency = settings.currency) else "—",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Month Digital Ledger Summary Page
            item {
                NotebookPageCard(accentColor = monthColor) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$monthTitle $yearTitle",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = monthColor
                                )
                                Text(
                                    text = if (isBn) "মাসিক বেতনের ডিজিটাল পাতা" else "Monthly Digital Ledger Page",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconBadge3D(
                                icon = Icons.Filled.CalendarMonth,
                                tint = monthColor,
                                contentDescription = null,
                                size = 46.dp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = monthColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isBn) "মোট বেতন" else "Monthly Total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = monthColor
                                    )
                                    Text(
                                        text = LocalizationHelper.formatCurrency(monthSummary.totalAmount, useBnDigits, currency = settings.currency),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isBn) "কয়বার পেয়েছি / রেকর্ড" else "Count / Records",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${LocalizationHelper.formatInt(monthSummary.recordCount, useBnDigits)} ${if (isBn) "বার" else "times"}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Method Breakdown Chips
                        val nonZeroMethods = PaymentMethod.entries.filter { (monthSummary.methodTotals[it] ?: 0.0) > 0.0 }
                        if (nonZeroMethods.isNotEmpty()) {
                            Text(
                                text = if (isBn) "কোন মাধ্যমে পেয়েছি:" else "By Payment Method:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                nonZeroMethods.forEach { method ->
                                    val amt = monthSummary.methodTotals[method] ?: 0.0
                                    Surface(
                                        color = method.accentColor.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "${method.label(isBn)}: ${LocalizationHelper.formatCurrency(amt, useBnDigits, currency = settings.currency)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = method.accentColor,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    pdfLauncher.launch("AmarNote_${selectedYear}_${selectedMonth}.pdf")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = monthColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBn) "মাসিক PDF" else "Monthly PDF")
                            }
                            OutlinedButton(
                                onClick = {
                                    csvLauncher.launch("AmarNote_${selectedYear}_${selectedMonth}.csv")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBn) "CSV এক্সপোর্ট" else "Export CSV")
                            }
                        }
                    }
                }
            }

            // Individual entries for the selected month
            if (monthSummary.records.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBn) "$monthTitle $yearTitle মাসে কোনো বেতন রেকর্ড যুক্ত করা হয়নি।"
                            else "No salary records logged for $monthTitle $yearTitle.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                }
            } else {
                items(monthSummary.records, key = { it.id }) { rec ->
                    val dateStr = LocalizationHelper.formatDate(rec.year, rec.month, rec.day, isBn, useBnDigits, style = settings.dateFormat)
                    val weekdayStr = LocalizationHelper.weekdayName(rec.dayOfWeek, isBn)
                    val timeStr = LocalizationHelper.formatTime(
                        rec.hour,
                        rec.minute,
                        rec.second,
                        settings.clockFormat == ClockFormat.HOUR_12,
                        isBn,
                        useBnDigits
                    )
                    NotebookPageCard(
                        accentColor = monthColor,
                        showRuledLines = false,
                        modifier = Modifier.clickable { onEditRecord(rec) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$dateStr ($weekdayStr)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${rec.methodEnum.label(isBn)} • $timeStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (rec.comment.isNotBlank()) {
                                    Text(
                                        text = rec.comment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Text(
                                text = LocalizationHelper.formatCurrency(rec.amount, useBnDigits, currency = settings.currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = monthColor
                            )
                        }
                    }
                }
            }
        }
    }
}
