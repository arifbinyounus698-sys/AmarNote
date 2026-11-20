package com.example.data.local

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.example.domain.LocalizationHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.OutputStream

data class ParsedBackupData(
    val records: List<SalaryRecord>,
    val settings: AppSettings?,
    val profile: UserProfile? = null,
    val profilePhotoBase64: String = "",
    val coverPhotoBase64: String = "",
    val personalNotes: List<PersonalNote> = emptyList(),
    val reminders: List<AppReminder> = emptyList()
)

object BackupManager {
    private const val BACKUP_FORMAT_VERSION = 2

    fun buildJsonBackup(
        records: List<SalaryRecord>,
        settings: AppSettings,
        profile: UserProfile? = null,
        profilePhotoBase64: String = "",
        coverPhotoBase64: String = "",
        personalNotes: List<PersonalNote> = emptyList(),
        reminders: List<AppReminder> = emptyList()
    ): String {
        val root = JSONObject()
        root.put("app", "AmarNote")
        root.put("version", BACKUP_FORMAT_VERSION)
        root.put("exportedAtMillis", System.currentTimeMillis())

        val settingsObj = JSONObject().apply {
            put("language", settings.language.name)
            put("themeMode", settings.themeMode.name)
            put("numeralStyle", settings.numeralStyle.name)
            put("clockFormat", settings.clockFormat.name)
            put("prayerMethod", settings.prayerMethod.name)
            put("asrMethod", settings.asrMethod.name)
            put("locationNameBn", settings.locationNameBn)
            put("locationNameEn", settings.locationNameEn)
            put("latitude", settings.latitude)
            put("longitude", settings.longitude)
            put("currency", settings.currency.name)
            put("dateFormat", settings.dateFormat.name)
            put("defaultDashboard", settings.defaultDashboard.name)
            put("showProfileOnDashboard", settings.showProfileOnDashboard)
            put("showPhoneInProfileCard", settings.showPhoneInProfileCard)
            put("showEmailInProfileCard", settings.showEmailInProfileCard)
            put("strictPrivateMode", settings.strictPrivateMode)
        }
        root.put("settings", settingsObj)

        profile?.let { p ->
            root.put("profile", profileToJsonObject(p))
        }
        if (profilePhotoBase64.isNotBlank()) {
            root.put("profilePhotoBase64", profilePhotoBase64)
        }
        if (coverPhotoBase64.isNotBlank()) {
            root.put("coverPhotoBase64", coverPhotoBase64)
        }

        val salaryArr = JSONArray()
        records.forEach { rec ->
            salaryArr.put(
                JSONObject().apply {
                    put("amount", rec.amount)
                    put("year", rec.year)
                    put("month", rec.month)
                    put("day", rec.day)
                    put("hour", rec.hour)
                    put("minute", rec.minute)
                    put("second", rec.second)
                    put("dayOfWeek", rec.dayOfWeek)
                    put("paymentMethod", rec.paymentMethod)
                    put("receiverNumber", rec.receiverNumber)
                    put("senderNumber", rec.senderNumber)
                    put("bankName", rec.bankName)
                    put("accountLastDigits", rec.accountLastDigits)
                    put("transactionId", rec.transactionId)
                    put("comment", rec.comment)
                    put("extraNote", rec.extraNote)
                    put("createdAtMillis", rec.createdAtMillis)
                    put("updatedAtMillis", rec.updatedAtMillis)
                }
            )
        }
        root.put("salaryRecords", salaryArr)

        val notesArr = JSONArray()
        personalNotes.forEach { n ->
            notesArr.put(
                JSONObject().apply {
                    put("title", n.title)
                    put("content", n.content)
                    put("category", n.category)
                    put("year", n.year)
                    put("month", n.month)
                    put("day", n.day)
                    put("isPinned", n.isPinned)
                    put("extraComment", n.extraComment)
                    put("createdAtMillis", n.createdAtMillis)
                    put("updatedAtMillis", n.updatedAtMillis)
                }
            )
        }
        root.put("personalNotes", notesArr)

        val remindersArr = JSONArray()
        reminders.forEach { r ->
            remindersArr.put(
                JSONObject().apply {
                    put("title", r.title)
                    put("description", r.description)
                    put("category", r.category)
                    put("year", r.year)
                    put("month", r.month)
                    put("day", r.day)
                    put("hour", r.hour)
                    put("minute", r.minute)
                    put("triggerAtMillis", r.triggerAtMillis)
                    put("isCompleted", r.isCompleted)
                    put("notifySystem", r.notifySystem)
                    put("createdAtMillis", r.createdAtMillis)
                }
            )
        }
        root.put("reminders", remindersArr)

        return root.toString(2)
    }

    fun buildPersonalProfileExportJson(profile: UserProfile): String {
        return JSONObject().apply {
            put("app", "AmarNote")
            put("type", "PersonalProfileExport")
            put("exportedAtMillis", System.currentTimeMillis())
            put("profile", profileToJsonObject(profile))
        }.toString(2)
    }

    private fun profileToJsonObject(p: UserProfile): JSONObject {
        return JSONObject().apply {
            put("fullName", p.fullName)
            put("mobileNumber", p.mobileNumber)
            put("altMobileNumber", p.altMobileNumber)
            put("email", p.email)
            put("aboutMe", p.aboutMe)
            put("profession", p.profession)
            put("organizationName", p.organizationName)
            put("designation", p.designation)
            put("workplaceAddress", p.workplaceAddress)
            put("cityAndCountry", p.cityAndCountry)
            put("dateOfBirth", p.dateOfBirth)
            put("extraComment", p.extraComment)
            put("updatedAtMillis", p.updatedAtMillis)
        }
    }

    fun parseJsonBackup(jsonText: String): ParsedBackupData {
        val root = JSONObject(jsonText)
        val arr = root.optJSONArray("salaryRecords")
            ?: throw IllegalArgumentException("Invalid AmarNote backup file: missing salaryRecords")

        val records = mutableListOf<SalaryRecord>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val year = obj.getInt("year")
            val month = obj.getInt("month").coerceIn(1, 12)
            val day = obj.getInt("day").coerceIn(1, 31)
            val dayOfWeek = obj.optInt("dayOfWeek", SalaryRecord.computeDayOfWeek(year, month, day))
            records.add(
                SalaryRecord(
                    id = 0L,
                    amount = obj.getDouble("amount"),
                    year = year,
                    month = month,
                    day = day,
                    hour = obj.optInt("hour", 0).coerceIn(0, 23),
                    minute = obj.optInt("minute", 0).coerceIn(0, 59),
                    second = obj.optInt("second", 0).coerceIn(0, 59),
                    dayOfWeek = dayOfWeek,
                    paymentMethod = obj.optString("paymentMethod", PaymentMethod.CASH.name),
                    receiverNumber = obj.optString("receiverNumber", ""),
                    senderNumber = obj.optString("senderNumber", ""),
                    bankName = obj.optString("bankName", ""),
                    accountLastDigits = obj.optString("accountLastDigits", ""),
                    transactionId = obj.optString("transactionId", ""),
                    comment = obj.optString("comment", ""),
                    extraNote = obj.optString("extraNote", ""),
                    createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                    updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())
                )
            )
        }

        val settingsObj = root.optJSONObject("settings")
        val parsedSettings = settingsObj?.let { s ->
            AppSettings(
                language = runCatching { AppLanguage.valueOf(s.optString("language")) }.getOrDefault(AppLanguage.BANGLA),
                themeMode = runCatching { ThemeMode.valueOf(s.optString("themeMode")) }.getOrDefault(ThemeMode.LIGHT),
                numeralStyle = runCatching { NumeralStyle.valueOf(s.optString("numeralStyle")) }.getOrDefault(NumeralStyle.BANGLA),
                clockFormat = runCatching { ClockFormat.valueOf(s.optString("clockFormat")) }.getOrDefault(ClockFormat.HOUR_12),
                prayerMethod = runCatching { PrayerCalcMethod.valueOf(s.optString("prayerMethod")) }.getOrDefault(PrayerCalcMethod.KARACHI),
                asrMethod = runCatching { AsrJuristicMethod.valueOf(s.optString("asrMethod")) }.getOrDefault(AsrJuristicMethod.HANAFI),
                locationNameBn = s.optString("locationNameBn", "ঢাকা, বাংলাদেশ"),
                locationNameEn = s.optString("locationNameEn", "Dhaka, Bangladesh"),
                latitude = s.optDouble("latitude", 23.8103),
                longitude = s.optDouble("longitude", 90.4125),
                currency = runCatching { CurrencyOption.valueOf(s.optString("currency")) }.getOrDefault(CurrencyOption.BDT),
                dateFormat = runCatching { DateFormatStyle.valueOf(s.optString("dateFormat")) }.getOrDefault(DateFormatStyle.DD_MMM_YYYY),
                defaultDashboard = runCatching { DefaultDashboardOption.valueOf(s.optString("defaultDashboard")) }.getOrDefault(DefaultDashboardOption.DASHBOARD),
                showProfileOnDashboard = s.optBoolean("showProfileOnDashboard", true),
                showPhoneInProfileCard = s.optBoolean("showPhoneInProfileCard", true),
                showEmailInProfileCard = s.optBoolean("showEmailInProfileCard", false),
                strictPrivateMode = s.optBoolean("strictPrivateMode", true)
            )
        }

        val profileObj = root.optJSONObject("profile")
        val parsedProfile = profileObj?.let { p ->
            UserProfile(
                id = 1,
                fullName = p.optString("fullName", ""),
                mobileNumber = p.optString("mobileNumber", ""),
                altMobileNumber = p.optString("altMobileNumber", ""),
                email = p.optString("email", ""),
                aboutMe = p.optString("aboutMe", ""),
                profession = p.optString("profession", ""),
                organizationName = p.optString("organizationName", ""),
                designation = p.optString("designation", ""),
                workplaceAddress = p.optString("workplaceAddress", ""),
                cityAndCountry = p.optString("cityAndCountry", ""),
                dateOfBirth = p.optString("dateOfBirth", ""),
                extraComment = p.optString("extraComment", ""),
                updatedAtMillis = p.optLong("updatedAtMillis", System.currentTimeMillis())
            )
        }

        val notesArr = root.optJSONArray("personalNotes")
        val parsedNotes = mutableListOf<PersonalNote>()
        if (notesArr != null) {
            for (i in 0 until notesArr.length()) {
                val n = notesArr.getJSONObject(i)
                parsedNotes.add(
                    PersonalNote(
                        id = 0L,
                        title = n.optString("title", ""),
                        content = n.optString("content", ""),
                        category = n.optString("category", NoteCategory.IMPORTANT_NOTE.name),
                        year = n.optInt("year", 2026),
                        month = n.optInt("month", 1),
                        day = n.optInt("day", 1),
                        isPinned = n.optBoolean("isPinned", false),
                        extraComment = n.optString("extraComment", ""),
                        createdAtMillis = n.optLong("createdAtMillis", System.currentTimeMillis()),
                        updatedAtMillis = n.optLong("updatedAtMillis", System.currentTimeMillis())
                    )
                )
            }
        }

        val remindersArr = root.optJSONArray("reminders")
        val parsedReminders = mutableListOf<AppReminder>()
        if (remindersArr != null) {
            for (i in 0 until remindersArr.length()) {
                val r = remindersArr.getJSONObject(i)
                parsedReminders.add(
                    AppReminder(
                        id = 0L,
                        title = r.optString("title", ""),
                        description = r.optString("description", ""),
                        category = r.optString("category", ReminderCategory.PERSONAL_TASK.name),
                        year = r.optInt("year", 2026),
                        month = r.optInt("month", 1),
                        day = r.optInt("day", 1),
                        hour = r.optInt("hour", 9),
                        minute = r.optInt("minute", 0),
                        triggerAtMillis = r.optLong("triggerAtMillis", System.currentTimeMillis()),
                        isCompleted = r.optBoolean("isCompleted", false),
                        notifySystem = r.optBoolean("notifySystem", true),
                        createdAtMillis = r.optLong("createdAtMillis", System.currentTimeMillis())
                    )
                )
            }
        }

        return ParsedBackupData(
            records = records,
            settings = parsedSettings,
            profile = parsedProfile,
            profilePhotoBase64 = root.optString("profilePhotoBase64", ""),
            coverPhotoBase64 = root.optString("coverPhotoBase64", ""),
            personalNotes = parsedNotes,
            reminders = parsedReminders
        )
    }

    fun buildCsv(records: List<SalaryRecord>, isBangla: Boolean): String {
        val sb = StringBuilder()
        sb.append('\uFEFF')
        sb.appendLine(
            "Date,Day,Time,Month,Year,Amount,PaymentMethod,ReceiverNumber,SenderNumber,BankName,AccountLastDigits,TransactionID,Comment,ExtraNote"
        )
        records.forEach { rec ->
            val dateStr = String.format("%04d-%02d-%02d", rec.year, rec.month, rec.day)
            val timeStr = String.format("%02d:%02d:%02d", rec.hour, rec.minute, rec.second)
            val dayName = LocalizationHelper.weekdayName(rec.dayOfWeek, isBangla)
            val monthName = LocalizationHelper.gregorianMonthName(rec.month, isBangla)
            val cols = listOf(
                dateStr,
                dayName,
                timeStr,
                monthName,
                rec.year.toString(),
                rec.amount.toString(),
                rec.methodEnum.label(isBangla),
                rec.receiverNumber,
                rec.senderNumber,
                rec.bankName,
                rec.accountLastDigits,
                rec.transactionId,
                rec.comment,
                rec.extraNote
            )
            sb.appendLine(cols.joinToString(",") { escapeCsv(it) })
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\r", " ").replace("\n", " ")
        return if (clean.contains(",") || clean.contains("\"")) {
            "\"${clean.replace("\"", "\"\"")}\""
        } else {
            clean
        }
    }

    fun writePdfReport(
        outputStream: OutputStream,
        title: String,
        subtitle: String,
        records: List<SalaryRecord>,
        settings: AppSettings
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val lineHeight = 20f

        val headerPaint = Paint().apply {
            color = Color.parseColor("#0F766E")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 11f
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 10.5f
            isAntiAlias = true
        }
        val boldBodyPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        val isBn = settings.isBangla
        val useBnDigits = settings.useBanglaDigits
        val total = records.sumOf { it.amount }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas: Canvas = page.canvas
        var y = 48f

        fun drawHeader(c: Canvas) {
            c.drawText("AmarNote — $title", margin, y, headerPaint)
            y += 18f
            c.drawText(subtitle, margin, y, subPaint)
            y += 14f
            val totalLabel = if (isBn) "মোট বেতন:" else "Total Salary:"
            val countLabel = if (isBn) "মোট রেকর্ড:" else "Total Records:"
            val summaryText = "$totalLabel ${LocalizationHelper.formatCurrency(total, useBnDigits, currency = settings.currency)}   |   $countLabel ${LocalizationHelper.formatInt(records.size, useBnDigits)}"
            c.drawText(summaryText, margin, y, boldBodyPaint)
            y += 10f
            c.drawLine(margin, y, pageWidth - margin, y, linePaint)
            y += 18f
        }

        drawHeader(canvas)

        if (records.isEmpty()) {
            val emptyMsg = if (isBn) "এই সময়কালে কোনো বেতন রেকর্ড নেই।" else "No salary records in this period."
            canvas.drawText(emptyMsg, margin, y, bodyPaint)
        } else {
            records.forEachIndexed { idx, rec ->
                if (y > pageHeight - 64f) {
                    document.finishPage(page)
                    pageNumber++
                    page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = page.canvas
                    y = 48f
                    drawHeader(canvas)
                }
                val dateStr = LocalizationHelper.formatDate(rec.year, rec.month, rec.day, isBn, useBnDigits, settings.dateFormat)
                val dayStr = LocalizationHelper.weekdayName(rec.dayOfWeek, isBn)
                val timeStr = LocalizationHelper.formatTime(
                    rec.hour,
                    rec.minute,
                    rec.second,
                    settings.clockFormat == ClockFormat.HOUR_12,
                    isBn,
                    useBnDigits
                )
                val amtStr = LocalizationHelper.formatCurrency(rec.amount, useBnDigits, currency = settings.currency)
                val methodStr = rec.methodEnum.label(isBn)

                canvas.drawText(
                    "${LocalizationHelper.formatInt(idx + 1, useBnDigits)}. $dateStr ($dayStr)  |  $amtStr  |  $methodStr",
                    margin,
                    y,
                    boldBodyPaint
                )
                y += 14f
                val details = buildList {
                    add(timeStr)
                    if (rec.bankName.isNotBlank()) add(rec.bankName)
                    if (rec.receiverNumber.isNotBlank()) add("To: ${rec.receiverNumber}")
                    if (rec.transactionId.isNotBlank()) add("Trx: ${rec.transactionId}")
                    if (rec.comment.isNotBlank()) add(rec.comment)
                }.joinToString(" • ")
                canvas.drawText(details.take(92), margin + 12f, y, subPaint)
                y += lineHeight - 2f
                canvas.drawLine(margin, y - 6f, pageWidth - margin, y - 6f, linePaint)
                y += 6f
            }
        }

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    fun formatShareText(record: SalaryRecord, settings: AppSettings): String {
        val isBn = settings.isBangla
        val useBnDigits = settings.useBanglaDigits
        val dateStr = LocalizationHelper.formatDate(record.year, record.month, record.day, isBn, useBnDigits, settings.dateFormat)
        val banglaCal = LocalizationHelper.formatBanglaCalendarDate(record.year, record.month, record.day, isBn, useBnDigits)
        val dayStr = LocalizationHelper.weekdayName(record.dayOfWeek, isBn)
        val timeStr = LocalizationHelper.formatTime(
            record.hour,
            record.minute,
            record.second,
            settings.clockFormat == ClockFormat.HOUR_12,
            isBn,
            useBnDigits
        )
        val amtStr = LocalizationHelper.formatCurrency(record.amount, useBnDigits, currency = settings.currency)
        val methodStr = record.methodEnum.label(isBn)

        return buildString {
            appendLine(if (isBn) "📓 AmarNote বেতন রসিদ" else "📓 AmarNote Salary Receipt")
            appendLine("────────────────────────")
            appendLine((if (isBn) "পরিমাণ: " else "Amount: ") + amtStr)
            appendLine((if (isBn) "তারিখ: " else "Date: ") + "$dateStr ($dayStr)")
            appendLine((if (isBn) "বাংলা তারিখ: " else "Bangla Date: ") + banglaCal)
            appendLine((if (isBn) "সময়: " else "Time: ") + timeStr)
            appendLine((if (isBn) "মাধ্যম: " else "Method: ") + methodStr)
            if (record.receiverNumber.isNotBlank()) {
                appendLine((if (isBn) "প্রাপক নম্বর: " else "Receiver No: ") + record.receiverNumber)
            }
            if (record.senderNumber.isNotBlank()) {
                appendLine((if (isBn) "প্রেরক নম্বর: " else "Sender No: ") + record.senderNumber)
            }
            if (record.bankName.isNotBlank()) {
                appendLine((if (isBn) "ব্যাংক: " else "Bank: ") + record.bankName)
            }
            if (record.accountLastDigits.isNotBlank()) {
                appendLine((if (isBn) "অ্যাকাউন্ট (শেষ সংখ্যা): " else "Account Last Digits: ") + record.accountLastDigits)
            }
            if (record.transactionId.isNotBlank()) {
                appendLine((if (isBn) "ট্রানজেকশন আইডি: " else "Transaction ID: ") + record.transactionId)
            }
            if (record.comment.isNotBlank()) {
                appendLine((if (isBn) "মন্তব্য: " else "Comment: ") + record.comment)
            }
            if (record.extraNote.isNotBlank()) {
                appendLine((if (isBn) "অতিরিক্ত নোট: " else "Extra Note: ") + record.extraNote)
            }
        }
    }

    fun shareText(context: Context, text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareTempFile(context: Context, fileName: String, mimeType: String, writeAction: (OutputStream) -> Unit) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.outputStream().use { writeAction(it) }
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, fileName))
    }

    fun printRecord(context: Context, record: SalaryRecord, settings: AppSettings) {
        val isBn = settings.isBangla
        val useBnDigits = settings.useBanglaDigits
        val dateStr = LocalizationHelper.formatDate(record.year, record.month, record.day, isBn, useBnDigits, settings.dateFormat)
        val dayStr = LocalizationHelper.weekdayName(record.dayOfWeek, isBn)
        val timeStr = LocalizationHelper.formatTime(
            record.hour,
            record.minute,
            record.second,
            settings.clockFormat == ClockFormat.HOUR_12,
            isBn,
            useBnDigits
        )
        val amtStr = LocalizationHelper.formatCurrency(record.amount, useBnDigits, currency = settings.currency)
        val html = """
            <html>
            <head>
            <meta charset="utf-8"/>
            <style>
              body { font-family: sans-serif; padding: 28px; color: #1a1f24; }
              .card { border: 2px solid #0f766e; border-radius: 14px; padding: 24px; }
              h1 { color: #0f766e; margin: 0 0 6px 0; font-size: 22px; }
              .amount { font-size: 28px; font-weight: bold; color: #064e3b; margin: 14px 0; }
              table { width: 100%; border-collapse: collapse; margin-top: 12px; }
              td { padding: 8px 4px; border-bottom: 1px solid #e2ddd5; font-size: 14px; }
              .label { font-weight: bold; color: #475569; width: 42%; }
            </style>
            </head>
            <body>
              <div class="card">
                <h1>AmarNote — ${if (isBn) "বেতন হিসাবের পৃষ্ঠা" else "Salary Ledger Record"}</h1>
                <div class="amount">$amtStr</div>
                <table>
                  <tr><td class="label">${if (isBn) "তারিখ ও বার" else "Date & Day"}</td><td>$dateStr ($dayStr)</td></tr>
                  <tr><td class="label">${if (isBn) "সময়" else "Time"}</td><td>$timeStr</td></tr>
                  <tr><td class="label">${if (isBn) "মাধ্যম" else "Payment Method"}</td><td>${escapeHtml(record.methodEnum.label(isBn))}</td></tr>
                  ${rowIf(if (isBn) "প্রাপক নম্বর" else "Receiver No.", record.receiverNumber)}
                  ${rowIf(if (isBn) "প্রেরক নম্বর" else "Sender No.", record.senderNumber)}
                  ${rowIf(if (isBn) "ব্যাংক" else "Bank Name", record.bankName)}
                  ${rowIf(if (isBn) "অ্যাকাউন্ট (শেষ)" else "Account Last Digits", record.accountLastDigits)}
                  ${rowIf(if (isBn) "ট্রানজেকশন আইডি" else "Transaction ID", record.transactionId)}
                  ${rowIf(if (isBn) "মন্তব্য" else "Comment", record.comment)}
                  ${rowIf(if (isBn) "অতিরিক্ত নোট" else "Extra Note", record.extraNote)}
                </table>
              </div>
            </body>
            </html>
        """.trimIndent()

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
                val adapter = webView.createPrintDocumentAdapter("AmarNote_${record.year}_${record.month}_${record.day}")
                printManager.print("AmarNote Salary Record", adapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
    }

    private fun rowIf(label: String, value: String): String {
        if (value.isBlank()) return ""
        return "<tr><td class=\"label\">${escapeHtml(label)}</td><td>${escapeHtml(value)}</td></tr>"
    }

    private fun escapeHtml(s: String): String {
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
}
