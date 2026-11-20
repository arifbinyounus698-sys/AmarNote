package com.example.data.repository

import com.example.data.local.AppLanguage
import com.example.data.local.AppReminder
import com.example.data.local.AppSettings
import com.example.data.local.AsrJuristicMethod
import com.example.data.local.CalculatorDao
import com.example.data.local.CalculatorHistoryItem
import com.example.data.local.ClockFormat
import com.example.data.local.CurrencyOption
import com.example.data.local.DateFormatStyle
import com.example.data.local.DefaultDashboardOption
import com.example.data.local.NumeralStyle
import com.example.data.local.PersonalNote
import com.example.data.local.PersonalNoteDao
import com.example.data.local.PrayerCalcMethod
import com.example.data.local.ProfileDao
import com.example.data.local.ReminderDao
import com.example.data.local.SalaryDao
import com.example.data.local.SalaryRecord
import com.example.data.local.ThemeMode
import com.example.data.local.UserPreferences
import com.example.data.local.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AmarNoteRepository(
    private val salaryDao: SalaryDao,
    private val calculatorDao: CalculatorDao,
    private val profileDao: ProfileDao,
    private val personalNoteDao: PersonalNoteDao,
    private val reminderDao: ReminderDao,
    private val preferences: UserPreferences
) {
    val allSalaries: Flow<List<SalaryRecord>> = salaryDao.getAllRecords()
    val recordedYears: Flow<List<Int>> = salaryDao.getRecordedYears()
    val calculatorHistory: Flow<List<CalculatorHistoryItem>> = calculatorDao.getHistory()
    val settings: Flow<AppSettings> = preferences.settingsFlow

    val userProfile: Flow<UserProfile> = profileDao.observeProfile().map { it ?: UserProfile() }
    val personalNotes: Flow<List<PersonalNote>> = personalNoteDao.getAllNotes()
    val reminders: Flow<List<AppReminder>> = reminderDao.getAllReminders()

    suspend fun getAllSalariesOnce(): List<SalaryRecord> = salaryDao.getAllRecordsOnce()
    suspend fun getProfileOnce(): UserProfile = profileDao.getProfileOnce() ?: UserProfile()
    suspend fun getAllNotesOnce(): List<PersonalNote> = personalNoteDao.getAllNotesOnce()
    suspend fun getAllRemindersOnce(): List<AppReminder> = reminderDao.getAllRemindersOnce()

    suspend fun saveSalary(record: SalaryRecord): Long {
        return if (record.id == 0L) {
            salaryDao.insert(record)
        } else {
            salaryDao.update(record.copy(updatedAtMillis = System.currentTimeMillis()))
            record.id
        }
    }

    suspend fun insertAllSalaries(records: List<SalaryRecord>) = salaryDao.insertAll(records)

    suspend fun replaceAllSalaries(records: List<SalaryRecord>) {
        salaryDao.deleteAll()
        if (records.isNotEmpty()) {
            salaryDao.insertAll(records)
        }
    }

    suspend fun deleteSalary(record: SalaryRecord) = salaryDao.delete(record)

    // --- Profile ---
    suspend fun saveProfile(profile: UserProfile) {
        profileDao.upsertProfile(profile.copy(id = 1, updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun resetProfile() {
        profileDao.clearProfile()
    }

    // --- Personal Notes ---
    suspend fun savePersonalNote(note: PersonalNote): Long {
        return if (note.id == 0L) {
            personalNoteDao.insert(note)
        } else {
            personalNoteDao.update(note.copy(updatedAtMillis = System.currentTimeMillis()))
            note.id
        }
    }

    suspend fun deletePersonalNote(note: PersonalNote) = personalNoteDao.delete(note)

    suspend fun insertAllNotes(notes: List<PersonalNote>) = personalNoteDao.insertAll(notes)

    suspend fun replaceAllNotes(notes: List<PersonalNote>) {
        personalNoteDao.deleteAll()
        if (notes.isNotEmpty()) personalNoteDao.insertAll(notes)
    }

    // --- Reminders ---
    suspend fun saveReminder(reminder: AppReminder): Long {
        return if (reminder.id == 0L) {
            reminderDao.insert(reminder)
        } else {
            reminderDao.update(reminder)
            reminder.id
        }
    }

    suspend fun deleteReminder(reminder: AppReminder) = reminderDao.delete(reminder)

    suspend fun insertAllReminders(reminders: List<AppReminder>) = reminderDao.insertAll(reminders)

    suspend fun replaceAllReminders(reminders: List<AppReminder>) {
        reminderDao.deleteAll()
        if (reminders.isNotEmpty()) reminderDao.insertAll(reminders)
    }

    // --- Calculator ---
    suspend fun addCalculatorHistory(expression: String, result: String) {
        calculatorDao.insert(CalculatorHistoryItem(expression = expression, result = result))
    }

    suspend fun clearCalculatorHistory() = calculatorDao.clearAll()

    // --- Settings & Customization ---
    suspend fun setLanguage(language: AppLanguage) = preferences.setLanguage(language)
    suspend fun setThemeMode(mode: ThemeMode) = preferences.setThemeMode(mode)
    suspend fun setNumeralStyle(style: NumeralStyle) = preferences.setNumeralStyle(style)
    suspend fun setClockFormat(format: ClockFormat) = preferences.setClockFormat(format)
    suspend fun setPrayerMethod(method: PrayerCalcMethod) = preferences.setPrayerMethod(method)
    suspend fun setAsrMethod(method: AsrJuristicMethod) = preferences.setAsrMethod(method)
    suspend fun setLocation(nameBn: String, nameEn: String, lat: Double, lng: Double) =
        preferences.setLocation(nameBn, nameEn, lat, lng)
    suspend fun setCalculatorMemory(value: Double) = preferences.setCalculatorMemory(value)
    suspend fun setCurrency(currency: CurrencyOption) = preferences.setCurrency(currency)
    suspend fun setDateFormat(format: DateFormatStyle) = preferences.setDateFormat(format)
    suspend fun setDefaultDashboard(option: DefaultDashboardOption) = preferences.setDefaultDashboard(option)
    suspend fun setProfileDisplayPrefs(
        showOnDashboard: Boolean,
        showPhone: Boolean,
        showEmail: Boolean,
        strictPrivate: Boolean
    ) = preferences.setProfileDisplayPrefs(showOnDashboard, showPhone, showEmail, strictPrivate)

    // --- Security ---
    suspend fun setPin(salt: String, hash: String) = preferences.setPin(salt, hash)
    suspend fun disablePin() = preferences.disablePin()
    suspend fun setBiometricEnabled(enabled: Boolean) = preferences.setBiometricEnabled(enabled)
    suspend fun setAutoLockMinutes(minutes: Int) = preferences.setAutoLockMinutes(minutes)

    suspend fun restoreSettings(settings: AppSettings) = preferences.restoreSettings(settings)
}
