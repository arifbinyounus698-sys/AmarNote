package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.local.AppSettings
import com.example.data.local.PaymentMethod
import com.example.data.local.SalaryRecord
import com.example.domain.CityCatalog
import com.example.domain.LocalizationHelper
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentLime
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentSkyBlue
import com.example.ui.theme.AccentSoftPink
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AccentViolet
import com.example.ui.viewmodel.LiveClockState
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

fun monthAccentColor(month1To12: Int): Color {
    return when (((month1To12 - 1) % 12 + 12) % 12) {
        0 -> AccentEmerald
        1 -> AccentPurple
        2 -> AccentOrange
        3 -> AccentSkyBlue
        4 -> AccentSoftPink
        5 -> AccentTeal
        6 -> AccentIndigo
        7 -> AccentAmber
        8 -> AccentRose
        9 -> AccentCyan
        10 -> AccentLime
        else -> AccentViolet
    }
}

@Composable
fun IconBadge3D(
    icon: ImageVector,
    tint: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        tint.copy(alpha = 0.25f),
                        tint.copy(alpha = 0.08f)
                    )
                )
            )
            .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

@Composable
fun NotebookPageCard(
    accentColor: Color,
    modifier: Modifier = Modifier,
    showRuledLines: Boolean = true,
    content: @Composable () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (showRuledLines) {
                        val step = 28.dp.toPx()
                        var y = step
                        while (y < size.height) {
                            drawLine(
                                color = lineColor,
                                start = Offset(22.dp.toPx(), y),
                                end = Offset(size.width - 12.dp.toPx(), y),
                                strokeWidth = 0.8f
                            )
                            y += step
                        }
                    }
                }
        ) {
            // Notebook colored spine + binder holes
            Column(
                modifier = Modifier
                    .width(12.dp)
                    .background(accentColor.copy(alpha = 0.88f))
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(surfaceColor.copy(alpha = 0.85f))
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SalaryEditorDialog(
    initialRecord: SalaryRecord?,
    isDuplicateMode: Boolean,
    prefillAmount: Double?,
    settings: AppSettings,
    clock: LiveClockState,
    onDismiss: () -> Unit,
    onSave: (SalaryRecord) -> Unit
) {
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits

    var amountText by remember {
        mutableStateOf(
            when {
                initialRecord != null -> initialRecord.amount.let {
                    if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
                }
                prefillAmount != null -> if (prefillAmount % 1.0 == 0.0) prefillAmount.toLong().toString() else prefillAmount.toString()
                else -> ""
            }
        )
    }

    var year by remember {
        mutableIntStateOf(
            if (initialRecord != null && !isDuplicateMode) initialRecord.year else clock.year
        )
    }
    var month by remember {
        mutableIntStateOf(
            if (initialRecord != null && !isDuplicateMode) initialRecord.month else clock.month
        )
    }
    var day by remember {
        mutableIntStateOf(
            if (initialRecord != null && !isDuplicateMode) initialRecord.day else clock.day
        )
    }
    var hourText by remember {
        mutableStateOf(
            (if (initialRecord != null && !isDuplicateMode) initialRecord.hour else clock.hour24)
                .toString().padStart(2, '0')
        )
    }
    var minuteText by remember {
        mutableStateOf(
            (if (initialRecord != null && !isDuplicateMode) initialRecord.minute else clock.minute)
                .toString().padStart(2, '0')
        )
    }
    var secondText by remember {
        mutableStateOf(
            (if (initialRecord != null && !isDuplicateMode) initialRecord.second else clock.second)
                .toString().padStart(2, '0')
        )
    }

    var selectedMethod by remember {
        mutableStateOf(initialRecord?.methodEnum ?: PaymentMethod.BKASH)
    }
    var receiverNumber by remember { mutableStateOf(initialRecord?.receiverNumber ?: "") }
    var senderNumber by remember { mutableStateOf(initialRecord?.senderNumber ?: "") }
    var bankName by remember { mutableStateOf(initialRecord?.bankName ?: "") }
    var accountLastDigits by remember { mutableStateOf(initialRecord?.accountLastDigits ?: "") }
    var transactionId by remember { mutableStateOf(initialRecord?.transactionId ?: "") }
    var comment by remember { mutableStateOf(initialRecord?.comment ?: "") }
    var extraNote by remember { mutableStateOf(initialRecord?.extraNote ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val computedDow = remember(year, month, day) {
        SalaryRecord.computeDayOfWeek(year, month, day)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            isDuplicateMode -> if (isBn) "বেতন রেকর্ড ডুপ্লিকেট" else "Duplicate Salary Record"
                            initialRecord != null -> if (isBn) "বেতন রেকর্ড সম্পাদনা" else "Edit Salary Record"
                            else -> if (isBn) "নতুন বেতন যোগ করুন" else "Add New Salary Record"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = if (isBn) "বন্ধ করুন" else "Close")
                    }
                }

                // Salary Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = false
                    },
                    label = {
                        Text(
                            if (isBn) "বেতনের পরিমাণ (${settings.currency.symbol} ${settings.currency.code}) *"
                            else "Salary Amount (${settings.currency.code}) *"
                        )
                    },
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text(if (isBn) "সঠিক টাকার পরিমাণ লিখুন" else "Enter a valid amount") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("salary_amount_input")
                )

                // Date Picker Row + Automatic Day/Month/Year/Weekday Banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBn) "বেতন পাওয়ার তারিখ ও বার" else "Date & Weekday",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${LocalizationHelper.formatDate(year, month, day, isBn, useBnDigits, style = settings.dateFormat)} (${LocalizationHelper.weekdayName(computedDow, isBn)})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${if (isBn) "মাস:" else "Month:"} ${LocalizationHelper.gregorianMonthName(month, isBn)} • ${if (isBn) "দিন:" else "Day:"} ${LocalizationHelper.formatInt(day, useBnDigits)} • ${if (isBn) "বছর:" else "Year:"} ${LocalizationHelper.formatInt(year, useBnDigits)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.testTag("pick_date_button")
                            ) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBn) "তারিখ" else "Date")
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        // Hour : Minute : Second Inputs + Now button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "সময় (ঘণ্টা : মিনিট : সেকেন্ড)" else "Time (HH : MM : SS)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    val now = LiveClockState.now()
                                    year = now.year
                                    month = now.month
                                    day = now.day
                                    hourText = now.hour24.toString().padStart(2, '0')
                                    minuteText = now.minute.toString().padStart(2, '0')
                                    secondText = now.second.toString().padStart(2, '0')
                                }
                            ) {
                                Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isBn) "বর্তমান সময়" else "Now")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = hourText,
                                onValueChange = { if (it.length <= 2) hourText = it.filter { c -> c.isDigit() } },
                                label = { Text(if (isBn) "ঘণ্টা (০-২৩)" else "Hour") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = minuteText,
                                onValueChange = { if (it.length <= 2) minuteText = it.filter { c -> c.isDigit() } },
                                label = { Text(if (isBn) "মিনিট" else "Min") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = secondText,
                                onValueChange = { if (it.length <= 2) secondText = it.filter { c -> c.isDigit() } },
                                label = { Text(if (isBn) "সেকেন্ড" else "Sec") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Payment Method Selection
                Text(
                    text = if (isBn) "টাকা গ্রহণের মাধ্যম নির্বাচন করুন" else "Select Payment Method",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        val selected = selectedMethod == method
                        FilterChip(
                            selected = selected,
                            onClick = { selectedMethod = method },
                            label = { Text(method.label(isBn)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = method.accentColor.copy(alpha = 0.18f),
                                selectedLabelColor = method.accentColor
                            ),
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = method.accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }

                // Optional Payment Details
                if (selectedMethod == PaymentMethod.BKASH || selectedMethod == PaymentMethod.NAGAD || selectedMethod == PaymentMethod.OTHER) {
                    OutlinedTextField(
                        value = receiverNumber,
                        onValueChange = { receiverNumber = it },
                        label = { Text(if (isBn) "যে নম্বরে টাকা পাঠানো হয়েছে (ঐচ্ছিক)" else "Receiver Number (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = senderNumber,
                        onValueChange = { senderNumber = it },
                        label = { Text(if (isBn) "যে নম্বর থেকে টাকা এসেছে (ঐচ্ছিক)" else "Sender Number (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedMethod == PaymentMethod.BANK || selectedMethod == PaymentMethod.OTHER) {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text(if (isBn) "ব্যাংকের নাম (ঐচ্ছিক)" else "Bank Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = accountLastDigits,
                        onValueChange = { if (it.length <= 8) accountLastDigits = it },
                        label = { Text(if (isBn) "অ্যাকাউন্টের শেষ সংখ্যা (ঐচ্ছিক)" else "Account Last Digits (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedMethod != PaymentMethod.CASH) {
                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text(if (isBn) "ট্রানজেকশন আইডি (ঐচ্ছিক)" else "Transaction ID (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(if (isBn) "মন্তব্য (যেমন: মাসিক মূল বেতন / বোনাস)" else "Comment (e.g., Monthly Salary / Bonus)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("salary_comment_input")
                )

                OutlinedTextField(
                    value = extraNote,
                    onValueChange = { extraNote = it },
                    label = { Text(if (isBn) "অতিরিক্ত নোট (ঐচ্ছিক)" else "Extra Note (Optional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isBn) "বাতিল" else "Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amountText.trim().toDoubleOrNull()
                            if (parsedAmount == null || parsedAmount <= 0.0) {
                                amountError = true
                                return@Button
                            }
                            val h = (hourText.toIntOrNull() ?: clock.hour24).coerceIn(0, 23)
                            val m = (minuteText.toIntOrNull() ?: clock.minute).coerceIn(0, 59)
                            val s = (secondText.toIntOrNull() ?: clock.second).coerceIn(0, 59)
                            val record = SalaryRecord(
                                id = if (isDuplicateMode) 0L else (initialRecord?.id ?: 0L),
                                amount = parsedAmount,
                                year = year,
                                month = month,
                                day = day,
                                hour = h,
                                minute = m,
                                second = s,
                                dayOfWeek = computedDow,
                                paymentMethod = selectedMethod.name,
                                receiverNumber = receiverNumber.trim(),
                                senderNumber = senderNumber.trim(),
                                bankName = bankName.trim(),
                                accountLastDigits = accountLastDigits.trim(),
                                transactionId = transactionId.trim(),
                                comment = comment.trim(),
                                extraNote = extraNote.trim(),
                                createdAtMillis = if (isDuplicateMode) System.currentTimeMillis() else (initialRecord?.createdAtMillis ?: System.currentTimeMillis()),
                                updatedAtMillis = System.currentTimeMillis()
                            )
                            onSave(record)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("save_salary_button")
                    ) {
                        Text(if (isBn) "সংরক্ষণ করুন" else "Save Record")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val utcInitMillis = remember(year, month, day) {
            GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(year, month - 1, day, 12, 0, 0)
            }.timeInMillis
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = utcInitMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val utcCal = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = millis
                            }
                            year = utcCal.get(Calendar.YEAR)
                            month = utcCal.get(Calendar.MONTH) + 1
                            day = utcCal.get(Calendar.DAY_OF_MONTH)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(if (isBn) "নির্বাচন" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun LocationPickerDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSelectLocation: (nameBn: String, nameEn: String, lat: Double, lng: Double) -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val isBn = settings.isBangla
    var search by remember { mutableStateOf("") }
    var customLat by remember { mutableStateOf(settings.latitude.toString()) }
    var customLng by remember { mutableStateOf(settings.longitude.toString()) }
    var customName by remember { mutableStateOf("") }

    val filteredCities = remember(search) {
        val q = search.trim().lowercase()
        if (q.isEmpty()) CityCatalog.cities
        else CityCatalog.cities.filter {
            it.nameBn.lowercase().contains(q) || it.nameEn.lowercase().contains(q)
        }
    }

    val gpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val loc = getLastKnownDeviceLocation(context)
            if (loc != null) {
                onSelectLocation("জিপিএস অবস্থান", "GPS Location", loc.first, loc.second)
                onDismiss()
            } else {
                onMessage(
                    if (isBn) "ডিভাইস থেকে জিপিএস অবস্থান পাওয়া যায়নি; শহর নির্বাচন করুন।"
                    else "Could not read GPS fix; please select a city below."
                )
            }
        } else {
            onMessage(
                if (isBn) "অবস্থান অনুমতি পাওয়া যায়নি; ম্যানুয়াল শহর নির্বাচন করুন।"
                else "Location permission denied; select a city manually."
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBn) "অবস্থান নির্ধারণ" else "Set Location") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val fineGranted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val coarseGranted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (fineGranted || coarseGranted) {
                            val loc = getLastKnownDeviceLocation(context)
                            if (loc != null) {
                                onSelectLocation("জিপিএস অবস্থান", "GPS Location", loc.first, loc.second)
                                onDismiss()
                            } else {
                                onMessage(
                                    if (isBn) "জিপিএস অবস্থান পাওয়া যায়নি; তালিকা থেকে শহর বাছাই করুন।"
                                    else "No GPS fix available; please pick a city from the list."
                                )
                            }
                        } else {
                            gpsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isBn) "জিপিএস (GPS) থেকে অবস্থান নিন" else "Use Device GPS")
                }

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text(if (isBn) "শহর খুঁজুন (যেমন: ঢাকা, চট্টগ্রাম)..." else "Search city...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCities) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectLocation(city.nameBn, city.nameEn, city.latitude, city.longitude)
                                    onDismiss()
                                }
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = city.displayName(isBn),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                HorizontalDivider()
                Text(
                    text = if (isBn) "অথবা কাস্টম অক্ষাংশ/দ্রাঘিমাংশ লিখুন:" else "Or enter custom Lat/Lng:",
                    style = MaterialTheme.typography.labelMedium
                )
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text(if (isBn) "স্থানের নাম" else "Place Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customLat,
                        onValueChange = { customLat = it },
                        label = { Text("Lat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = customLng,
                        onValueChange = { customLng = it },
                        label = { Text("Lng") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lat = customLat.toDoubleOrNull()
                    val lng = customLng.toDoubleOrNull()
                    if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                        val label = customName.trim().ifEmpty {
                            String.format("%.3f, %.3f", lat, lng)
                        }
                        onSelectLocation(label, label, lat, lng)
                        onDismiss()
                    }
                }
            ) {
                Text(if (isBn) "কাস্টম সংরক্ষণ" else "Save Custom")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বন্ধ করুন" else "Close")
            }
        }
    )
}

@SuppressLint("MissingPermission")
private fun getLastKnownDeviceLocation(context: Context): Pair<Double, Double>? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = lm.getProviders(true)
    var bestLoc: android.location.Location? = null
    for (p in providers) {
        val l = runCatching { lm.getLastKnownLocation(p) }.getOrNull() ?: continue
        if (bestLoc == null || l.accuracy < bestLoc.accuracy) {
            bestLoc = l
        }
    }
    return bestLoc?.let { it.latitude to it.longitude }
}
