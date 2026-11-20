package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.AppSettings
import com.example.data.local.BackupManager
import com.example.data.local.SalaryRecord
import com.example.data.local.SecurityManager
import com.example.domain.LocalizationHelper
import com.example.ui.components.IconBadge3D
import com.example.ui.components.NotebookPageCard
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentSkyBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BackupScreen(
    allSalaries: List<SalaryRecord>,
    currentYear: Int,
    currentMonth: Int,
    settings: AppSettings,
    generateJsonBackup: suspend (String?) -> String,
    generateCsv: suspend () -> String,
    onRestoreJson: (jsonText: String, replaceExisting: Boolean) -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits

    var encryptBackup by remember { mutableStateOf(false) }
    var backupPassphrase by remember { mutableStateOf("") }
    var showBackupPass by remember { mutableStateOf(false) }

    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var pendingEncryptedText by remember { mutableStateOf<String?>(null) }
    var decryptPassphrase by remember { mutableStateOf("") }
    var decryptError by remember { mutableStateOf<String?>(null) }
    var safetyPlainSaveMode by remember { mutableStateOf(false) }

    val jsonSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val pass = if (safetyPlainSaveMode) {
                        null
                    } else if (encryptBackup) {
                        backupPassphrase.trim()
                    } else {
                        null
                    }
                    safetyPlainSaveMode = false
                    val json = generateJsonBackup(pass)
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                    }
                    onMessage(
                        if (pass != null) {
                            if (isBn) "এনক্রিপ্টেড JSON ব্যাকআপ সংরক্ষিত হয়েছে" else "Encrypted JSON backup saved"
                        } else {
                            if (isBn) "JSON ব্যাকআপ সফলভাবে সংরক্ষিত হয়েছে" else "JSON backup saved successfully"
                        }
                    )
                } catch (e: Exception) {
                    safetyPlainSaveMode = false
                    onMessage(e.localizedMessage ?: "Backup Error")
                }
            }
        } else {
            safetyPlainSaveMode = false
        }
    }

    val jsonOpenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val text = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    }
                    if (text.isNullOrBlank()) {
                        onMessage(if (isBn) "ফাইলটি খালি" else "Selected file is empty")
                    } else if (SecurityManager.isEncryptedBackup(text)) {
                        decryptPassphrase = ""
                        decryptError = null
                        pendingEncryptedText = text
                    } else {
                        withContext(Dispatchers.IO) { BackupManager.parseJsonBackup(text) }
                        pendingRestoreJson = text
                    }
                } catch (e: Exception) {
                    onMessage(if (isBn) "সঠিক AmarNote ব্যাকআপ ফাইল নয়" else "Invalid AmarNote backup file")
                }
            }
        }
    }

    val csvSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val csv = generateCsv()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                    }
                    onMessage(if (isBn) "সম্পূর্ণ CSV ফাইল সংরক্ষিত হয়েছে" else "Full CSV exported")
                } catch (e: Exception) {
                    onMessage(e.localizedMessage ?: "CSV Error")
                }
            }
        }
    }

    val monthlyPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val monthRecords = allSalaries.filter { it.year == currentYear && it.month == currentMonth }
                    val mTitle = LocalizationHelper.gregorianMonthName(currentMonth, isBn)
                    val yTitle = LocalizationHelper.formatInt(currentYear, useBnDigits)
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            BackupManager.writePdfReport(
                                outputStream = out,
                                title = "$mTitle $yTitle",
                                subtitle = if (isBn) "মাসিক বেতন রিপোর্ট" else "Monthly Salary Report",
                                records = monthRecords,
                                settings = settings
                            )
                        }
                    }
                    onMessage(if (isBn) "মাসিক PDF সংরক্ষিত হয়েছে" else "Monthly PDF saved")
                } catch (e: Exception) {
                    onMessage(e.localizedMessage ?: "PDF Error")
                }
            }
        }
    }

    val yearlyPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val yearRecords = allSalaries.filter { it.year == currentYear }
                    val yTitle = LocalizationHelper.formatInt(currentYear, useBnDigits)
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            BackupManager.writePdfReport(
                                outputStream = out,
                                title = if (isBn) "$yTitle বার্ষিক রিপোর্ট" else "Annual Report $yTitle",
                                subtitle = if (isBn) "বার্ষিক বেতন হিসাব" else "Yearly Salary Summary",
                                records = yearRecords,
                                settings = settings
                            )
                        }
                    }
                    onMessage(if (isBn) "বার্ষিক PDF সংরক্ষিত হয়েছে" else "Yearly PDF saved")
                } catch (e: Exception) {
                    onMessage(e.localizedMessage ?: "PDF Error")
                }
            }
        }
    }

    fun canProceedEncryptedBackup(): Boolean {
        if (encryptBackup && backupPassphrase.trim().length < 4) {
            onMessage(
                if (isBn) "এনক্রিপ্ট করার জন্য কমপক্ষে ৪ অক্ষরের পাসওয়ার্ড লিখুন"
                else "Enter at least a 4-character passphrase to encrypt"
            )
            return false
        }
        return true
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Data Safety Status Banner
            item {
                NotebookPageCard(accentColor = AccentEmerald, showRuledLines = false) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconBadge3D(
                            icon = Icons.Filled.Shield,
                            tint = AccentEmerald,
                            contentDescription = null,
                            size = 48.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "সম্পূর্ণ অফলাইন ও সুরক্ষিত ডেটাবেস" else "100% Offline Local Database",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBn) "মোট সংরক্ষিত বেতন রেকর্ড: ${LocalizationHelper.formatInt(allSalaries.size, useBnDigits)} টি। প্রোফাইল, ছবি, নোট, রিমাইন্ডার ও ১০ বছরের হিসাব সবকিছু আপনার ডিভাইসেই নিরাপদ থাকে।"
                                else "Total saved salary records: ${LocalizationHelper.formatInt(allSalaries.size, useBnDigits)}. Profile, photos, notes, reminders & 10-year records persist safely offline.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // JSON Backup & Restore (with optional AES-256-GCM Encryption)
            item {
                NotebookPageCard(accentColor = AccentIndigo, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconBadge3D(icon = Icons.Filled.Download, tint = AccentIndigo, contentDescription = null, size = 40.dp)
                            Column {
                                Text(
                                    text = if (isBn) "JSON ব্যাকআপ ও স্থানান্তর" else "JSON Backup & Device Transfer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "বেতন রেকর্ড, প্রোফাইল, ছবি, ব্যক্তিগত নোট, রিমাইন্ডার ও সেটিংস সব একসাথে ব্যাকআপ নিন।"
                                    else "Back up salaries, profile, photos, personal notes, reminders & settings together.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                                        Icon(Icons.Filled.EnhancedEncryption, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(20.dp))
                                        Column {
                                            Text(
                                                text = if (isBn) "ব্যাকআপ এনক্রিপ্ট করুন (AES-256)" else "Encrypt Backup (AES-256)",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = if (isBn) "পাসওয়ার্ড দিয়ে ফাইল লক রাখুন" else "Protect backup file with passphrase",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = encryptBackup,
                                        onCheckedChange = { encryptBackup = it },
                                        modifier = Modifier.testTag("encrypt_backup_switch")
                                    )
                                }

                                if (encryptBackup) {
                                    OutlinedTextField(
                                        value = backupPassphrase,
                                        onValueChange = { backupPassphrase = it },
                                        label = { Text(if (isBn) "ব্যাকআপ পাসওয়ার্ড লিখুন (কমপক্ষে ৪ অক্ষর)" else "Backup Passphrase (min 4 chars)") },
                                        singleLine = true,
                                        visualTransformation = if (showBackupPass) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { showBackupPass = !showBackupPass }) {
                                                Icon(
                                                    if (showBackupPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("backup_passphrase_input")
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (canProceedEncryptedBackup()) {
                                        val suffix = if (encryptBackup) "_Encrypted" else ""
                                        jsonSaveLauncher.launch("AmarNote_Backup_${currentYear}${suffix}.json")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("json_backup_button")
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBn) "JSON ব্যাকআপ" else "JSON Backup")
                            }
                            OutlinedButton(
                                onClick = {
                                    if (canProceedEncryptedBackup()) {
                                        scope.launch {
                                            val pass = if (encryptBackup) backupPassphrase.trim() else null
                                            val json = generateJsonBackup(pass)
                                            val suffix = if (pass != null) "_Encrypted" else ""
                                            BackupManager.shareTempFile(
                                                context = context,
                                                fileName = "AmarNote_Backup_${currentYear}${suffix}.json",
                                                mimeType = "application/json"
                                            ) { it.write(json.toByteArray(Charsets.UTF_8)) }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBn) "শেয়ার" else "Share")
                            }
                        }

                        OutlinedButton(
                            onClick = { jsonOpenLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("json_restore_button")
                        ) {
                            Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "JSON ফাইল থেকে Restore করুন" else "Restore from JSON Backup")
                        }
                    }
                }
            }

            // CSV Export
            item {
                NotebookPageCard(accentColor = AccentOrange, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconBadge3D(icon = Icons.Filled.TableChart, tint = AccentOrange, contentDescription = null, size = 40.dp)
                            Column {
                                Text(
                                    text = if (isBn) "CSV স্প্রেডশিট এক্সপোর্ট" else "CSV Spreadsheet Export",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "Excel বা Google Sheets-এ খোলার জন্য সম্পূর্ণ বেতন তালিকা।"
                                    else "Full salary history formatted for Excel and Google Sheets.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { csvSaveLauncher.launch("AmarNote_All_Salaries.csv") },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "সব হিসাব CSV এক্সপোর্ট করুন" else "Export All Records (CSV)")
                        }
                    }
                }
            }

            // PDF Reports (Monthly & Yearly)
            item {
                NotebookPageCard(accentColor = AccentPurple, showRuledLines = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconBadge3D(icon = Icons.Filled.PictureAsPdf, tint = AccentPurple, contentDescription = null, size = 40.dp)
                            Column {
                                Text(
                                    text = if (isBn) "PDF রিপোর্ট তৈরি করুন" else "Generate PDF Reports",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "মাসভিত্তিক এবং বার্ষিক প্রিন্টযোগ্য PDF রিপোর্ট।"
                                    else "Printable monthly and annual salary PDF documents.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { monthlyPdfLauncher.launch("AmarNote_Monthly_${currentYear}_${currentMonth}.pdf") },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isBn) "মাসিক PDF" else "Monthly PDF")
                            }
                            OutlinedButton(
                                onClick = { yearlyPdfLauncher.launch("AmarNote_Yearly_${currentYear}.pdf") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isBn) "বার্ষিক PDF" else "Yearly PDF")
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    color = AccentSkyBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = AccentSkyBlue)
                        Text(
                            text = if (isBn) "ব্যাকআপ ও এক্সপোর্ট সম্পূর্ণ অফলাইনে আপনার ফোনেই কাজ করে—কোনো ইন্টারনেট বা ক্লাউড অ্যাকাউন্ট প্রয়োজন নেই।"
                            else "All backups and PDF/CSV exports run 100% offline on your device without needing an account.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    // Encrypted Backup Passphrase Prompt Dialog
    pendingEncryptedText?.let { cipherText ->
        AlertDialog(
            onDismissRequest = { pendingEncryptedText = null },
            title = {
                Text(if (isBn) "এনক্রিপ্টেড ব্যাকআপ খুলুন" else "Decrypt Backup File")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBn) "এই ব্যাকআপ ফাইলটি পাসওয়ার্ড দিয়ে সুরক্ষিত। আনলক করতে পাসওয়ার্ড লিখুন:"
                        else "This backup file is encrypted. Enter your passphrase to unlock:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = decryptPassphrase,
                        onValueChange = {
                            decryptPassphrase = it
                            decryptError = null
                        },
                        label = { Text(if (isBn) "পাসওয়ার্ড" else "Passphrase") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = decryptError != null,
                        supportingText = decryptError?.let { err -> { Text(err) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val plain = withContext(Dispatchers.Default) {
                                    SecurityManager.decryptBackupJson(cipherText, decryptPassphrase.trim())
                                }
                                withContext(Dispatchers.IO) { BackupManager.parseJsonBackup(plain) }
                                pendingEncryptedText = null
                                pendingRestoreJson = plain
                            } catch (_: Exception) {
                                decryptError = if (isBn) "পাসওয়ার্ড সঠিক নয় বা ফাইল ক্ষতিগ্রস্ত" else "Incorrect passphrase or corrupted file"
                            }
                        }
                    }
                ) {
                    Text(if (isBn) "আনলক করুন" else "Decrypt")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingEncryptedText = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Pre-Restore Safety Dialog
    pendingRestoreJson?.let { jsonPayload ->
        AlertDialog(
            onDismissRequest = { pendingRestoreJson = null },
            title = {
                Text(if (isBn) "Restore করার আগে নিরাপত্তা যাচাই" else "Safety Check Before Restore")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBn) "বর্তমান ডেটা সুরক্ষিত রাখতে Restore করার আগে বর্তমান রেকর্ডের একটি ব্যাকআপ নিয়ে নিতে পারেন।"
                        else "To keep your current records safe, you can back up your existing data before restoring.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            safetyPlainSaveMode = true
                            jsonSaveLauncher.launch("AmarNote_PreRestore_Safety_${currentYear}.json")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isBn) "আগে বর্তমান ডেটার ব্যাকআপ নিন" else "Backup Current Data First")
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = {
                            onRestoreJson(jsonPayload, false)
                            pendingRestoreJson = null
                        }
                    ) {
                        Text(if (isBn) "একসাথে যোগ (Merge)" else "Merge")
                    }
                    Button(
                        onClick = {
                            onRestoreJson(jsonPayload, true)
                            pendingRestoreJson = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
                    ) {
                        Text(if (isBn) "প্রতিস্থাপন (Replace)" else "Replace")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreJson = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
