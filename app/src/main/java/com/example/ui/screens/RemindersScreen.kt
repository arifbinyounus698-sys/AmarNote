package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AppReminder
import com.example.data.local.AppSettings
import com.example.data.local.ClockFormat
import com.example.data.local.ReminderCategory
import com.example.data.local.ReminderScheduler
import com.example.domain.LocalizationHelper
import com.example.ui.components.IconBadge3D
import com.example.ui.components.NotebookPageCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.viewmodel.LiveClockState
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

@Composable
fun RemindersScreen(
    reminders: List<AppReminder>,
    settings: AppSettings,
    clock: LiveClockState,
    onSaveReminder: (AppReminder) -> Unit,
    onToggleComplete: (AppReminder) -> Unit,
    onDeleteReminder: (AppReminder) -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits
    val is12Hour = settings.clockFormat == ClockFormat.HOUR_12

    var hasNotifPerm by remember { mutableStateOf(ReminderScheduler.hasNotificationPermission(context)) }
    var showEditor by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<AppReminder?>(null) }
    var reminderToDelete by remember { mutableStateOf<AppReminder?>(null) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPerm = granted
        onMessage(
            if (granted) {
                if (isBn) "রিমাইন্ডার নোটিফিকেশন চালু হয়েছে" else "Reminder notifications enabled"
            } else {
                if (isBn) "নোটিফিকেশন অনুমতি না দিলেও সব রিমাইন্ডার অ্যাপের মধ্যে দেখা যাবে।" else "Notifications denied — reminders remain visible inside the app."
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "স্মরণিকা ও বিল রিমাইন্ডার" else "Reminders & Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            editingReminder = null
                            showEditor = true
                        },
                        modifier = Modifier.testTag("add_reminder_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (isBn) "নতুন রিমাইন্ডার" else "Add Reminder")
                    }
                }

                if (!hasNotifPerm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Surface(
                        color = AccentAmber.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.NotificationsOff, contentDescription = null, tint = AccentAmber)
                                Text(
                                    text = if (isBn) "নোটিফিকেশন অনুমতি বন্ধ আছে (অ্যাপের মধ্যে রিমাইন্ডার দেখা যাবে)।"
                                    else "Notification permission off (reminders still visible in-app).",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            TextButton(onClick = {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }) {
                                Text(if (isBn) "অনুমতি দিন" else "Allow")
                            }
                        }
                    }
                }
            }

            if (reminders.isEmpty()) {
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
                            icon = Icons.Filled.NotificationsActive,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null,
                            size = 64.dp
                        )
                        Text(
                            text = if (isBn) "কোনো রিমাইন্ডার সেট করা নেই" else "No Reminders Set",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "বেতন পাওয়ার তারিখ, গুরুত্বপূর্ণ বিল, কাজ বা বিশেষ তারিখ মনে রাখতে রিমাইন্ডার যোগ করুন।"
                            else "Add reminders for salary dates, bills, tasks, or special dates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reminders, key = { it.id }) { rem ->
                        val cat = rem.categoryEnum
                        val dateText = LocalizationHelper.formatDate(
                            rem.year, rem.month, rem.day, isBn, useBnDigits, settings.dateFormat
                        )
                        val timeText = LocalizationHelper.formatTime(
                            rem.hour, rem.minute, null, is12Hour, isBn, useBnDigits
                        )
                        NotebookPageCard(accentColor = if (rem.isCompleted) AccentEmerald else cat.accentColor, showRuledLines = false) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = cat.accentColor.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = cat.label(isBn),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = cat.accentColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = "$dateText • $timeText",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { onToggleComplete(rem) }) {
                                        Icon(
                                            imageVector = if (rem.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = if (isBn) "সম্পন্ন" else "Complete",
                                            tint = if (rem.isCompleted) AccentEmerald else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rem.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (rem.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                        if (rem.description.isNotBlank()) {
                                            Text(
                                                text = rem.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (rem.notifySystem) {
                                            if (isBn) "লোকাল নোটিফিকেশন চালু" else "Local Notification On"
                                        } else {
                                            if (isBn) "শুধু অ্যাপের মধ্যে" else "In-App Only"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row {
                                        IconButton(onClick = {
                                            editingReminder = rem
                                            showEditor = true
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { reminderToDelete = rem }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        ReminderEditorDialog(
            initial = editingReminder,
            settings = settings,
            clock = clock,
            onDismiss = { showEditor = false },
            onSave = { rem ->
                if (rem.notifySystem && !hasNotifPerm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                onSaveReminder(rem)
                showEditor = false
            }
        )
    }

    reminderToDelete?.let { rem ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text(if (isBn) "রিমাইন্ডার মুছে ফেলবেন?" else "Delete Reminder?") },
            text = { Text("'${rem.title}'") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteReminder(rem)
                        reminderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "মুছুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ReminderEditorDialog(
    initial: AppReminder?,
    settings: AppSettings,
    clock: LiveClockState,
    onDismiss: () -> Unit,
    onSave: (AppReminder) -> Unit
) {
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var category by remember { mutableStateOf(initial?.categoryEnum ?: ReminderCategory.SALARY_DATE) }
    var year by remember { mutableIntStateOf(initial?.year ?: clock.year) }
    var month by remember { mutableIntStateOf(initial?.month ?: clock.month) }
    var day by remember { mutableIntStateOf(initial?.day ?: clock.day) }
    var hourText by remember { mutableStateOf((initial?.hour ?: 9).toString().padStart(2, '0')) }
    var minuteText by remember { mutableStateOf((initial?.minute ?: 0).toString().padStart(2, '0')) }
    var notifySystem by remember { mutableStateOf(initial?.notifySystem ?: true) }
    var showDatePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initial != null) (if (isBn) "রিমাইন্ডার সম্পাদনা" else "Edit Reminder") else (if (isBn) "নতুন রিমাইন্ডার" else "New Reminder"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ReminderCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label(isBn)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text(if (isBn) "শিরোনাম *" else "Reminder Title *") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isBn) "বিবরণ (ঐচ্ছিক)" else "Description (Optional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(LocalizationHelper.formatDate(year, month, day, isBn, useBnDigits, settings.dateFormat))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { if (it.length <= 2) hourText = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isBn) "ঘণ্টা (০-২৩)" else "Hour (0-23)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { if (it.length <= 2) minuteText = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isBn) "মিনিট" else "Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "লোকাল নোটিফিকেশন পাঠান" else "Send Local Notification",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = notifySystem,
                        onCheckedChange = { notifySystem = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text(if (isBn) "বাতিল" else "Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.trim().isEmpty()) {
                                titleError = true
                                return@Button
                            }
                            val h = (hourText.toIntOrNull() ?: 9).coerceIn(0, 23)
                            val m = (minuteText.toIntOrNull() ?: 0).coerceIn(0, 59)
                            val cal = GregorianCalendar(year, month - 1, day, h, m, 0)
                            onSave(
                                AppReminder(
                                    id = initial?.id ?: 0L,
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = category.name,
                                    year = year,
                                    month = month,
                                    day = day,
                                    hour = h,
                                    minute = m,
                                    triggerAtMillis = cal.timeInMillis,
                                    isCompleted = initial?.isCompleted ?: false,
                                    notifySystem = notifySystem,
                                    createdAtMillis = initial?.createdAtMillis ?: System.currentTimeMillis()
                                )
                            )
                        }
                    ) {
                        Text(if (isBn) "সংরক্ষণ" else "Save")
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
        val state = rememberDatePickerState(initialSelectedDateMillis = utcInitMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { ms ->
                            val c = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply { timeInMillis = ms }
                            year = c.get(Calendar.YEAR)
                            month = c.get(Calendar.MONTH) + 1
                            day = c.get(Calendar.DAY_OF_MONTH)
                        }
                        showDatePicker = false
                    }
                ) { Text(if (isBn) "নির্বাচন" else "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(if (isBn) "বাতিল" else "Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
