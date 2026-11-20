package com.example.domain

import com.example.data.local.CurrencyOption
import com.example.data.local.DateFormatStyle
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

data class BanglaDate(
    val day: Int,
    val monthIndex: Int, // 0..11
    val monthNameBn: String,
    val monthNameEn: String,
    val year: Int
)

object LocalizationHelper {
    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    private val banglaMonthsGregorian = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    private val englishMonthsGregorian = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val banglaMonthsBanglaCal = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )
    private val englishMonthsBanglaCal = listOf(
        "Boishakh", "Joishtho", "Asharh", "Srabon", "Bhadro", "Ashwin",
        "Kartik", "Ogrohayon", "Poush", "Magh", "Falgun", "Choitro"
    )

    private val banglaWeekdays = listOf(
        "", "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"
    )
    private val englishWeekdays = listOf(
        "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    fun toLocalizedDigits(input: String, useBanglaDigits: Boolean): String {
        if (!useBanglaDigits) return input
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatInt(value: Int, useBanglaDigits: Boolean, padTwo: Boolean = false): String {
        val raw = if (padTwo) String.format(Locale.US, "%02d", value) else value.toString()
        return toLocalizedDigits(raw, useBanglaDigits)
    }

    fun formatCurrency(
        amount: Double,
        useBanglaDigits: Boolean,
        includeSymbol: Boolean = true,
        currency: CurrencyOption = CurrencyOption.BDT
    ): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val hasDecimals = abs(amount - amount.roundToLong()) > 0.005
        val formatter = if (hasDecimals) {
            DecimalFormat("#,##0.00", symbols)
        } else {
            DecimalFormat("#,##0", symbols)
        }
        val formatted = toLocalizedDigits(formatter.format(amount), useBanglaDigits)
        return if (includeSymbol) "${currency.symbol} $formatted" else formatted
    }

    fun formatDecimal(value: Double, useBanglaDigits: Boolean, decimals: Int = 1): String {
        val raw = String.format(Locale.US, "%.${decimals}f", value)
        return toLocalizedDigits(raw, useBanglaDigits)
    }

    fun gregorianMonthName(month1To12: Int, isBangla: Boolean): String {
        val idx = (month1To12 - 1).coerceIn(0, 11)
        return if (isBangla) banglaMonthsGregorian[idx] else englishMonthsGregorian[idx]
    }

    fun weekdayName(calendarDayOfWeek: Int, isBangla: Boolean): String {
        val idx = calendarDayOfWeek.coerceIn(1, 7)
        return if (isBangla) banglaWeekdays[idx] else englishWeekdays[idx]
    }

    fun formatDate(
        year: Int,
        month: Int,
        day: Int,
        isBangla: Boolean,
        useBanglaDigits: Boolean,
        style: DateFormatStyle = DateFormatStyle.DD_MMM_YYYY
    ): String {
        return when (style) {
            DateFormatStyle.DD_MMM_YYYY -> {
                val d = formatInt(day, useBanglaDigits, padTwo = false)
                val m = gregorianMonthName(month, isBangla)
                val y = formatInt(year, useBanglaDigits, padTwo = false)
                "$d $m $y"
            }
            DateFormatStyle.DD_MM_YYYY -> {
                val d = formatInt(day, useBanglaDigits, padTwo = true)
                val m = formatInt(month, useBanglaDigits, padTwo = true)
                val y = formatInt(year, useBanglaDigits, padTwo = false)
                "$d/$m/$y"
            }
            DateFormatStyle.YYYY_MM_DD -> {
                val y = formatInt(year, useBanglaDigits, padTwo = false)
                val m = formatInt(month, useBanglaDigits, padTwo = true)
                val d = formatInt(day, useBanglaDigits, padTwo = true)
                "$y-$m-$d"
            }
        }
    }

    fun formatTime(
        hour24: Int,
        minute: Int,
        second: Int?,
        is12Hour: Boolean,
        isBangla: Boolean,
        useBanglaDigits: Boolean
    ): String {
        val displayHour = if (is12Hour) {
            val mod = hour24 % 12
            if (mod == 0) 12 else mod
        } else {
            hour24
        }
        val hStr = formatInt(displayHour, useBanglaDigits, padTwo = true)
        val mStr = formatInt(minute, useBanglaDigits, padTwo = true)
        val timeCore = if (second != null) {
            val sStr = formatInt(second, useBanglaDigits, padTwo = true)
            "$hStr:$mStr:$sStr"
        } else {
            "$hStr:$mStr"
        }
        if (!is12Hour) return timeCore
        val suffix = if (isBangla) {
            when {
                hour24 < 5 -> "ভোর"
                hour24 < 12 -> "সকাল"
                hour24 < 16 -> "দুপুর"
                hour24 < 18 -> "বিকাল"
                hour24 < 20 -> "সন্ধ্যা"
                else -> "রাত"
            }
        } else {
            if (hour24 < 12) "AM" else "PM"
        }
        return if (isBangla) "$suffix $timeCore" else "$timeCore $suffix"
    }

    fun toBanglaCalendarDate(year: Int, month1To12: Int, day: Int): BanglaDate {
        val target = GregorianCalendar(year, month1To12 - 1, day)
        val pohelaBoishakhThisYear = GregorianCalendar(year, Calendar.APRIL, 14)

        val banglaYear: Int
        val startGregorianYear: Int
        if (target.before(pohelaBoishakhThisYear)) {
            banglaYear = year - 594
            startGregorianYear = year - 1
        } else {
            banglaYear = year - 593
            startGregorianYear = year
        }

        val startCal = GregorianCalendar(startGregorianYear, Calendar.APRIL, 14)
        val diffMillis = target.timeInMillis - startCal.timeInMillis
        var remainingDays = ((diffMillis + 43_200_000L) / 86_400_000L).toInt().coerceAtLeast(0)

        val falgunGregorianYear = startGregorianYear + 1
        val isLeap = isGregorianLeapYear(falgunGregorianYear)
        val monthLengths = intArrayOf(
            31, 31, 31, 31, 31, 31,
            30, 30, 30, 30,
            if (isLeap) 30 else 29,
            30
        )

        var monthIdx = 0
        while (monthIdx < 11 && remainingDays >= monthLengths[monthIdx]) {
            remainingDays -= monthLengths[monthIdx]
            monthIdx++
        }

        return BanglaDate(
            day = remainingDays + 1,
            monthIndex = monthIdx,
            monthNameBn = banglaMonthsBanglaCal[monthIdx],
            monthNameEn = englishMonthsBanglaCal[monthIdx],
            year = banglaYear
        )
    }

    fun formatBanglaCalendarDate(
        year: Int,
        month1To12: Int,
        day: Int,
        isBangla: Boolean,
        useBanglaDigits: Boolean
    ): String {
        val bd = toBanglaCalendarDate(year, month1To12, day)
        val dStr = formatInt(bd.day, useBanglaDigits)
        val yStr = formatInt(bd.year, useBanglaDigits)
        val mStr = if (isBangla) bd.monthNameBn else bd.monthNameEn
        val suffix = if (isBangla) "বঙ্গাব্দ" else "BS"
        return "$dStr $mStr $yStr $suffix"
    }

    private fun isGregorianLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}
