package com.example.domain

import com.example.data.local.AsrJuristicMethod
import com.example.data.local.PrayerCalcMethod
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class PrayerSlot(val bnName: String, val enName: String) {
    FAJR("ফজর", "Fajr"),
    SUNRISE("সূর্যোদয়", "Sunrise"),
    DHUHR("যোহর", "Dhuhr"),
    ASR("আসর", "Asr"),
    MAGHRIB("মাগরিব", "Maghrib"),
    ISHA("এশা", "Isha");

    fun label(isBangla: Boolean): String = if (isBangla) bnName else enName
}

data class MakruhWindow(
    val titleBn: String,
    val titleEn: String,
    val descBn: String,
    val descEn: String,
    val startMinutesFromMidnight: Double,
    val endMinutesFromMidnight: Double
) {
    fun title(isBangla: Boolean): String = if (isBangla) titleBn else titleEn
    fun description(isBangla: Boolean): String = if (isBangla) descBn else descEn
}

data class DailyPrayerTimes(
    val year: Int,
    val month: Int,
    val day: Int,
    val fajrMinutes: Double,
    val sunriseMinutes: Double,
    val dhuhrMinutes: Double,
    val asrMinutes: Double,
    val sunsetMinutes: Double,
    val maghribMinutes: Double,
    val ishaMinutes: Double,
    val makruhSunrise: MakruhWindow,
    val makruhZawal: MakruhWindow,
    val makruhSunset: MakruhWindow
) {
    fun timeForSlot(slot: PrayerSlot): Double = when (slot) {
        PrayerSlot.FAJR -> fajrMinutes
        PrayerSlot.SUNRISE -> sunriseMinutes
        PrayerSlot.DHUHR -> dhuhrMinutes
        PrayerSlot.ASR -> asrMinutes
        PrayerSlot.MAGHRIB -> maghribMinutes
        PrayerSlot.ISHA -> ishaMinutes
    }
}

data class NextPrayerInfo(
    val slot: PrayerSlot,
    val targetMinutesFromMidnight: Double,
    val remainingSeconds: Long,
    val isTomorrow: Boolean
)

data class QiblaData(
    val bearingDegrees: Double,
    val distanceKm: Double
)

object PrayerTimesCalculator {
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    private fun deg2rad(d: Double): Double = d * PI / 180.0
    private fun rad2deg(r: Double): Double = r * 180.0 / PI

    private fun fixAngle(a: Double): Double {
        var res = a % 360.0
        if (res < 0) res += 360.0
        return res
    }

    private fun fixHour(h: Double): Double {
        var res = h % 24.0
        if (res < 0) res += 24.0
        return res
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private data class SolarPosition(val declinationRad: Double, val equationOfTimeHours: Double)

    private fun sunPosition(jd: Double): SolarPosition {
        val d = jd - 2451545.0
        val g = deg2rad(fixAngle(357.529 + 0.98560028 * d))
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = deg2rad(fixAngle(q + 1.915 * sin(g) + 0.020 * sin(2.0 * g)))
        val e = deg2rad(23.439 - 0.00000036 * d)

        val ra = rad2deg(atan2(cos(e) * sin(l), cos(l))) / 15.0
        val decl = asin(sin(e) * sin(l))
        var eqt = q / 15.0 - fixHour(ra)
        if (eqt > 12.0) eqt -= 24.0
        if (eqt < -12.0) eqt += 24.0
        return SolarPosition(declinationRad = decl, equationOfTimeHours = eqt)
    }

    private fun computeSunTimeHours(
        jd: Double,
        latDeg: Double,
        lngDeg: Double,
        tzHours: Double,
        approxLocalHours: Double,
        altitudeDeg: Double,
        isMorning: Boolean
    ): Double {
        val pos = sunPosition(jd + (approxLocalHours - tzHours) / 24.0)
        val latRad = deg2rad(latDeg)
        val noon = 12.0 + tzHours - lngDeg / 15.0 - pos.equationOfTimeHours
        val cosH = (sin(deg2rad(altitudeDeg)) - sin(latRad) * sin(pos.declinationRad)) /
            (cos(latRad) * cos(pos.declinationRad))
        val clamped = cosH.coerceIn(-0.9999, 0.9999)
        val hourAngleHours = rad2deg(acos(clamped)) / 15.0
        return if (isMorning) noon - hourAngleHours else noon + hourAngleHours
    }

    private fun computeSolarNoonHours(
        jd: Double,
        lngDeg: Double,
        tzHours: Double
    ): Double {
        val approxUtc = 0.5 - lngDeg / 360.0
        val pos1 = sunPosition(jd + approxUtc)
        val noon1 = 12.0 + tzHours - lngDeg / 15.0 - pos1.equationOfTimeHours
        val pos2 = sunPosition(jd + (noon1 - tzHours) / 24.0)
        return 12.0 + tzHours - lngDeg / 15.0 - pos2.equationOfTimeHours
    }

    private fun computeAsrHours(
        jd: Double,
        latDeg: Double,
        lngDeg: Double,
        tzHours: Double,
        shadowFactor: Int
    ): Double {
        val approxAsr = 15.5
        val pos = sunPosition(jd + (approxAsr - tzHours) / 24.0)
        val latRad = deg2rad(latDeg)
        val angleRad = atan(1.0 / (shadowFactor + tan(abs(latRad - pos.declinationRad))))
        val altitudeDeg = rad2deg(angleRad)
        return computeSunTimeHours(jd, latDeg, lngDeg, tzHours, approxAsr, altitudeDeg, isMorning = false)
    }

    fun getTimeZoneOffsetHours(
        year: Int,
        month: Int,
        day: Int,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Double {
        val cal = GregorianCalendar(timeZone).apply {
            set(year, month - 1, day, 12, 0, 0)
        }
        return timeZone.getOffset(cal.timeInMillis) / 3_600_000.0
    }

    fun calculateDaily(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        method: PrayerCalcMethod,
        asrMethod: AsrJuristicMethod,
        tzOffsetHours: Double = getTimeZoneOffsetHours(year, month, day)
    ): DailyPrayerTimes {
        val jd = julianDate(year, month, day)

        // Two-pass refinement for astronomical accuracy
        val noonH = computeSolarNoonHours(jd, longitude, tzOffsetHours)
        val fajrH = computeSunTimeHours(jd, latitude, longitude, tzOffsetHours, 5.0, -method.fajrAngle, true)
        val sunriseH = computeSunTimeHours(jd, latitude, longitude, tzOffsetHours, 6.0, -0.8333, true)
        val sunriseMakruhEndH = computeSunTimeHours(jd, latitude, longitude, tzOffsetHours, 6.3, 4.5, true)
        val dhuhrH = noonH + 1.0 / 60.0 // +1 min safety past exact solar transit
        val asrH = computeAsrHours(jd, latitude, longitude, tzOffsetHours, asrMethod.shadowFactor)
        val sunsetMakruhStartH = computeSunTimeHours(jd, latitude, longitude, tzOffsetHours, 17.5, 4.5, false)
        val sunsetH = computeSunTimeHours(jd, latitude, longitude, tzOffsetHours, 18.0, -0.8333, false)
        val maghribH = sunsetH
        val ishaH = if (method.ishaMinutesAfterMaghrib != null) {
            maghribH + method.ishaMinutesAfterMaghrib / 60.0
        } else {
            computeSunTimeHours(jd, latitude, longitude, tzOffsetHours, 19.5, -method.ishaAngle, false)
        }

        val noonMin = noonH * 60.0
        val sunriseMin = sunriseH * 60.0
        val sunriseMakruhEndMin = sunriseMakruhEndH * 60.0
        val sunsetMakruhStartMin = sunsetMakruhStartH * 60.0
        val sunsetMin = sunsetH * 60.0

        return DailyPrayerTimes(
            year = year,
            month = month,
            day = day,
            fajrMinutes = fajrH * 60.0,
            sunriseMinutes = sunriseMin,
            dhuhrMinutes = dhuhrH * 60.0,
            asrMinutes = asrH * 60.0,
            sunsetMinutes = sunsetMin,
            maghribMinutes = maghribH * 60.0,
            ishaMinutes = ishaH * 60.0,
            makruhSunrise = MakruhWindow(
                titleBn = "১. সূর্যোদয়ের নিষিদ্ধ সময়",
                titleEn = "1. Sunrise Forbidden Window",
                descBn = "সূর্য উদিত হওয়া শুরু থেকে এক বর্শা পরিমাণ (৪.৫° উচ্চতা) ওপরে ওঠা পর্যন্ত নামাজ পড়া নিষিদ্ধ।",
                descEn = "From exact sunrise (-0.833°) until the sun rises to +4.5° altitude above the horizon.",
                startMinutesFromMidnight = sunriseMin,
                endMinutesFromMidnight = sunriseMakruhEndMin
            ),
            makruhZawal = MakruhWindow(
                titleBn = "২. দ্বিপ্রহরের যাওয়াল (মধ্যাহ্ন) সময়",
                titleEn = "2. Solar Zenith (Zawal) Window",
                descBn = "সূর্য ঠিক মাথার ওপর (মেরিডিয়ান ট্রানজিট) অবস্থানকালে যোহরের পূর্ব পর্যন্ত নামাজ পড়া নিষিদ্ধ।",
                descEn = "When the sun reaches exact astronomical solar meridian transit right before Dhuhr begins.",
                startMinutesFromMidnight = noonMin - 8.0,
                endMinutesFromMidnight = noonMin
            ),
            makruhSunset = MakruhWindow(
                titleBn = "৩. সূর্যাস্তের নিষিদ্ধ সময়",
                titleEn = "3. Sunset Forbidden Window",
                descBn = "সূর্য হলুদ বর্ণ ধারণ করে অস্তগামী হলে (৪.৫° থেকে অস্ত পর্যন্ত) নফল ও সাধারণ নামাজ নিষিদ্ধ (ঐ দিনের আসর ব্যতীত)।",
                descEn = "From sun descending below +4.5° altitude until exact astronomical sunset (-0.833°).",
                startMinutesFromMidnight = sunsetMakruhStartMin,
                endMinutesFromMidnight = sunsetMin
            )
        )
    }

    fun calculateMonth(
        year: Int,
        month: Int,
        latitude: Double,
        longitude: Double,
        method: PrayerCalcMethod,
        asrMethod: AsrJuristicMethod
    ): List<DailyPrayerTimes> {
        val cal = GregorianCalendar(year, month - 1, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..daysInMonth).map { d ->
            calculateDaily(year, month, d, latitude, longitude, method, asrMethod)
        }
    }

    fun findNextPrayer(
        today: DailyPrayerTimes,
        tomorrow: DailyPrayerTimes,
        nowHour: Int,
        nowMinute: Int,
        nowSecond: Int
    ): NextPrayerInfo {
        val nowSeconds = nowHour * 3600L + nowMinute * 60L + nowSecond
        val slots = listOf(
            PrayerSlot.FAJR,
            PrayerSlot.SUNRISE,
            PrayerSlot.DHUHR,
            PrayerSlot.ASR,
            PrayerSlot.MAGHRIB,
            PrayerSlot.ISHA
        )
        for (slot in slots) {
            val slotSeconds = (today.timeForSlot(slot) * 60.0).roundToInt().toLong()
            if (slotSeconds > nowSeconds) {
                return NextPrayerInfo(
                    slot = slot,
                    targetMinutesFromMidnight = today.timeForSlot(slot),
                    remainingSeconds = slotSeconds - nowSeconds,
                    isTomorrow = false
                )
            }
        }
        val tomorrowFajrSec = (tomorrow.fajrMinutes * 60.0).roundToInt().toLong()
        val remaining = (86400L - nowSeconds) + tomorrowFajrSec
        return NextPrayerInfo(
            slot = PrayerSlot.FAJR,
            targetMinutesFromMidnight = tomorrow.fajrMinutes,
            remainingSeconds = remaining,
            isTomorrow = true
        )
    }

    fun formatMinutesTime(
        minutesFromMidnight: Double,
        is12Hour: Boolean,
        isBangla: Boolean,
        useBanglaDigits: Boolean
    ): String {
        val totalMinutes = ((minutesFromMidnight.roundToInt() % 1440) + 1440) % 1440
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return LocalizationHelper.formatTime(
            hour24 = h,
            minute = m,
            second = null,
            is12Hour = is12Hour,
            isBangla = isBangla,
            useBanglaDigits = useBanglaDigits
        )
    }

    fun formatCountdown(
        remainingSeconds: Long,
        useBanglaDigits: Boolean
    ): String {
        val safe = remainingSeconds.coerceAtLeast(0L)
        val h = (safe / 3600L).toInt()
        val m = ((safe % 3600L) / 60L).toInt()
        val s = (safe % 60L).toInt()
        return "${LocalizationHelper.formatInt(h, useBanglaDigits, true)}:" +
            "${LocalizationHelper.formatInt(m, useBanglaDigits, true)}:" +
            LocalizationHelper.formatInt(s, useBanglaDigits, true)
    }

    fun calculateQibla(latitude: Double, longitude: Double): QiblaData {
        val lat1 = deg2rad(latitude)
        val lng1 = deg2rad(longitude)
        val lat2 = deg2rad(KAABA_LAT)
        val lng2 = deg2rad(KAABA_LNG)
        val dLng = lng2 - lng1

        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        val bearing = fixAngle(rad2deg(atan2(y, x)))

        // Haversine distance in km
        val dLat = lat2 - lat1
        val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
            cos(lat1) * cos(lat2) * sin(dLng / 2.0) * sin(dLng / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        val earthRadiusKm = 6371.0
        return QiblaData(bearingDegrees = bearing, distanceKm = earthRadiusKm * c)
    }
}
