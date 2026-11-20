package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppSettings
import com.example.data.local.AsrJuristicMethod
import com.example.data.local.ClockFormat
import com.example.data.local.PrayerCalcMethod
import com.example.domain.DailyPrayerTimes
import com.example.domain.LocalizationHelper
import com.example.domain.MakruhWindow
import com.example.domain.NextPrayerInfo
import com.example.domain.PrayerSlot
import com.example.domain.PrayerTimesCalculator
import com.example.ui.components.IconBadge3D
import com.example.ui.components.LocationPickerDialog
import com.example.ui.components.NotebookPageCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentSkyBlue
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.LiveClockState

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    clock: LiveClockState,
    settings: AppSettings,
    dailyTimes: DailyPrayerTimes,
    nextPrayer: NextPrayerInfo,
    calendarYear: Int,
    calendarMonth: Int,
    monthlyCalendar: List<DailyPrayerTimes>,
    onChangeCalendarMonthYear: (Int, Int) -> Unit,
    onSelectLocation: (String, String, Double, Double) -> Unit,
    onSelectPrayerMethod: (PrayerCalcMethod) -> Unit,
    onSelectAsrMethod: (AsrJuristicMethod) -> Unit,
    onMessage: (String) -> Unit
) {
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits
    val is12Hour = settings.clockFormat == ClockFormat.HOUR_12

    var selectedTab by remember { mutableIntStateOf(0) }
    var showLocationDialog by remember { mutableStateOf(false) }

    val tabTitles = if (isBn) {
        listOf("দৈনিক সময়", "মাকরুহ সময়", "মাসিক ক্যালেন্ডার")
    } else {
        listOf("Daily Times", "Makruh Times", "Monthly")
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, maxLines = 1) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Next Prayer Countdown Hero Card
                        item {
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF0F766E), Color(0xFF0284C7))
                                            )
                                        )
                                        .padding(18.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = settings.displayLocationName(),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color(0xFFCCFBF1)
                                                )
                                                Text(
                                                    text = if (isBn) "পরবর্তী ওয়াক্ত: ${nextPrayer.slot.label(true)}"
                                                    else "Next: ${nextPrayer.slot.label(false)}",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = { showLocationDialog = true },
                                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                                    contentColor = Color.White
                                                )
                                            ) {
                                                Icon(Icons.Filled.EditLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(if (isBn) "অবস্থান" else "Location")
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Text(
                                                text = PrayerTimesCalculator.formatMinutesTime(
                                                    nextPrayer.targetMinutesFromMidnight, is12Hour, isBn, useBnDigits
                                                ),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFDE68A)
                                            )
                                            Text(
                                                text = PrayerTimesCalculator.formatCountdown(nextPrayer.remainingSeconds, useBnDigits),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontFamily = JetBrainsMonoFamily,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Calculation Method & Asr Method Quick Selectors
                        item {
                            NotebookPageCard(accentColor = AccentSkyBlue, showRuledLines = false) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = if (isBn) "আসরের হিসাব ও পদ্ধতি (অফলাইন ক্যালকুলেশন)" else "Asr & Calculation Method (Offline)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AsrJuristicMethod.entries.forEach { asr ->
                                            FilterChip(
                                                selected = settings.asrMethod == asr,
                                                onClick = { onSelectAsrMethod(asr) },
                                                label = { Text(if (asr == AsrJuristicMethod.HANAFI) (if (isBn) "হানাফী" else "Hanafi") else (if (isBn) "স্ট্যান্ডার্ড" else "Standard")) }
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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

                        // 5 Daily Prayers + Sunrise & Sunset
                        item {
                            val rows = listOf(
                                Triple(if (isBn) "ফজর" else "Fajr", dailyTimes.fajrMinutes, Icons.Filled.WbTwilight to AccentIndigo),
                                Triple(if (isBn) "সূর্যোদয়" else "Sunrise", dailyTimes.sunriseMinutes, Icons.Filled.WbSunny to AccentAmber),
                                Triple(if (isBn) "যোহর" else "Dhuhr", dailyTimes.dhuhrMinutes, Icons.Filled.WbSunny to AccentOrange),
                                Triple(if (isBn) "আসর" else "Asr", dailyTimes.asrMinutes, Icons.Filled.WbSunny to AccentEmerald),
                                Triple(if (isBn) "সূর্যাস্ত" else "Sunset", dailyTimes.sunsetMinutes, Icons.Filled.WbTwilight to AccentRose),
                                Triple(if (isBn) "মাগরিব" else "Maghrib", dailyTimes.maghribMinutes, Icons.Filled.NightsStay to AccentPurple),
                                Triple(if (isBn) "এশা" else "Isha", dailyTimes.ishaMinutes, Icons.Filled.Mosque to AccentSkyBlue)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                rows.forEach { (label, mins, iconTint) ->
                                    val isNext = nextPrayer.slot.label(isBn) == label && !nextPrayer.isTomorrow
                                    PrayerRowCard(
                                        name = label,
                                        time = PrayerTimesCalculator.formatMinutesTime(mins, is12Hour, isBn, useBnDigits),
                                        icon = iconTint.first,
                                        accent = iconTint.second,
                                        isHighlighted = isNext
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Dedicated Makruh Times Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Surface(
                                color = AccentRose.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    IconBadge3D(
                                        icon = Icons.Filled.WarningAmber,
                                        tint = AccentRose,
                                        contentDescription = null,
                                        size = 44.dp
                                    )
                                    Column {
                                        Text(
                                            text = if (isBn) "নামাজের ৩টি নিষিদ্ধ (মাকরুহ তাহরিমি) সময়" else "3 Makruh (Forbidden) Prayer Times",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentRose
                                        )
                                        Text(
                                            text = if (isBn) "${settings.displayLocationName()} অনুযায়ী সূর্যের জ্যোতির্বৈজ্ঞানিক অবস্থান থেকে নির্ভুলভাবে গণনা করা হয়েছে।"
                                            else "Calculated astronomically from solar altitude & meridian transit for ${settings.displayLocationName()}.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        val makruhList = listOf(
                            dailyTimes.makruhSunrise to AccentOrange,
                            dailyTimes.makruhZawal to AccentRose,
                            dailyTimes.makruhSunset to AccentPurple
                        )
                        items(makruhList) { (window, color) ->
                            MakruhWindowCard(
                                window = window,
                                color = color,
                                settings = settings
                            )
                        }
                    }
                }

                else -> {
                    // Monthly Prayer Calendar Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val prevM = if (calendarMonth == 1) 12 else calendarMonth - 1
                                        val prevY = if (calendarMonth == 1) calendarYear - 1 else calendarYear
                                        onChangeCalendarMonthYear(prevY, prevM)
                                    }
                                ) {
                                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                                }
                                Text(
                                    text = "${LocalizationHelper.gregorianMonthName(calendarMonth, isBn)} ${LocalizationHelper.formatInt(calendarYear, useBnDigits)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = {
                                        val nextM = if (calendarMonth == 12) 1 else calendarMonth + 1
                                        val nextY = if (calendarMonth == 12) calendarYear + 1 else calendarYear
                                        onChangeCalendarMonthYear(nextY, nextM)
                                    }
                                ) {
                                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                                }
                            }
                        }

                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val headers = if (isBn) {
                                        listOf("দিন", "ফজর", "যোহর", "আসর", "মাগরিব", "এশা")
                                    } else {
                                        listOf("Day", "Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                                    }
                                    headers.forEach { h ->
                                        Text(
                                            text = h,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        items(monthlyCalendar) { dayItem ->
                            val isToday = dayItem.year == clock.year && dayItem.month == clock.month && dayItem.day == clock.day
                            Surface(
                                color = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = LocalizationHelper.formatInt(dayItem.day, useBnDigits, true),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = PrayerTimesCalculator.formatMinutesTime(dayItem.fajrMinutes, is12Hour, isBn, useBnDigits),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = PrayerTimesCalculator.formatMinutesTime(dayItem.dhuhrMinutes, is12Hour, isBn, useBnDigits),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = PrayerTimesCalculator.formatMinutesTime(dayItem.asrMinutes, is12Hour, isBn, useBnDigits),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = PrayerTimesCalculator.formatMinutesTime(dayItem.maghribMinutes, is12Hour, isBn, useBnDigits),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = PrayerTimesCalculator.formatMinutesTime(dayItem.ishaMinutes, is12Hour, isBn, useBnDigits),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
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
}

@Composable
private fun PrayerRowCard(
    name: String,
    time: String,
    icon: ImageVector,
    accent: Color,
    isHighlighted: Boolean
) {
    Surface(
        color = if (isHighlighted) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = if (isHighlighted) 3.dp else 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconBadge3D(icon = icon, tint = accent, contentDescription = null, size = 38.dp)
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                    color = if (isHighlighted) accent else MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = time,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isHighlighted) accent else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MakruhWindowCard(
    window: MakruhWindow,
    color: Color,
    settings: AppSettings
) {
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits
    val is12Hour = settings.clockFormat == ClockFormat.HOUR_12

    val startStr = PrayerTimesCalculator.formatMinutesTime(window.startMinutesFromMidnight, is12Hour, isBn, useBnDigits)
    val endStr = PrayerTimesCalculator.formatMinutesTime(window.endMinutesFromMidnight, is12Hour, isBn, useBnDigits)

    NotebookPageCard(accentColor = color, showRuledLines = false) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = window.title(isBn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Surface(
                color = color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isBn) "শুরু: $startStr   —   শেষ: $endStr" else "Start: $startStr   —   End: $endStr",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = color,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Text(
                text = window.description(isBn),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
