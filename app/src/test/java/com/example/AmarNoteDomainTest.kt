package com.example

import com.example.data.local.AppLanguage
import com.example.data.local.AppSettings
import com.example.data.local.AsrJuristicMethod
import com.example.data.local.BackupManager
import com.example.data.local.PaymentMethod
import com.example.data.local.PrayerCalcMethod
import com.example.data.local.SalaryRecord
import com.example.domain.LocalizationHelper
import com.example.domain.PrayerTimesCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AmarNoteDomainTest {

    @Test
    fun `bangla calendar conversion for 2026-11-20 equals 5 Ogrohayon 1433`() {
        val bd = LocalizationHelper.toBanglaCalendarDate(2026, 11, 20)
        assertEquals(5, bd.day)
        assertEquals("অগ্রহায়ণ", bd.monthNameBn)
        assertEquals(1433, bd.year)
    }

    @Test
    fun `bangla calendar pohela boishakh on April 14`() {
        val bd = LocalizationHelper.toBanglaCalendarDate(2026, 4, 14)
        assertEquals(1, bd.day)
        assertEquals("বৈশাখ", bd.monthNameBn)
        assertEquals(1433, bd.year)
    }

    @Test
    fun `dhaka prayer times and makruh windows are chronologically ordered`() {
        val daily = PrayerTimesCalculator.calculateDaily(
            year = 2026,
            month = 11,
            day = 20,
            latitude = 23.8103,
            longitude = 90.4125,
            method = PrayerCalcMethod.KARACHI,
            asrMethod = AsrJuristicMethod.HANAFI,
            tzOffsetHours = 6.0
        )

        // Verify chronological order across the day
        assertTrue(daily.fajrMinutes < daily.sunriseMinutes)
        assertTrue(daily.sunriseMinutes < daily.makruhSunrise.endMinutesFromMidnight)
        assertTrue(daily.makruhSunrise.endMinutesFromMidnight < daily.makruhZawal.startMinutesFromMidnight)
        assertTrue(daily.makruhZawal.endMinutesFromMidnight < daily.dhuhrMinutes)
        assertTrue(daily.dhuhrMinutes < daily.asrMinutes)
        assertTrue(daily.asrMinutes < daily.makruhSunset.startMinutesFromMidnight)
        assertTrue(daily.makruhSunset.startMinutesFromMidnight < daily.sunsetMinutes)
        assertTrue(daily.maghribMinutes < daily.ishaMinutes)

        // Dhaka in late November: Sunrise ~6:17 (377 min), Sunset ~17:11 (1031 min)
        assertTrue(daily.sunriseMinutes in 360.0..400.0)
        assertTrue(daily.sunsetMinutes in 1015.0..1055.0)
    }

    @Test
    fun `dhaka qibla bearing points roughly west-northwest around 277 degrees`() {
        val qibla = PrayerTimesCalculator.calculateQibla(23.8103, 90.4125)
        assertTrue(qibla.bearingDegrees in 275.0..280.0)
        assertTrue(qibla.distanceKm in 5000.0..5300.0)
    }

    @Test
    fun `csv export includes UTF-8 BOM and record fields`() {
        val rec = SalaryRecord(
            amount = 30000.0,
            year = 2026,
            month = 1,
            day = 5,
            hour = 9,
            minute = 30,
            second = 0,
            dayOfWeek = Calendar.MONDAY,
            paymentMethod = PaymentMethod.NAGAD.name,
            comment = "জানুয়ারি বেতন"
        )
        val csv = BackupManager.buildCsv(listOf(rec), isBangla = true)
        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("2026-01-05"))
        assertTrue(csv.contains("30000.0"))
        assertTrue(csv.contains("জানুয়ারি বেতন"))
    }
}
