package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.AppLanguage
import com.example.data.local.AppSettings
import com.example.data.local.AsrJuristicMethod
import com.example.data.local.BackupManager
import com.example.data.local.ClockFormat
import com.example.data.local.CurrencyOption
import com.example.data.local.DateFormatStyle
import com.example.data.local.DefaultDashboardOption
import com.example.data.local.NumeralStyle
import com.example.data.local.PrayerCalcMethod
import com.example.data.local.SecurityManager
import com.example.data.local.ThemeMode
import com.example.data.local.UserProfile
import com.example.ui.components.IconBadge3D
import com.example.ui.components.LocationPickerDialog
import com.example.ui.components.NotebookPageCard
import com.example.ui.components.ProfileAvatarView
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentSkyBlue
import com.example.ui.theme.AccentTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    profile: UserProfile,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectTheme: (ThemeMode) -> Unit,
    onSelectNumerals: (NumeralStyle) -> Unit,
    onSelectClockFormat: (ClockFormat) -> Unit,
    onSelectCurrency: (CurrencyOption) -> Unit,
    onSelectDateFormat: (DateFormatStyle) -> Unit,
    onSelectDefaultDashboard: (DefaultDashboardOption) -> Unit,
    onUpdateProfileDisplayPrefs: (showOnDash: Boolean, showPhone: Boolean, showEmail: Boolean, strictPrivate: Boolean) -> Unit,
    onSetOrChangePin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onSetBiometricEnabled: (Boolean) -> Unit,
    onSetAutoLockMinutes: (Int) -> Unit,
    onLockNow: () -> Unit,
    onSelectPrayerMethod: (PrayerCalcMethod) -> Unit,
    onSelectAsrMethod: (AsrJuristicMethod) -> Unit,
    onSelectLocation: (String, String, Double, Double) -> Unit,
    onOpenBackupScreen: () -> Unit,
    onOpenProfileScreen: () -> Unit,
    onDeletePersonalInfo: () -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = settings.isBangla
    var showLocationDialog by remember { mutableStateOf(false) }

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showDisablePinDialog by remember { mutableStateOf(false) }
    var showDeletePersonalConfirm by remember { mutableStateOf(false) }

    val biometricAvailable = remember(context) { SecurityManager.isBiometricAvailable(context) }

    val exportPersonalLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = BackupManager.buildPersonalProfileExportJson(profile)
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                    }
                    onMessage(if (isBn) "ব্যক্তিগত তথ্য এক্সপোর্ট হয়েছে" else "Personal info exported")
                } catch (e: Exception) {
                    onMessage(e.localizedMessage ?: "Export error")
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
            // 0. Personal Profile Quick Card
            item {
                NotebookPageCard(accentColor = AccentPurple, showRuledLines = false) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            ProfileAvatarView(
                                photoPath = profile.profilePhotoPath,
                                fullName = profile.fullName,
                                size = 48.dp
                            )
                            Column {
                                Text(
                                    text = profile.fullName.ifBlank { if (isBn) "ব্যক্তিগত প্রোফাইল" else "Personal Profile" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = profile.profession.ifBlank {
                                        if (isBn) "নাম, ছবি, পেশা ও পরিচয় সম্পাদনা" else "Edit photo, name, profession & bio"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(onClick = onOpenProfileScreen) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isBn) "খুলুন" else "Open")
                        }
                    }
                }
            }

            // 1. Language
            item {
                NotebookPageCard(accentColor = AccentEmerald, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconBadge3D(icon = Icons.Filled.Language, tint = AccentEmerald, contentDescription = null, size = 36.dp)
                            Text(
                                text = if (isBn) "অ্যাপের ভাষা (Language)" else "App Language",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = settings.language == AppLanguage.BANGLA,
                                onClick = { onSelectLanguage(AppLanguage.BANGLA) },
                                label = { Text("বাংলা") }
                            )
                            FilterChip(
                                selected = settings.language == AppLanguage.ENGLISH,
                                onClick = { onSelectLanguage(AppLanguage.ENGLISH) },
                                label = { Text("English") }
                            )
                        }
                    }
                }
            }

            // 2. Theme Mode (Light / Dark / System)
            item {
                NotebookPageCard(accentColor = AccentPurple, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconBadge3D(icon = Icons.Filled.Brightness6, tint = AccentPurple, contentDescription = null, size = 36.dp)
                            Text(
                                text = if (isBn) "থিম মোড (Theme)" else "Theme Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = settings.themeMode == ThemeMode.LIGHT,
                                onClick = { onSelectTheme(ThemeMode.LIGHT) },
                                label = { Text(if (isBn) "লাইট (Light)" else "Light") }
                            )
                            FilterChip(
                                selected = settings.themeMode == ThemeMode.DARK,
                                onClick = { onSelectTheme(ThemeMode.DARK) },
                                label = { Text(if (isBn) "ডার্ক (Dark)" else "Dark") }
                            )
                            FilterChip(
                                selected = settings.themeMode == ThemeMode.SYSTEM,
                                onClick = { onSelectTheme(ThemeMode.SYSTEM) },
                                label = { Text(if (isBn) "সিস্টেম" else "System") }
                            )
                        }
                    }
                }
            }

            // 3. Numeral Style & 12h/24h Clock
            item {
                NotebookPageCard(accentColor = AccentOrange, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconBadge3D(icon = Icons.Filled.Pin, tint = AccentOrange, contentDescription = null, size = 36.dp)
                            Text(
                                text = if (isBn) "সংখ্যা পদ্ধতি ও ঘড়ির ধরন" else "Numerals & Clock Format",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = settings.numeralStyle == NumeralStyle.BANGLA,
                                onClick = { onSelectNumerals(NumeralStyle.BANGLA) },
                                label = { Text(if (isBn) "বাংলা (০১২৩)" else "Bangla (০১২৩)") }
                            )
                            FilterChip(
                                selected = settings.numeralStyle == NumeralStyle.ENGLISH,
                                onClick = { onSelectNumerals(NumeralStyle.ENGLISH) },
                                label = { Text(if (isBn) "ইংরেজি (0123)" else "English (0123)") }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                            FilterChip(
                                selected = settings.clockFormat == ClockFormat.HOUR_12,
                                onClick = { onSelectClockFormat(ClockFormat.HOUR_12) },
                                label = { Text(if (isBn) "১২ ঘণ্টা" else "12 Hour") }
                            )
                            FilterChip(
                                selected = settings.clockFormat == ClockFormat.HOUR_24,
                                onClick = { onSelectClockFormat(ClockFormat.HOUR_24) },
                                label = { Text(if (isBn) "২৪ ঘণ্টা" else "24 Hour") }
                            )
                        }
                    }
                }
            }

            // 4. App Customization (Currency, Date Format, Default Dashboard, Profile Display Preferences)
            item {
                NotebookPageCard(accentColor = AccentAmber, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconBadge3D(icon = Icons.Filled.CurrencyExchange, tint = AccentAmber, contentDescription = null, size = 36.dp)
                            Text(
                                text = if (isBn) "অ্যাপ কাস্টমাইজেশন" else "App Customization",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (isBn) "মুদ্রা নির্বাচন (Currency):" else "Currency Selection:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CurrencyOption.entries.forEach { curr ->
                                FilterChip(
                                    selected = settings.currency == curr,
                                    onClick = { onSelectCurrency(curr) },
                                    label = { Text(curr.label(isBn)) },
                                    modifier = Modifier.testTag("currency_chip_${curr.code}")
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isBn) "তারিখের ফরম্যাট (Date Format):" else "Date Format:",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DateFormatStyle.entries.forEach { df ->
                                FilterChip(
                                    selected = settings.dateFormat == df,
                                    onClick = { onSelectDateFormat(df) },
                                    label = { Text(df.label(isBn)) }
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Home, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isBn) "অ্যাপ চালু হলে ডিফল্ট স্ক্রিন:" else "Default Startup Screen:",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DefaultDashboardOption.entries.forEach { opt ->
                                FilterChip(
                                    selected = settings.defaultDashboard == opt,
                                    onClick = { onSelectDefaultDashboard(opt) },
                                    label = { Text(opt.label(isBn)) }
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Visibility, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isBn) "প্রোফাইল প্রদর্শন পছন্দ (Profile Display):" else "Profile Display Preferences:",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        SwitchPreferenceRow(
                            title = if (isBn) "ড্যাশবোর্ডে প্রোফাইল কার্ড দেখান" else "Show Profile Card on Dashboard",
                            checked = settings.showProfileOnDashboard,
                            onCheckedChange = {
                                onUpdateProfileDisplayPrefs(it, settings.showPhoneInProfileCard, settings.showEmailInProfileCard, settings.strictPrivateMode)
                            }
                        )
                        SwitchPreferenceRow(
                            title = if (isBn) "প্রোফাইল কার্ডে মোবাইল নম্বর দেখান" else "Show Mobile Number on Card",
                            checked = settings.showPhoneInProfileCard,
                            onCheckedChange = {
                                onUpdateProfileDisplayPrefs(settings.showProfileOnDashboard, it, settings.showEmailInProfileCard, settings.strictPrivateMode)
                            }
                        )
                        SwitchPreferenceRow(
                            title = if (isBn) "প্রোফাইল কার্ডে ইমেইল দেখান" else "Show Email on Card",
                            checked = settings.showEmailInProfileCard,
                            onCheckedChange = {
                                onUpdateProfileDisplayPrefs(settings.showProfileOnDashboard, settings.showPhoneInProfileCard, it, settings.strictPrivateMode)
                            }
                        )
                        SwitchPreferenceRow(
                            title = if (isBn) "কঠোর প্রাইভেসি মোড (তথ্য কেবল অফলাইন)" else "Strict Private Mode (Local Only)",
                            checked = settings.strictPrivateMode,
                            onCheckedChange = {
                                onUpdateProfileDisplayPrefs(settings.showProfileOnDashboard, settings.showPhoneInProfileCard, settings.showEmailInProfileCard, it)
                            }
                        )
                    }
                }
            }

            // 5. Security & Privacy (Salted PBKDF2 PIN, Biometric Lock, Auto-Lock, Lock Now, Delete/Export Personal Data)
            item {
                NotebookPageCard(accentColor = AccentRose, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconBadge3D(icon = Icons.Filled.Security, tint = AccentRose, contentDescription = null, size = 36.dp)
                            Column {
                                Text(
                                    text = if (isBn) "নিরাপত্তা ও প্রাইভেসি (Security)" else "Security & Privacy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (settings.pinEnabled) {
                                        if (isBn) "PIN লক সক্রিয় আছে (সাল্টেড এনক্রিপ্টেড হ্যাশ)" else "PIN Lock Active (Salted Hash)"
                                    } else {
                                        if (isBn) "অ্যাপ সুরক্ষিত রাখতে ৪-সংখ্যার PIN সেট করুন" else "Set a 4-digit PIN to protect AmarNote"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (settings.pinEnabled) AccentEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showPinSetupDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("setup_change_pin_button")
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (settings.pinEnabled) {
                                        if (isBn) "PIN পরিবর্তন" else "Change PIN"
                                    } else {
                                        if (isBn) "PIN লক সেট করুন" else "Set PIN Lock"
                                    }
                                )
                            }
                            if (settings.pinEnabled) {
                                OutlinedButton(
                                    onClick = { showDisablePinDialog = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isBn) "PIN বন্ধ করুন" else "Disable PIN")
                                }
                            }
                        }

                        if (settings.pinEnabled) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp),
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = AccentRose)
                                            Column {
                                                Text(
                                                    text = if (isBn) "ফিঙ্গারপ্রিন্ট / বায়োমেট্রিক লক" else "Fingerprint / Biometric Lock",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (!biometricAvailable) {
                                                    Text(
                                                        text = if (isBn) "এই ডিভাইসে বায়োমেট্রিক সক্রিয় নেই" else "Biometric not enrolled on device",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        Switch(
                                            checked = settings.biometricEnabled,
                                            enabled = biometricAvailable,
                                            onCheckedChange = onSetBiometricEnabled
                                        )
                                    }

                                    Text(
                                        text = if (isBn) "অটো-লক সময় (Auto-Lock):" else "Auto-Lock Timer:",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    val lockOptions = listOf(
                                        0 to (if (isBn) "সাথে সাথে" else "Immediate"),
                                        1 to (if (isBn) "১ মিনিট" else "1 min"),
                                        5 to (if (isBn) "৫ মিনিট" else "5 min"),
                                        15 to (if (isBn) "১৫ মিনিট" else "15 min"),
                                        -1 to (if (isBn) "ম্যানুয়াল" else "Manual")
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        lockOptions.forEach { (mins, label) ->
                                            FilterChip(
                                                selected = settings.autoLockMinutes == mins,
                                                onClick = { onSetAutoLockMinutes(mins) },
                                                label = { Text(label) }
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = onLockNow,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (isBn) "এখনই অ্যাপ লক করুন (Lock Now)" else "Lock App Now")
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { exportPersonalLauncher.launch("AmarNote_Personal_Profile.json") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isBn) "তথ্য এক্সপোর্ট" else "Export Info")
                            }
                            OutlinedButton(
                                onClick = { showDeletePersonalConfirm = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isBn) "ব্যক্তিগত তথ্য মুছুন" else "Delete Personal Info")
                            }
                        }
                    }
                }
            }

            // 6. Prayer Calculation Method, Asr Method, & Location
            item {
                NotebookPageCard(accentColor = AccentSkyBlue, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconBadge3D(icon = Icons.Filled.Mosque, tint = AccentSkyBlue, contentDescription = null, size = 36.dp)
                            Text(
                                text = if (isBn) "নামাজ ও অবস্থান সেটিংস" else "Prayer & Location Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isBn) "বর্তমান অবস্থান: ${settings.displayLocationName()}" else "Location: ${settings.displayLocationName()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(onClick = { showLocationDialog = true }) {
                            Icon(Icons.Filled.EditLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isBn) "অবস্থান পরিবর্তন করুন" else "Change Location")
                        }
                        Text(
                            text = if (isBn) "আসরের পদ্ধতি:" else "Asr Juristic Method:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AsrJuristicMethod.entries.forEach { method ->
                                FilterChip(
                                    selected = settings.asrMethod == method,
                                    onClick = { onSelectAsrMethod(method) },
                                    label = { Text(method.title(isBn)) }
                                )
                            }
                        }
                        Text(
                            text = if (isBn) "হিসাব পদ্ধতি:" else "Calculation Method:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PrayerCalcMethod.entries.forEach { method ->
                                FilterChip(
                                    selected = settings.prayerMethod == method,
                                    onClick = { onSelectPrayerMethod(method) },
                                    label = { Text(method.title(isBn)) }
                                )
                            }
                        }
                    }
                }
            }

            // 7. Data Backup & Restore Shortcut
            item {
                NotebookPageCard(accentColor = AccentIndigo, showRuledLines = false) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconBadge3D(icon = Icons.Filled.Backup, tint = AccentIndigo, contentDescription = null, size = 40.dp)
                            Column {
                                Text(
                                    text = if (isBn) "ডেটা ব্যাকআপ ও Restore" else "Data Backup & Restore",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "AES-256 JSON, CSV, PDF",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(onClick = onOpenBackupScreen) {
                            Text(if (isBn) "খুলুন" else "Open")
                        }
                    }
                }
            }

            // 8. About AmarNote & Privacy Information
            item {
                NotebookPageCard(accentColor = AccentTeal, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconBadge3D(icon = Icons.Filled.Info, tint = AccentTeal, contentDescription = null, size = 36.dp)
                            Text(
                                text = if (isBn) "অ্যাপ সম্পর্কে (AmarNote)" else "About AmarNote",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isBn) "AmarNote একটি সম্পূর্ণ অফলাইন ব্যক্তিগত বেতন খাতা, ১০+ বছরের আর্থিক সংরক্ষণাগার, ব্যক্তিগত প্রোফাইল, নোটবুক, রিমাইন্ডার, নামাজ/মাকরুহ সময়সূচি, কিবলা কম্পাস এবং ডিজিটাল ক্যালকুলেটর অ্যাপ।"
                            else "AmarNote is an offline-first personal salary ledger, 10+ year archive, personal profile, notes & reminders, astronomical prayer & Makruh time calculator, Qibla compass, and digital utility calculator.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isBn) "প্রাইভেসি তথ্য: আপনার সমস্ত হিসাব, প্রোফাইল, ছবি ও নোট শুধুমাত্র আপনার ফোনের লোকাল ডেটাবেসে সংরক্ষিত থাকে। PIN কেবল সাল্টেড PBKDF2 হ্যাশ আকারে সংরক্ষিত হয়।"
                                else "Privacy: All salary records, profile, photos, and notes stay locally on your device. PINs are stored exclusively as salted PBKDF2 hashes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLocationDialog) {
        LocationPickerDialog(
            settings = settings,
            onDismiss = { showLocationDialog = false },
            onSelectLocation = onSelectLocation,
            onMessage = onMessage
        )
    }

    if (showPinSetupDialog) {
        PinSetupDialog(
            isChangeMode = settings.pinEnabled,
            isBangla = isBn,
            onVerifyCurrent = { oldPin ->
                SecurityManager.verifyPin(oldPin, settings.pinSalt, settings.pinHash)
            },
            onDismiss = { showPinSetupDialog = false },
            onSaveNewPin = { newPin ->
                onSetOrChangePin(newPin)
                showPinSetupDialog = false
            }
        )
    }

    if (showDisablePinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var pinErr by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showDisablePinDialog = false },
            title = { Text(if (isBn) "PIN লক বন্ধ করুন" else "Disable PIN Lock") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (isBn) "PIN বন্ধ করতে বর্তমান ৪-সংখ্যার PIN লিখুন:" else "Enter current 4-digit PIN to disable:")
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = {
                            if (it.length <= 4) currentPin = it.filter { c -> c.isDigit() }
                            pinErr = null
                        },
                        label = { Text(if (isBn) "বর্তমান PIN" else "Current PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = pinErr != null,
                        supportingText = pinErr?.let { err -> { Text(err) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (SecurityManager.verifyPin(currentPin, settings.pinSalt, settings.pinHash)) {
                            onDisablePin()
                            showDisablePinDialog = false
                        } else {
                            pinErr = if (isBn) "ভুল PIN!" else "Incorrect PIN!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "বন্ধ করুন" else "Disable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisablePinDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    if (showDeletePersonalConfirm) {
        AlertDialog(
            onDismissRequest = { showDeletePersonalConfirm = false },
            title = { Text(if (isBn) "ব্যক্তিগত প্রোফাইল তথ্য মুছবেন?" else "Delete Personal Info?") },
            text = {
                Text(
                    if (isBn) "আপনার প্রোফাইল নাম, ছবি, কভার ফটো ও ব্যক্তিগত পরিচয় মুছে ফেলা হবে। আপনার বেতন হিসাব ও নোট অক্ষত থাকবে।"
                    else "Your personal profile details and photos will be removed. Your salary ledger and notes will remain untouched."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePersonalInfo()
                        showDeletePersonalConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "মুছে ফেলুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePersonalConfirm = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun SwitchPreferenceRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PinSetupDialog(
    isChangeMode: Boolean,
    isBangla: Boolean,
    onVerifyCurrent: (String) -> Boolean,
    onDismiss: () -> Unit,
    onSaveNewPin: (String) -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isChangeMode) {
                    if (isBangla) "PIN পরিবর্তন করুন" else "Change 4-Digit PIN"
                } else {
                    if (isBangla) "নতুন ৪-সংখ্যার PIN সেট করুন" else "Set New 4-Digit PIN"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isChangeMode) {
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = {
                            if (it.length <= 4) currentPin = it.filter { c -> c.isDigit() }
                            errorMsg = null
                        },
                        label = { Text(if (isBangla) "বর্তমান PIN" else "Current PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        if (it.length <= 4) newPin = it.filter { c -> c.isDigit() }
                        errorMsg = null
                    },
                    label = { Text(if (isBangla) "নতুন ৪-সংখ্যার PIN" else "New 4-Digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_pin_input")
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 4) confirmPin = it.filter { c -> c.isDigit() }
                        errorMsg = null
                    },
                    label = { Text(if (isBangla) "PIN পুনরায় লিখুন" else "Confirm PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_pin_input")
                )
                errorMsg?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isChangeMode && !onVerifyCurrent(currentPin)) {
                        errorMsg = if (isBangla) "বর্তমান PIN সঠিক নয়" else "Current PIN is incorrect"
                        return@Button
                    }
                    if (newPin.length != 4) {
                        errorMsg = if (isBangla) "অবশ্যই ৪ সংখ্যার PIN দিন" else "PIN must be 4 digits"
                        return@Button
                    }
                    if (newPin != confirmPin) {
                        errorMsg = if (isBangla) "দুটি PIN একরকম নয়" else "PINs do not match"
                        return@Button
                    }
                    onSaveNewPin(newPin)
                },
                modifier = Modifier.testTag("save_pin_button")
            ) {
                Text(if (isBangla) "সংরক্ষণ" else "Save PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}
