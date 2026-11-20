package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AmarNoteDatabase
import com.example.data.local.AppLanguage
import com.example.data.local.AppReminder
import com.example.data.local.AppSettings
import com.example.data.local.AsrJuristicMethod
import com.example.data.local.BackupManager
import com.example.data.local.CalculatorHistoryItem
import com.example.data.local.ClockFormat
import com.example.data.local.CurrencyOption
import com.example.data.local.DateFormatStyle
import com.example.data.local.DefaultDashboardOption
import com.example.data.local.NoteCategory
import com.example.data.local.NumeralStyle
import com.example.data.local.PaymentMethod
import com.example.data.local.PersonalNote
import com.example.data.local.PrayerCalcMethod
import com.example.data.local.ProfileMediaManager
import com.example.data.local.ReminderScheduler
import com.example.data.local.SalaryRecord
import com.example.data.local.SecurityManager
import com.example.data.local.ThemeMode
import com.example.data.local.UserPreferences
import com.example.data.local.UserProfile
import com.example.data.repository.AmarNoteRepository
import com.example.domain.DailyPrayerTimes
import com.example.domain.LocalizationHelper
import com.example.domain.NextPrayerInfo
import com.example.domain.PrayerTimesCalculator
import com.example.domain.QiblaData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

data class LiveClockState(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour24: Int,
    val minute: Int,
    val second: Int,
    val dayOfWeek: Int
) {
    companion object {
        fun now(): LiveClockState {
            val cal = Calendar.getInstance()
            return LiveClockState(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH),
                hour24 = cal.get(Calendar.HOUR_OF_DAY),
                minute = cal.get(Calendar.MINUTE),
                second = cal.get(Calendar.SECOND),
                dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            )
        }
    }
}

data class CalculatorUiState(
    val display: String = "0",
    val expression: String = "",
    val pendingOperator: String? = null,
    val firstOperand: Double? = null,
    val shouldResetDisplay: Boolean = false
)

data class MonthlySummary(
    val month: Int,
    val totalAmount: Double,
    val recordCount: Int,
    val records: List<SalaryRecord>,
    val methodTotals: Map<PaymentMethod, Double>
)

data class YearlyReportData(
    val year: Int,
    val totalSalary: Double,
    val totalTransactions: Int,
    val monthlyTotals: List<Double>,
    val highestMonth: Pair<Int, Double>?,
    val lowestMonth: Pair<Int, Double>?,
    val methodTotals: Map<PaymentMethod, Double>
)

class AmarNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val db = AmarNoteDatabase.getInstance(application)
    private val prefs = UserPreferences(application)
    private val repository = AmarNoteRepository(
        salaryDao = db.salaryDao(),
        calculatorDao = db.calculatorDao(),
        profileDao = db.profileDao(),
        personalNoteDao = db.personalNoteDao(),
        reminderDao = db.reminderDao(),
        preferences = prefs
    )

    private val _clockState = MutableStateFlow(LiveClockState.now())
    val clockState: StateFlow<LiveClockState> = _clockState.asStateFlow()

    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppSettings()
    )

    val allSalaries: StateFlow<List<SalaryRecord>> = repository.allSalaries.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val userProfile: StateFlow<UserProfile> = repository.userProfile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UserProfile()
    )

    val allNotes: StateFlow<List<PersonalNote>> = repository.personalNotes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allReminders: StateFlow<List<AppReminder>> = repository.reminders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val calculatorHistory: StateFlow<List<CalculatorHistoryItem>> = repository.calculatorHistory.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // App Lock State
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()
    private var lastBackgroundedAtMillis: Long = 0L

    // Notes Search & Category Filter
    private val _noteSearchQuery = MutableStateFlow("")
    val noteSearchQuery: StateFlow<String> = _noteSearchQuery.asStateFlow()

    private val _noteCategoryFilter = MutableStateFlow<NoteCategory?>(null)
    val noteCategoryFilter: StateFlow<NoteCategory?> = _noteCategoryFilter.asStateFlow()

    val filteredNotes: StateFlow<List<PersonalNote>> = combine(
        allNotes,
        _noteSearchQuery,
        _noteCategoryFilter
    ) { notes, query, catFilter ->
        val q = query.trim().lowercase(Locale.ROOT)
        notes.filter { note ->
            val matchesCat = catFilter == null || note.categoryEnum == catFilter
            val matchesQ = if (q.isEmpty()) true else {
                note.title.lowercase(Locale.ROOT).contains(q) ||
                    note.content.lowercase(Locale.ROOT).contains(q) ||
                    note.extraComment.lowercase(Locale.ROOT).contains(q) ||
                    note.categoryEnum.bnLabel.lowercase(Locale.ROOT).contains(q) ||
                    note.categoryEnum.enLabel.lowercase(Locale.ROOT).contains(q)
            }
            matchesCat && matchesQ
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search & filter state for Salary Ledger
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterYear = MutableStateFlow<Int?>(null)
    val filterYear: StateFlow<Int?> = _filterYear.asStateFlow()

    private val _filterMethod = MutableStateFlow<PaymentMethod?>(null)
    val filterMethod: StateFlow<PaymentMethod?> = _filterMethod.asStateFlow()

    val filteredSalaries: StateFlow<List<SalaryRecord>> = combine(
        allSalaries,
        _searchQuery,
        _filterYear,
        _filterMethod,
        settings
    ) { records, query, yearFilter, methodFilter, _ ->
        val q = query.trim().lowercase(Locale.ROOT)
        records.filter { rec ->
            val matchesYear = yearFilter == null || rec.year == yearFilter
            val matchesMethod = methodFilter == null || rec.methodEnum == methodFilter
            val matchesQuery = if (q.isEmpty()) {
                true
            } else {
                val monthBn = LocalizationHelper.gregorianMonthName(rec.month, true).lowercase(Locale.ROOT)
                val monthEn = LocalizationHelper.gregorianMonthName(rec.month, false).lowercase(Locale.ROOT)
                val dayBn = LocalizationHelper.weekdayName(rec.dayOfWeek, true).lowercase(Locale.ROOT)
                val dayEn = LocalizationHelper.weekdayName(rec.dayOfWeek, false).lowercase(Locale.ROOT)
                val dateFormatted = String.format(Locale.US, "%04d-%02d-%02d", rec.year, rec.month, rec.day)
                val dateBn = LocalizationHelper.formatDate(rec.year, rec.month, rec.day, true, true)
                val dateEn = LocalizationHelper.formatDate(rec.year, rec.month, rec.day, false, false).lowercase(Locale.ROOT)
                val amtStr = rec.amount.toString()
                val amtBn = LocalizationHelper.toLocalizedDigits(amtStr, true)
                val methodBn = rec.methodEnum.bnLabel.lowercase(Locale.ROOT)
                val methodEn = rec.methodEnum.enLabel.lowercase(Locale.ROOT)

                listOf(
                    amtStr, amtBn,
                    rec.year.toString(), LocalizationHelper.toLocalizedDigits(rec.year.toString(), true),
                    monthBn, monthEn,
                    dayBn, dayEn,
                    dateFormatted, dateBn, dateEn,
                    methodBn, methodEn,
                    rec.receiverNumber.lowercase(Locale.ROOT),
                    rec.senderNumber.lowercase(Locale.ROOT),
                    rec.bankName.lowercase(Locale.ROOT),
                    rec.accountLastDigits.lowercase(Locale.ROOT),
                    rec.transactionId.lowercase(Locale.ROOT),
                    rec.comment.lowercase(Locale.ROOT),
                    rec.extraNote.lowercase(Locale.ROOT)
                ).any { it.contains(q) }
            }
            matchesYear && matchesMethod && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableYears: StateFlow<List<Int>> = combine(allSalaries, clockState) { records, clock ->
        val baseYears = (2026..2036).toMutableSet()
        baseYears.add(clock.year)
        records.forEach { baseYears.add(it.year) }
        baseYears.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), (2026..2036).toList())

    private val _selectedYear = MutableStateFlow(LiveClockState.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(LiveClockState.now().month)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    val selectedMonthSummary: StateFlow<MonthlySummary> = combine(
        allSalaries,
        _selectedYear,
        _selectedMonth
    ) { records, year, month ->
        val monthRecords = records.filter { it.year == year && it.month == month }
        val total = monthRecords.sumOf { it.amount }
        val methodTotals = PaymentMethod.entries.associateWith { method ->
            monthRecords.filter { it.methodEnum == method }.sumOf { it.amount }
        }
        MonthlySummary(
            month = month,
            totalAmount = total,
            recordCount = monthRecords.size,
            records = monthRecords,
            methodTotals = methodTotals
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlySummary(LiveClockState.now().month, 0.0, 0, emptyList(), emptyMap())
    )

    val yearlyReport: StateFlow<YearlyReportData> = combine(
        allSalaries,
        _selectedYear
    ) { records, year ->
        val yearRecords = records.filter { it.year == year }
        val monthlyTotals = (1..12).map { m ->
            yearRecords.filter { it.month == m }.sumOf { it.amount }
        }
        val activeMonths = monthlyTotals.mapIndexed { idx, amt -> (idx + 1) to amt }.filter { it.second > 0.0 }
        val highest = activeMonths.maxByOrNull { it.second }
        val lowest = activeMonths.minByOrNull { it.second }
        val methodTotals = PaymentMethod.entries.associateWith { method ->
            yearRecords.filter { it.methodEnum == method }.sumOf { it.amount }
        }
        YearlyReportData(
            year = year,
            totalSalary = yearRecords.sumOf { it.amount },
            totalTransactions = yearRecords.size,
            monthlyTotals = monthlyTotals,
            highestMonth = highest,
            lowestMonth = lowest,
            methodTotals = methodTotals
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        YearlyReportData(LiveClockState.now().year, 0.0, 0, List(12) { 0.0 }, null, null, emptyMap())
    )

    val dailyPrayerTimes: StateFlow<DailyPrayerTimes> = combine(
        clockState,
        settings
    ) { clock, s ->
        PrayerTimesCalculator.calculateDaily(
            year = clock.year,
            month = clock.month,
            day = clock.day,
            latitude = s.latitude,
            longitude = s.longitude,
            method = s.prayerMethod,
            asrMethod = s.asrMethod
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PrayerTimesCalculator.calculateDaily(
            LiveClockState.now().year,
            LiveClockState.now().month,
            LiveClockState.now().day,
            23.8103,
            90.4125,
            PrayerCalcMethod.KARACHI,
            AsrJuristicMethod.HANAFI
        )
    )

    val nextPrayerInfo: StateFlow<NextPrayerInfo> = combine(
        clockState,
        settings,
        dailyPrayerTimes
    ) { clock, s, todayTimes ->
        val cal = GregorianCalendar(clock.year, clock.month - 1, clock.day).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val tomorrowTimes = PrayerTimesCalculator.calculateDaily(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH),
            latitude = s.latitude,
            longitude = s.longitude,
            method = s.prayerMethod,
            asrMethod = s.asrMethod
        )
        PrayerTimesCalculator.findNextPrayer(
            today = todayTimes,
            tomorrow = tomorrowTimes,
            nowHour = clock.hour24,
            nowMinute = clock.minute,
            nowSecond = clock.second
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NextPrayerInfo(com.example.domain.PrayerSlot.FAJR, 300.0, 3600L, false)
    )

    private val _prayerCalendarYear = MutableStateFlow(LiveClockState.now().year)
    val prayerCalendarYear: StateFlow<Int> = _prayerCalendarYear.asStateFlow()

    private val _prayerCalendarMonth = MutableStateFlow(LiveClockState.now().month)
    val prayerCalendarMonth: StateFlow<Int> = _prayerCalendarMonth.asStateFlow()

    val monthlyPrayerCalendar: StateFlow<List<DailyPrayerTimes>> = combine(
        _prayerCalendarYear,
        _prayerCalendarMonth,
        settings
    ) { year, month, s ->
        PrayerTimesCalculator.calculateMonth(
            year = year,
            month = month,
            latitude = s.latitude,
            longitude = s.longitude,
            method = s.prayerMethod,
            asrMethod = s.asrMethod
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val qiblaData: StateFlow<QiblaData> = combine(settings) { (s) ->
        PrayerTimesCalculator.calculateQibla(s.latitude, s.longitude)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PrayerTimesCalculator.calculateQibla(23.8103, 90.4125)
    )

    private val _calculatorState = MutableStateFlow(CalculatorUiState())
    val calculatorState: StateFlow<CalculatorUiState> = _calculatorState.asStateFlow()

    private val _prefillSalaryAmount = MutableStateFlow<Double?>(null)
    val prefillSalaryAmount: StateFlow<Double?> = _prefillSalaryAmount.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            val initialSettings = repository.settings.first()
            if (initialSettings.pinEnabled) {
                _isAppLocked.value = true
            }
        }
        viewModelScope.launch {
            while (true) {
                _clockState.value = LiveClockState.now()
                delay(1000L)
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun showStatusMessage(msg: String) {
        _statusMessage.value = msg
    }

    // --- App Lock & Security ---

    fun onAppBackgrounded() {
        lastBackgroundedAtMillis = System.currentTimeMillis()
    }

    fun onAppResumed() {
        val s = settings.value
        if (!s.pinEnabled || s.autoLockMinutes < 0) return
        if (lastBackgroundedAtMillis == 0L) return
        val elapsedMillis = System.currentTimeMillis() - lastBackgroundedAtMillis
        val thresholdMillis = s.autoLockMinutes * 60_000L
        if (elapsedMillis >= thresholdMillis) {
            _isAppLocked.value = true
        }
    }

    fun lockNow() {
        if (settings.value.pinEnabled) {
            _isAppLocked.value = true
        }
    }

    fun verifyAndUnlockPin(pin: String): Boolean {
        val s = settings.value
        val ok = SecurityManager.verifyPin(pin, s.pinSalt, s.pinHash)
        if (ok) _isAppLocked.value = false
        return ok
    }

    fun unlockWithBiometric() {
        _isAppLocked.value = false
    }

    fun setOrChangePin(newPin: String) {
        viewModelScope.launch {
            val hashed = withContext(Dispatchers.Default) { SecurityManager.hashPin(newPin) }
            repository.setPin(hashed.saltBase64, hashed.hashBase64)
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "নিরাপত্তা PIN সংরক্ষণ করা হয়েছে" else "Security PIN saved"
        }
    }

    fun disableAppPin() {
        viewModelScope.launch {
            repository.disablePin()
            _isAppLocked.value = false
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "অ্যাপ লক বন্ধ করা হয়েছে" else "App lock disabled"
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setBiometricEnabled(enabled) }
    }

    fun setAutoLockMinutes(minutes: Int) {
        viewModelScope.launch { repository.setAutoLockMinutes(minutes) }
    }

    // --- Profile Management ---

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "ব্যক্তিগত প্রোফাইল সংরক্ষণ করা হয়েছে" else "Personal profile saved"
        }
    }

    fun updateProfilePhotoPath(path: String) {
        viewModelScope.launch {
            val current = repository.getProfileOnce()
            repository.saveProfile(current.copy(profilePhotoPath = path))
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "প্রোফাইল ছবি আপডেট হয়েছে" else "Profile photo updated"
        }
    }

    fun updateCoverPhotoPath(path: String) {
        viewModelScope.launch {
            val current = repository.getProfileOnce()
            repository.saveProfile(current.copy(coverPhotoPath = path))
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "কভার ফটো আপডেট হয়েছে" else "Cover photo updated"
        }
    }

    fun deleteProfilePhoto() {
        viewModelScope.launch {
            val current = repository.getProfileOnce()
            ProfileMediaManager.deletePhotoFile(current.profilePhotoPath)
            repository.saveProfile(current.copy(profilePhotoPath = ""))
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "প্রোফাইল ছবি মুছে ফেলা হয়েছে" else "Profile photo deleted"
        }
    }

    fun deleteCoverPhoto() {
        viewModelScope.launch {
            val current = repository.getProfileOnce()
            ProfileMediaManager.deletePhotoFile(current.coverPhotoPath)
            repository.saveProfile(current.copy(coverPhotoPath = ""))
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "কভার ফটো মুছে ফেলা হয়েছে" else "Cover photo deleted"
        }
    }

    fun resetProfileInformation() {
        viewModelScope.launch {
            val current = repository.getProfileOnce()
            ProfileMediaManager.deletePhotoFile(current.profilePhotoPath)
            ProfileMediaManager.deletePhotoFile(current.coverPhotoPath)
            repository.resetProfile()
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "প্রোফাইলের তথ্য রিসেট করা হয়েছে" else "Profile information reset"
        }
    }

    // --- Personal Notes ---

    fun setNoteSearchQuery(query: String) {
        _noteSearchQuery.value = query
    }

    fun setNoteCategoryFilter(category: NoteCategory?) {
        _noteCategoryFilter.value = category
    }

    fun savePersonalNote(note: PersonalNote) {
        viewModelScope.launch {
            repository.savePersonalNote(note)
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "ব্যক্তিগত নোট সংরক্ষিত হয়েছে" else "Personal note saved"
        }
    }

    fun togglePinNote(note: PersonalNote) {
        viewModelScope.launch {
            repository.savePersonalNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun deletePersonalNote(note: PersonalNote) {
        viewModelScope.launch {
            repository.deletePersonalNote(note)
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "নোট মুছে ফেলা হয়েছে" else "Note deleted"
        }
    }

    // --- Reminders ---

    fun saveReminder(reminder: AppReminder) {
        viewModelScope.launch {
            val savedId = repository.saveReminder(reminder)
            val savedReminder = reminder.copy(id = savedId)
            if (savedReminder.notifySystem && !savedReminder.isCompleted) {
                ReminderScheduler.scheduleReminder(appContext, savedReminder)
            } else {
                ReminderScheduler.cancelReminder(appContext, savedId)
            }
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "রিমাইন্ডার সংরক্ষণ করা হয়েছে" else "Reminder saved"
        }
    }

    fun toggleReminderCompleted(reminder: AppReminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isCompleted = !reminder.isCompleted)
            repository.saveReminder(updated)
            if (updated.isCompleted) {
                ReminderScheduler.cancelReminder(appContext, updated.id)
            } else if (updated.notifySystem) {
                ReminderScheduler.scheduleReminder(appContext, updated)
            }
        }
    }

    fun deleteReminder(reminder: AppReminder) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(appContext, reminder.id)
            repository.deleteReminder(reminder)
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "রিমাইন্ডার মুছে ফেলা হয়েছে" else "Reminder deleted"
        }
    }

    // --- Salary CRUD & Filters ---

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterYear(year: Int?) {
        _filterYear.value = year
    }

    fun setFilterMethod(method: PaymentMethod?) {
        _filterMethod.value = method
    }

    fun setSelectedYear(year: Int) {
        _selectedYear.value = year
    }

    fun setSelectedMonth(month: Int) {
        _selectedMonth.value = month.coerceIn(1, 12)
    }

    fun setPrayerCalendarMonthYear(year: Int, month: Int) {
        _prayerCalendarYear.value = year
        _prayerCalendarMonth.value = month.coerceIn(1, 12)
    }

    fun saveSalaryRecord(record: SalaryRecord, isDuplicate: Boolean = false) {
        viewModelScope.launch {
            val computedDow = SalaryRecord.computeDayOfWeek(record.year, record.month, record.day)
            val toSave = if (isDuplicate) {
                record.copy(
                    id = 0L,
                    dayOfWeek = computedDow,
                    createdAtMillis = System.currentTimeMillis(),
                    updatedAtMillis = System.currentTimeMillis()
                )
            } else {
                record.copy(dayOfWeek = computedDow)
            }
            repository.saveSalary(toSave)
            val isBn = settings.value.isBangla
            _statusMessage.value = when {
                isDuplicate -> if (isBn) "রেকর্ড ডুপ্লিকেট করে সংরক্ষণ করা হয়েছে" else "Record duplicated and saved"
                record.id == 0L -> if (isBn) "নতুন বেতন রেকর্ড সংরক্ষণ করা হয়েছে" else "New salary record saved"
                else -> if (isBn) "বেতন রেকর্ড আপডেট করা হয়েছে" else "Salary record updated"
            }
        }
    }

    fun deleteSalaryRecord(record: SalaryRecord) {
        viewModelScope.launch {
            repository.deleteSalary(record)
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "বেতন রেকর্ড মুছে ফেলা হয়েছে" else "Salary record deleted"
        }
    }

    fun setPrefillSalaryAmount(amount: Double?) {
        _prefillSalaryAmount.value = amount
    }

    // --- Backup & Restore (Profile + Photos + Notes + Reminders + Settings + Salaries + Optional Encryption) ---

    suspend fun generateJsonBackupString(passphrase: String? = null): String = withContext(Dispatchers.IO) {
        val records = repository.getAllSalariesOnce()
        val profile = repository.getProfileOnce()
        val notes = repository.getAllNotesOnce()
        val reminders = repository.getAllRemindersOnce()
        val avatarB64 = ProfileMediaManager.encodeFileToBase64(profile.profilePhotoPath)
        val coverB64 = ProfileMediaManager.encodeFileToBase64(profile.coverPhotoPath)

        val plainJson = BackupManager.buildJsonBackup(
            records = records,
            settings = settings.value,
            profile = profile,
            profilePhotoBase64 = avatarB64,
            coverPhotoBase64 = coverB64,
            personalNotes = notes,
            reminders = reminders
        )
        if (!passphrase.isNullOrBlank()) {
            SecurityManager.encryptBackupJson(plainJson, passphrase)
        } else {
            plainJson
        }
    }

    suspend fun generateCsvString(recordsOverride: List<SalaryRecord>? = null): String = withContext(Dispatchers.IO) {
        val records = recordsOverride ?: repository.getAllSalariesOnce()
        BackupManager.buildCsv(records, settings.value.isBangla)
    }

    fun restoreFromJson(rawJsonOrPlain: String, replaceExisting: Boolean) {
        viewModelScope.launch {
            try {
                val parsed = withContext(Dispatchers.IO) { BackupManager.parseJsonBackup(rawJsonOrPlain) }
                if (replaceExisting) {
                    repository.replaceAllSalaries(parsed.records)
                    if (parsed.personalNotes.isNotEmpty()) {
                        repository.replaceAllNotes(parsed.personalNotes)
                    }
                    if (parsed.reminders.isNotEmpty()) {
                        repository.replaceAllReminders(parsed.reminders)
                    }
                } else {
                    repository.insertAllSalaries(parsed.records)
                    if (parsed.personalNotes.isNotEmpty()) {
                        repository.insertAllNotes(parsed.personalNotes)
                    }
                    if (parsed.reminders.isNotEmpty()) {
                        repository.insertAllReminders(parsed.reminders)
                    }
                }

                // Restore Profile & Offline Photos
                if (parsed.profile != null || parsed.profilePhotoBase64.isNotBlank() || parsed.coverPhotoBase64.isNotBlank()) {
                    val baseProfile = parsed.profile ?: repository.getProfileOnce()
                    val avatarPath = if (parsed.profilePhotoBase64.isNotBlank()) {
                        withContext(Dispatchers.IO) {
                            ProfileMediaManager.decodeBase64ToFile(appContext, parsed.profilePhotoBase64, isAvatar = true)
                        }
                    } else baseProfile.profilePhotoPath
                    val coverPath = if (parsed.coverPhotoBase64.isNotBlank()) {
                        withContext(Dispatchers.IO) {
                            ProfileMediaManager.decodeBase64ToFile(appContext, parsed.coverPhotoBase64, isAvatar = false)
                        }
                    } else baseProfile.coverPhotoPath

                    repository.saveProfile(
                        baseProfile.copy(
                            id = 1,
                            profilePhotoPath = avatarPath,
                            coverPhotoPath = coverPath
                        )
                    )
                }

                parsed.settings?.let { repository.restoreSettings(it) }

                val isBn = settings.value.isBangla
                val countStr = LocalizationHelper.formatInt(parsed.records.size, settings.value.useBanglaDigits)
                _statusMessage.value = if (isBn) {
                    "$countStr টি বেতন রেকর্ড, প্রোফাইল, নোট ও রিমাইন্ডার সফলভাবে Restore হয়েছে"
                } else {
                    "Restored $countStr salary records, profile, notes & reminders"
                }
            } catch (e: Exception) {
                val isBn = settings.value.isBangla
                _statusMessage.value = if (isBn) {
                    "ব্যাকআপ ফাইল পড়া যায়নি: ${e.localizedMessage ?: "ত্রুটি"}"
                } else {
                    "Failed to restore backup: ${e.localizedMessage ?: "Invalid file"}"
                }
            }
        }
    }

    // --- Settings & App Customization Actions ---

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(lang) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setNumeralStyle(style: NumeralStyle) {
        viewModelScope.launch { repository.setNumeralStyle(style) }
    }

    fun setClockFormat(format: ClockFormat) {
        viewModelScope.launch { repository.setClockFormat(format) }
    }

    fun setPrayerMethod(method: PrayerCalcMethod) {
        viewModelScope.launch { repository.setPrayerMethod(method) }
    }

    fun setAsrMethod(method: AsrJuristicMethod) {
        viewModelScope.launch { repository.setAsrMethod(method) }
    }

    fun setCurrency(currency: CurrencyOption) {
        viewModelScope.launch { repository.setCurrency(currency) }
    }

    fun setDateFormat(format: DateFormatStyle) {
        viewModelScope.launch { repository.setDateFormat(format) }
    }

    fun setDefaultDashboard(option: DefaultDashboardOption) {
        viewModelScope.launch { repository.setDefaultDashboard(option) }
    }

    fun setProfileDisplayPrefs(
        showOnDashboard: Boolean,
        showPhone: Boolean,
        showEmail: Boolean,
        strictPrivate: Boolean
    ) {
        viewModelScope.launch {
            repository.setProfileDisplayPrefs(showOnDashboard, showPhone, showEmail, strictPrivate)
        }
    }

    fun setLocation(nameBn: String, nameEn: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.setLocation(nameBn, nameEn, lat, lng)
            val isBn = settings.value.isBangla
            _statusMessage.value = if (isBn) "অবস্থান আপডেট হয়েছে: $nameBn" else "Location updated: $nameEn"
        }
    }

    // --- Calculator Actions ---

    fun onCalcDigit(digit: String) {
        _calculatorState.update { state ->
            if (state.shouldResetDisplay) {
                state.copy(
                    display = if (digit == ".") "0." else digit,
                    shouldResetDisplay = false
                )
            } else {
                val current = state.display
                val updated = when {
                    digit == "." && current.contains(".") -> current
                    digit == "." -> "$current."
                    current == "0" || current == "Error" -> digit
                    current.length >= 15 -> current
                    else -> current + digit
                }
                state.copy(display = updated)
            }
        }
    }

    fun onCalcBackspace() {
        _calculatorState.update { state ->
            if (state.shouldResetDisplay || state.display == "Error") {
                state.copy(display = "0", shouldResetDisplay = false)
            } else if (state.display.length <= 1 || (state.display.length == 2 && state.display.startsWith("-"))) {
                state.copy(display = "0")
            } else {
                state.copy(display = state.display.dropLast(1))
            }
        }
    }

    fun onCalcClear() {
        _calculatorState.value = CalculatorUiState()
    }

    fun onCalcToggleSign() {
        _calculatorState.update { state ->
            val value = state.display.toDoubleOrNull() ?: return@update state
            state.copy(display = formatCalcNumber(-value))
        }
    }

    fun onCalcPercent() {
        _calculatorState.update { state ->
            val current = state.display.toDoubleOrNull() ?: return@update state
            val result = if (state.firstOperand != null && (state.pendingOperator == "+" || state.pendingOperator == "-")) {
                state.firstOperand * current / 100.0
            } else {
                current / 100.0
            }
            state.copy(display = formatCalcNumber(result), shouldResetDisplay = true)
        }
    }

    fun onCalcOperator(op: String) {
        _calculatorState.update { state ->
            val currentVal = state.display.toDoubleOrNull() ?: 0.0
            if (state.firstOperand != null && state.pendingOperator != null && !state.shouldResetDisplay) {
                val intermediate = compute(state.firstOperand, currentVal, state.pendingOperator)
                if (intermediate == null) {
                    return@update CalculatorUiState(display = "Error", shouldResetDisplay = true)
                }
                val formatted = formatCalcNumber(intermediate)
                state.copy(
                    display = formatted,
                    firstOperand = intermediate,
                    pendingOperator = op,
                    expression = "$formatted $op",
                    shouldResetDisplay = true
                )
            } else {
                val formatted = formatCalcNumber(currentVal)
                state.copy(
                    firstOperand = currentVal,
                    pendingOperator = op,
                    expression = "$formatted $op",
                    shouldResetDisplay = true
                )
            }
        }
    }

    fun onCalcEquals() {
        val state = _calculatorState.value
        val first = state.firstOperand ?: return
        val op = state.pendingOperator ?: return
        val second = state.display.toDoubleOrNull() ?: return
        val result = compute(first, second, op)
        if (result == null) {
            _calculatorState.value = CalculatorUiState(display = "Error", shouldResetDisplay = true)
            return
        }
        val exprText = "${formatCalcNumber(first)} $op ${formatCalcNumber(second)}"
        val resultText = formatCalcNumber(result)
        _calculatorState.value = CalculatorUiState(
            display = resultText,
            expression = "$exprText =",
            pendingOperator = null,
            firstOperand = null,
            shouldResetDisplay = true
        )
        viewModelScope.launch {
            repository.addCalculatorHistory(exprText, resultText)
        }
    }

    fun onCalcMemoryAction(action: String) {
        val currentVal = _calculatorState.value.display.toDoubleOrNull() ?: 0.0
        val currentMem = settings.value.calculatorMemory
        viewModelScope.launch {
            when (action) {
                "MC" -> repository.setCalculatorMemory(0.0)
                "MR" -> _calculatorState.update {
                    it.copy(display = formatCalcNumber(currentMem), shouldResetDisplay = true)
                }
                "M+" -> repository.setCalculatorMemory(currentMem + currentVal)
                "M-" -> repository.setCalculatorMemory(currentMem - currentVal)
                "MS" -> repository.setCalculatorMemory(currentVal)
            }
        }
    }

    fun applyHistoryResult(result: String) {
        _calculatorState.update {
            it.copy(display = result, shouldResetDisplay = false)
        }
    }

    fun clearCalculatorHistory() {
        viewModelScope.launch { repository.clearCalculatorHistory() }
    }

    private fun compute(a: Double, b: Double, op: String): Double? {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> if (abs(b) < 1e-12) null else a / b
            else -> b
        }
    }

    private fun formatCalcNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val rounded = value.roundToLong()
        return if (abs(value - rounded) < 1e-9) {
            rounded.toString()
        } else {
            String.format(Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')
        }
    }
}
