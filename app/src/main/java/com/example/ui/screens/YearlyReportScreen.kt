package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppSettings
import com.example.data.local.BackupManager
import com.example.data.local.PaymentMethod
import com.example.data.local.SalaryRecord
import com.example.domain.LocalizationHelper
import com.example.ui.components.IconBadge3D
import com.example.ui.components.NotebookPageCard
import com.example.ui.components.monthAccentColor
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.viewmodel.YearlyReportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun YearlyReportScreen(
    selectedYear: Int,
    availableYears: List<Int>,
    report: YearlyReportData,
    allSalaries: List<SalaryRecord>,
    settings: AppSettings,
    onSelectYear: (Int) -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits
    val yearRecords = allSalaries.filter { it.year == selectedYear }
    val yearLabel = LocalizationHelper.formatInt(selectedYear, useBnDigits)

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
                                title = if (isBn) "$yearLabel সালের বার্ষিক বেতন রিপোর্ট" else "Annual Salary Report $yearLabel",
                                subtitle = if (isBn) "AmarNote ১০ বছরের আর্থিক খাতা" else "AmarNote 10-Year Financial Archive",
                                records = yearRecords,
                                settings = settings
                            )
                        }
                    }
                    onMessage(if (isBn) "বার্ষিক PDF রিপোর্ট সংরক্ষিত হয়েছে" else "Yearly PDF report saved")
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
                        BackupManager.buildCsv(yearRecords, isBn)
                    }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                    }
                    onMessage(if (isBn) "বার্ষিক CSV ফাইল সংরক্ষিত হয়েছে" else "Yearly CSV saved")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Year Selector (10+ years)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isBn) "বছর নির্বাচন করুন (২০২৬ - ২০৩৬+)" else "Select Year (2026 - 2036+)",
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

            // Yearly Total & Transactions Hero Notebook Page
            item {
                NotebookPageCard(accentColor = AccentPurple) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBn) "$yearLabel সালের বার্ষিক মোট বেতন" else "Annual Total Salary ($yearLabel)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = LocalizationHelper.formatCurrency(report.totalSalary, useBnDigits, currency = settings.currency),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPurple
                                )
                                Text(
                                    text = "${if (isBn) "মোট লেনদেন:" else "Total Transactions:"} ${LocalizationHelper.formatInt(report.totalTransactions, useBnDigits)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconBadge3D(
                                icon = Icons.Filled.Assessment,
                                tint = AccentPurple,
                                contentDescription = null,
                                size = 50.dp
                            )
                        }

                        // Highest & Lowest Month
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = AccentEmerald.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = if (isBn) "সর্বোচ্চ আয়" else "Highest Month",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AccentEmerald
                                        )
                                    }
                                    val h = report.highestMonth
                                    Text(
                                        text = if (h != null) LocalizationHelper.gregorianMonthName(h.first, isBn) else "—",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (h != null) LocalizationHelper.formatCurrency(h.second, useBnDigits, currency = settings.currency) else "—",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Surface(
                                color = AccentOrange.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = if (isBn) "সর্বনিম্ন আয়" else "Lowest Month",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AccentOrange
                                        )
                                    }
                                    val l = report.lowestMonth
                                    Text(
                                        text = if (l != null) LocalizationHelper.gregorianMonthName(l.first, isBn) else "—",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (l != null) LocalizationHelper.formatCurrency(l.second, useBnDigits, currency = settings.currency) else "—",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { pdfLauncher.launch("AmarNote_Yearly_$selectedYear.pdf") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBn) "বার্ষিক PDF" else "Yearly PDF")
                            }
                            OutlinedButton(
                                onClick = { csvLauncher.launch("AmarNote_Yearly_$selectedYear.csv") },
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

            // 12-Month Canvas Bar Chart
            item {
                NotebookPageCard(accentColor = AccentEmerald, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isBn) "মাসভিত্তিক আয় চার্ট (জানুয়ারি - ডিসেম্বর)" else "Monthly Income Chart (Jan - Dec)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val maxMonthly = (report.monthlyTotals.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        ) {
                            val barCount = 12
                            val gap = 8.dp.toPx()
                            val barWidth = (size.width - gap * (barCount - 1)) / barCount
                            report.monthlyTotals.forEachIndexed { idx, amt ->
                                val ratio = (amt / maxMonthly).toFloat().coerceIn(0f, 1f)
                                val barHeight = (size.height - 12.dp.toPx()) * ratio
                                val x = idx * (barWidth + gap)
                                val y = size.height - barHeight
                                val barColor = monthAccentColor(idx + 1)
                                // Track
                                drawRoundRect(
                                    color = barColor.copy(alpha = 0.12f),
                                    topLeft = Offset(x, 0f),
                                    size = Size(barWidth, size.height),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                                if (barHeight > 2f) {
                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            (1..12).forEach { m ->
                                Text(
                                    text = LocalizationHelper.formatInt(m, useBnDigits),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = monthAccentColor(m)
                                )
                            }
                        }

                        HorizontalDivider()

                        // Monthly List
                        report.monthlyTotals.forEachIndexed { idx, amt ->
                            val m = idx + 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = LocalizationHelper.gregorianMonthName(m, isBn),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = LocalizationHelper.formatCurrency(amt, useBnDigits, currency = settings.currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (amt > 0) monthAccentColor(m) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Payment Method Breakdown for Selected Year
            item {
                NotebookPageCard(accentColor = AccentOrange, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isBn) "পেমেন্ট মাধ্যম অনুযায়ী হিসাব" else "Income by Payment Method",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val total = report.totalSalary.coerceAtLeast(1.0)
                        PaymentMethod.entries.forEach { method ->
                            val amt = report.methodTotals[method] ?: 0.0
                            val progress = (amt / total).toFloat().coerceIn(0f, 1f)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = method.label(isBn),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = method.accentColor
                                    )
                                    Text(
                                        text = LocalizationHelper.formatCurrency(amt, useBnDigits, currency = settings.currency),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    color = method.accentColor,
                                    trackColor = method.accentColor.copy(alpha = 0.14f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
