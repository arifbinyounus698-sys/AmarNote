package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "amarnote_prefs")

enum class AppLanguage { BANGLA, ENGLISH }
enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class NumeralStyle { BANGLA, ENGLISH }
enum class ClockFormat { HOUR_12, HOUR_24 }

enum class CurrencyOption(val symbol: String, val code: String, val bnName: String, val enName: String) {
    BDT("৳", "BDT", "টাকা (৳ BDT)", "BDT (৳ Taka)"),
    USD("$", "USD", "ডলার ($ USD)", "USD ($ Dollar)"),
    SAR("﷼", "SAR", "রিয়াল (﷼ SAR)", "SAR (﷼ Riyal)"),
    EUR("€", "EUR", "ইউরো (€ EUR)", "EUR (€ Euro)"),
    GBP("£", "GBP", "পাউন্ড (£ GBP)", "GBP (£ Pound)"),
    INR("₹", "INR", "রুপি (₹ INR)", "INR (₹ Rupee)");

    fun label(isBangla: Boolean): String = if (isBangla) bnName else enName
}

enum class DateFormatStyle(val bnLabel: String, val enLabel: String) {
    DD_MMM_YYYY("২০ নভেম্বর ২০২৬", "20 November 2026"),
    DD_MM_YYYY("২০/১১/২০২৬", "20/11/2026"),
    YYYY_MM_DD("২০২৬-১১-২০", "2026-11-20");

    fun label(isBangla: Boolean): String = if (isBangla) bnLabel else enLabel
}

enum class DefaultDashboardOption(val route: String, val bnLabel: String, val enLabel: String) {
    DASHBOARD("dashboard", "ড্যাশবোর্ড", "Dashboard"),
    SALARY_LEDGER("salary_ledger", "বেতন খাতা", "Salary Ledger"),
    MONTHLY_LEDGER("monthly_ledger", "১২ মাস", "12 Months"),
    PERSONAL_NOTES("personal_notes", "ব্যক্তিগত নোট", "Personal Notes"),
    REMINDERS("reminders", "রিমাইন্ডার", "Reminders"),
    PRAYER_TIMES("prayer_times", "নামাজের সময়", "Prayer Times");

    fun label(isBangla: Boolean): String = if (isBangla) bnLabel else enLabel
}

enum class PrayerCalcMethod(
    val bnName: String,
    val enName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int? = null
) {
    KARACHI("কারাচি (বাংলাদেশ মানক)", "University of Islamic Sciences, Karachi", 18.0, 18.0),
    MWL("মুসলিম ওয়ার্ল্ড লীগ (MWL)", "Muslim World League", 18.0, 17.0),
    ISNA("ইসলামিক সোসাইটি (ISNA)", "ISNA (North America)", 15.0, 15.0),
    MAKKAH("উম্মুল কুরা (মক্কা)", "Umm al-Qura, Makkah", 18.5, 0.0, 90),
    EGYPT("মিশরীয় জরিপ কর্তৃপক্ষ", "Egyptian General Authority", 19.5, 17.5);

    fun title(isBangla: Boolean): String = if (isBangla) bnName else enName
}

enum class AsrJuristicMethod(val bnName: String, val enName: String, val shadowFactor: Int) {
    STANDARD("স্ট্যান্ডার্ড (শাফেয়ী/মালেকী/হাম্বলী)", "Standard (Shafi'i / Maliki / Hanbali)", 1),
    HANAFI("হানাফী পদ্ধতি", "Hanafi Method", 2);

    fun title(isBangla: Boolean): String = if (isBangla) bnName else enName
}

data class AppSettings(
    val language: AppLanguage = AppLanguage.BANGLA,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val numeralStyle: NumeralStyle = NumeralStyle.BANGLA,
    val clockFormat: ClockFormat = ClockFormat.HOUR_12,
    val prayerMethod: PrayerCalcMethod = PrayerCalcMethod.KARACHI,
    val asrMethod: AsrJuristicMethod = AsrJuristicMethod.HANAFI,
    val locationNameBn: String = "ঢাকা, বাংলাদেশ",
    val locationNameEn: String = "Dhaka, Bangladesh",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125,
    val calculatorMemory: Double = 0.0,
    // App Customization & Profile Display Preferences
    val currency: CurrencyOption = CurrencyOption.BDT,
    val dateFormat: DateFormatStyle = DateFormatStyle.DD_MMM_YYYY,
    val defaultDashboard: DefaultDashboardOption = DefaultDashboardOption.DASHBOARD,
    val showProfileOnDashboard: Boolean = true,
    val showPhoneInProfileCard: Boolean = true,
    val showEmailInProfileCard: Boolean = false,
    val strictPrivateMode: Boolean = true,
    // Security & PIN / Biometric Lock (hashed only, never plain text)
    val pinEnabled: Boolean = false,
    val pinSalt: String = "",
    val pinHash: String = "",
    val biometricEnabled: Boolean = false,
    val autoLockMinutes: Int = 0
) {
    val isBangla: Boolean get() = language == AppLanguage.BANGLA
    val useBanglaDigits: Boolean get() = numeralStyle == NumeralStyle.BANGLA
    fun displayLocationName(): String = if (isBangla) locationNameBn else locationNameEn
}

class UserPreferences(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NUMERAL_STYLE = stringPreferencesKey("numeral_style")
        val CLOCK_FORMAT = stringPreferencesKey("clock_format")
        val PRAYER_METHOD = stringPreferencesKey("prayer_method")
        val ASR_METHOD = stringPreferencesKey("asr_method")
        val LOCATION_BN = stringPreferencesKey("location_bn")
        val LOCATION_EN = stringPreferencesKey("location_en")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CALC_MEMORY = doublePreferencesKey("calc_memory")
        val CURRENCY = stringPreferencesKey("currency")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val DEFAULT_DASHBOARD = stringPreferencesKey("default_dashboard")
        val SHOW_PROFILE_DASH = booleanPreferencesKey("show_profile_dash")
        val SHOW_PHONE_CARD = booleanPreferencesKey("show_phone_card")
        val SHOW_EMAIL_CARD = booleanPreferencesKey("show_email_card")
        val STRICT_PRIVATE = booleanPreferencesKey("strict_private")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AUTO_LOCK_MINUTES = intPreferencesKey("auto_lock_minutes")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            language = prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.BANGLA,
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.LIGHT,
            numeralStyle = prefs[Keys.NUMERAL_STYLE]?.let { runCatching { NumeralStyle.valueOf(it) }.getOrNull() }
                ?: NumeralStyle.BANGLA,
            clockFormat = prefs[Keys.CLOCK_FORMAT]?.let { runCatching { ClockFormat.valueOf(it) }.getOrNull() }
                ?: ClockFormat.HOUR_12,
            prayerMethod = prefs[Keys.PRAYER_METHOD]?.let { runCatching { PrayerCalcMethod.valueOf(it) }.getOrNull() }
                ?: PrayerCalcMethod.KARACHI,
            asrMethod = prefs[Keys.ASR_METHOD]?.let { runCatching { AsrJuristicMethod.valueOf(it) }.getOrNull() }
                ?: AsrJuristicMethod.HANAFI,
            locationNameBn = prefs[Keys.LOCATION_BN] ?: "ঢাকা, বাংলাদেশ",
            locationNameEn = prefs[Keys.LOCATION_EN] ?: "Dhaka, Bangladesh",
            latitude = prefs[Keys.LATITUDE] ?: 23.8103,
            longitude = prefs[Keys.LONGITUDE] ?: 90.4125,
            calculatorMemory = prefs[Keys.CALC_MEMORY] ?: 0.0,
            currency = prefs[Keys.CURRENCY]?.let { runCatching { CurrencyOption.valueOf(it) }.getOrNull() }
                ?: CurrencyOption.BDT,
            dateFormat = prefs[Keys.DATE_FORMAT]?.let { runCatching { DateFormatStyle.valueOf(it) }.getOrNull() }
                ?: DateFormatStyle.DD_MMM_YYYY,
            defaultDashboard = prefs[Keys.DEFAULT_DASHBOARD]?.let { runCatching { DefaultDashboardOption.valueOf(it) }.getOrNull() }
                ?: DefaultDashboardOption.DASHBOARD,
            showProfileOnDashboard = prefs[Keys.SHOW_PROFILE_DASH] ?: true,
            showPhoneInProfileCard = prefs[Keys.SHOW_PHONE_CARD] ?: true,
            showEmailInProfileCard = prefs[Keys.SHOW_EMAIL_CARD] ?: false,
            strictPrivateMode = prefs[Keys.STRICT_PRIVATE] ?: true,
            pinEnabled = prefs[Keys.PIN_ENABLED] ?: false,
            pinSalt = prefs[Keys.PIN_SALT] ?: "",
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            autoLockMinutes = prefs[Keys.AUTO_LOCK_MINUTES] ?: 0
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setNumeralStyle(style: NumeralStyle) {
        context.dataStore.edit { it[Keys.NUMERAL_STYLE] = style.name }
    }

    suspend fun setClockFormat(format: ClockFormat) {
        context.dataStore.edit { it[Keys.CLOCK_FORMAT] = format.name }
    }

    suspend fun setPrayerMethod(method: PrayerCalcMethod) {
        context.dataStore.edit { it[Keys.PRAYER_METHOD] = method.name }
    }

    suspend fun setAsrMethod(method: AsrJuristicMethod) {
        context.dataStore.edit { it[Keys.ASR_METHOD] = method.name }
    }

    suspend fun setLocation(nameBn: String, nameEn: String, lat: Double, lng: Double) {
        context.dataStore.edit {
            it[Keys.LOCATION_BN] = nameBn
            it[Keys.LOCATION_EN] = nameEn
            it[Keys.LATITUDE] = lat
            it[Keys.LONGITUDE] = lng
        }
    }

    suspend fun setCalculatorMemory(value: Double) {
        context.dataStore.edit { it[Keys.CALC_MEMORY] = value }
    }

    suspend fun setCurrency(currency: CurrencyOption) {
        context.dataStore.edit { it[Keys.CURRENCY] = currency.name }
    }

    suspend fun setDateFormat(format: DateFormatStyle) {
        context.dataStore.edit { it[Keys.DATE_FORMAT] = format.name }
    }

    suspend fun setDefaultDashboard(option: DefaultDashboardOption) {
        context.dataStore.edit { it[Keys.DEFAULT_DASHBOARD] = option.name }
    }

    suspend fun setProfileDisplayPrefs(
        showOnDashboard: Boolean,
        showPhone: Boolean,
        showEmail: Boolean,
        strictPrivate: Boolean
    ) {
        context.dataStore.edit {
            it[Keys.SHOW_PROFILE_DASH] = showOnDashboard
            it[Keys.SHOW_PHONE_CARD] = showPhone
            it[Keys.SHOW_EMAIL_CARD] = showEmail
            it[Keys.STRICT_PRIVATE] = strictPrivate
        }
    }

    suspend fun setPin(salt: String, hash: String) {
        context.dataStore.edit {
            it[Keys.PIN_ENABLED] = true
            it[Keys.PIN_SALT] = salt
            it[Keys.PIN_HASH] = hash
        }
    }

    suspend fun disablePin() {
        context.dataStore.edit {
            it[Keys.PIN_ENABLED] = false
            it[Keys.PIN_SALT] = ""
            it[Keys.PIN_HASH] = ""
            it[Keys.BIOMETRIC_ENABLED] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setAutoLockMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.AUTO_LOCK_MINUTES] = minutes }
    }

    suspend fun restoreSettings(settings: AppSettings) {
        context.dataStore.edit {
            it[Keys.LANGUAGE] = settings.language.name
            it[Keys.THEME_MODE] = settings.themeMode.name
            it[Keys.NUMERAL_STYLE] = settings.numeralStyle.name
            it[Keys.CLOCK_FORMAT] = settings.clockFormat.name
            it[Keys.PRAYER_METHOD] = settings.prayerMethod.name
            it[Keys.ASR_METHOD] = settings.asrMethod.name
            it[Keys.LOCATION_BN] = settings.locationNameBn
            it[Keys.LOCATION_EN] = settings.locationNameEn
            it[Keys.LATITUDE] = settings.latitude
            it[Keys.LONGITUDE] = settings.longitude
            it[Keys.CURRENCY] = settings.currency.name
            it[Keys.DATE_FORMAT] = settings.dateFormat.name
            it[Keys.DEFAULT_DASHBOARD] = settings.defaultDashboard.name
            it[Keys.SHOW_PROFILE_DASH] = settings.showProfileOnDashboard
            it[Keys.SHOW_PHONE_CARD] = settings.showPhoneInProfileCard
            it[Keys.SHOW_EMAIL_CARD] = settings.showEmailInProfileCard
            it[Keys.STRICT_PRIVATE] = settings.strictPrivateMode
        }
    }
}
