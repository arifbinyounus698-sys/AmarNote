package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AppSettings
import com.example.data.local.ClockFormat
import com.example.data.local.SalaryRecord
import com.example.data.local.UserProfile
import com.example.domain.LocalizationHelper
import com.example.domain.NextPrayerInfo
import com.example.domain.PrayerTimesCalculator
import com.example.ui.components.IconBadge3D
import com.example.ui.components.NotebookPageCard
import com.example.ui.components.ProfileAvatarView
import com.example.ui.navigation.AppDestination
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentSkyBlue
import com.example.ui.theme.AccentSoftPink
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.LiveClockState

@Composable
fun DashboardScreen(
    clock: LiveClockState,
    settings: AppSettings,
    profile: UserProfile,
    allSalaries: List<SalaryRecord>,
    notesCount: Int,
    upcomingRemindersCount: Int,
    nextPrayer: NextPrayerInfo,
    onQuickAddSalary: () -> Unit,
    onOpenProfileSheet: () -> Unit,
    onNavigate: (AppDestination) -> Unit
) {
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits
    val is12Hour = settings.clockFormat == ClockFormat.HOUR_12

    val currentYearSalaries = remember(allSalaries, clock.year) {
        allSalaries.filter { it.year == clock.year }
    }
    val currentYearTotal = remember(currentYearSalaries) {
        currentYearSalaries.sumOf { it.amount }
    }
    val latestSalary = allSalaries.firstOrNull()

    val gregorianDate = LocalizationHelper.formatDate(
        clock.year, clock.month, clock.day, isBn, useBnDigits, style = settings.dateFormat
    )
    val banglaDate = LocalizationHelper.formatBanglaCalendarDate(clock.year, clock.month, clock.day, isBn, useBnDigits)
    val weekday = LocalizationHelper.weekdayName(clock.dayOfWeek, isBn)
    val monthYearText = "${LocalizationHelper.gregorianMonthName(clock.month, isBn)} ${LocalizationHelper.formatInt(clock.year, useBnDigits)}"
    val liveTimeText = LocalizationHelper.formatTime(
        clock.hour24, clock.minute, clock.second, is12Hour, isBn, useBnDigits
    )
    val nextPrayerTime = PrayerTimesCalculator.formatMinutesTime(
        nextPrayer.targetMinutesFromMidnight, is12Hour, isBn, useBnDigits
    )
    val countdown = PrayerTimesCalculator.formatCountdown(nextPrayer.remainingSeconds, useBnDigits)

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Notebook Banner with Today's Date, Live Clock, Bangla & English Date, Weekday, Month/Year & Quick Add
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -20 }
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(id = R.drawable.img_notebook_banner),
                                contentDescription = if (isBn) "আমার নোট ড্যাশবোর্ড" else "AmarNote Notebook Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(218.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(218.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xCC064E3B),
                                                Color(0xE60F172A)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.Today,
                                                contentDescription = null,
                                                tint = Color(0xFF99F6E4),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(5.dp))
                                            Text(
                                                text = "$weekday • $monthYearText",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    Text(
                                        text = liveTimeText,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFFFDE68A)
                                    )
                                }

                                Text(
                                    text = gregorianDate,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = banglaDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFCCFBF1)
                                )

                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = onQuickAddSalary,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("dashboard_quick_add_salary")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (isBn) "দ্রুত নতুন বেতন হিসাব যোগ করুন" else "Quick Add Salary Record",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1B. Optional Personal Profile Summary Card
            if (settings.showProfileOnDashboard) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenProfileSheet)
                            .testTag("dashboard_profile_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProfileAvatarView(
                                photoPath = profile.profilePhotoPath,
                                fullName = profile.fullName,
                                size = 50.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.fullName.ifBlank {
                                        if (isBn) "আপনার ব্যক্তিগত প্রোফাইল সেট করুন" else "Set Up Your Personal Profile"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val subtitleParts = buildList {
                                    if (profile.profession.isNotBlank()) add(profile.profession)
                                    if (settings.showPhoneInProfileCard && profile.mobileNumber.isNotBlank()) add(profile.mobileNumber)
                                    if (settings.showEmailInProfileCard && profile.email.isNotBlank()) add(profile.email)
                                }
                                Text(
                                    text = if (subtitleParts.isNotEmpty()) {
                                        subtitleParts.joinToString(" • ")
                                    } else {
                                        if (isBn) "নাম, পেশা, ছবি ও পরিচয় যুক্ত করতে ট্যাপ করুন" else "Tap to add photo, profession & personal details"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Summary Metric Grid: Current Year Total Salary & Total Saved Records
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        title = if (isBn) "চলতি বছরের মোট বেতন" else "This Year Total",
                        value = LocalizationHelper.formatCurrency(
                            currentYearTotal, useBnDigits, currency = settings.currency
                        ),
                        subtitle = LocalizationHelper.formatInt(clock.year, useBnDigits),
                        icon = Icons.Filled.AccountBalanceWallet,
                        accent = AccentEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppDestination.YEARLY_REPORT) }
                    )
                    DashboardStatCard(
                        title = if (isBn) "মোট সংরক্ষিত রেকর্ড" else "Total Saved Records",
                        value = LocalizationHelper.formatInt(allSalaries.size, useBnDigits),
                        subtitle = if (isBn) "১০+ বছরের খাতা" else "10+ Year Ledger",
                        icon = Icons.Filled.ReceiptLong,
                        accent = AccentPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppDestination.SALARY_LEDGER) }
                    )
                }
            }

            // 3. Latest Salary Card (Notebook Style)
            item {
                NotebookPageCard(
                    accentColor = latestSalary?.methodEnum?.accentColor ?: AccentOrange,
                    modifier = Modifier.clickable { onNavigate(AppDestination.SALARY_LEDGER) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "সর্বশেষ বেতন গ্রহণের তথ্য" else "Latest Salary Received",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            if (latestSalary != null) {
                                Text(
                                    text = LocalizationHelper.formatCurrency(
                                        latestSalary.amount, useBnDigits, currency = settings.currency
                                    ),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val dateStr = LocalizationHelper.formatDate(
                                    latestSalary.year, latestSalary.month, latestSalary.day,
                                    isBn, useBnDigits, style = settings.dateFormat
                                )
                                val dayStr = LocalizationHelper.weekdayName(latestSalary.dayOfWeek, isBn)
                                Text(
                                    text = "$dateStr ($dayStr) • ${latestSalary.methodEnum.label(isBn)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (latestSalary.comment.isNotBlank()) {
                                    Text(
                                        text = latestSalary.comment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Text(
                                    text = if (isBn) "এখনো কোনো বেতন রেকর্ড নেই" else "No salary records yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isBn) "উপরের বাটনে ট্যাপ করে প্রথম বেতন যোগ করুন।" else "Tap Quick Add above to record your first salary.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconBadge3D(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            tint = AccentOrange,
                            contentDescription = null,
                            size = 50.dp
                        )
                    }
                }
            }

            // 4. Next Prayer Time Card with Live Countdown
            item {
                NotebookPageCard(
                    accentColor = AccentSkyBlue,
                    showRuledLines = false,
                    modifier = Modifier.clickable { onNavigate(AppDestination.PRAYER_TIMES) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "নামাজের পরবর্তী সময় (${settings.displayLocationName()})" else "Next Prayer (${settings.displayLocationName()})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = nextPrayer.slot.label(isBn),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentSkyBlue
                                )
                                Text(
                                    text = nextPrayerTime,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = if (isBn) "বাকি সময়: $countdown" else "Starts in: $countdown",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconBadge3D(
                            icon = Icons.Filled.Mosque,
                            tint = AccentSkyBlue,
                            contentDescription = null,
                            size = 50.dp
                        )
                    }
                }
            }

            // 5. All Features Quick-Access Tiles
            item {
                Text(
                    text = if (isBn) "সকল বিভাগসমূহ" else "All Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickModuleTile(
                            title = AppDestination.MONTHLY_LEDGER.title(isBn),
                            subtitle = if (isBn) "জানুয়ারি - ডিসেম্বর" else "Jan - Dec",
                            icon = Icons.Filled.CalendarMonth,
                            accent = AccentPurple,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppDestination.MONTHLY_LEDGER) }
                        )
                        QuickModuleTile(
                            title = AppDestination.YEARLY_REPORT.title(isBn),
                            subtitle = if (isBn) "১০ বছরের রিপোর্ট" else "10-Year Report",
                            icon = Icons.Filled.Assessment,
                            accent = AccentOrange,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppDestination.YEARLY_REPORT) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickModuleTile(
                            title = AppDestination.PERSONAL_NOTES.title(isBn),
                            subtitle = "${LocalizationHelper.formatInt(notesCount, useBnDigits)} ${if (isBn) "টি নোট" else "notes"}",
                            icon = Icons.Filled.EditNote,
                            accent = AccentEmerald,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppDestination.PERSONAL_NOTES) }
                        )
                        QuickModuleTile(
                            title = AppDestination.REMINDERS.title(isBn),
                            subtitle = "${LocalizationHelper.formatInt(upcomingRemindersCount, useBnDigits)} ${if (isBn) "টি সক্রিয়" else "active"}",
                            icon = Icons.Filled.NotificationsActive,
                            accent = AccentAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppDestination.REMINDERS) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickModuleTile(
                            title = AppDestination.QIBLA.title(isBn),
                            subtitle = if (isBn) "৩৬০° কম্পাস" else "360° Compass",
                            icon = Icons.Filled.Explore,
                            accent = AccentTeal,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppDestination.QIBLA) }
                        )
                        QuickModuleTile(
                            title = AppDestination.CALCULATOR.title(isBn),
                            subtitle = if (isBn) "মেমোরি ও ইতিহাস" else "Memory & History",
                            icon = Icons.Filled.Calculate,
                            accent = AccentSoftPink,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppDestination.CALCULATOR) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickModuleTile(
                            title = AppDestination.BACKUP.title(isBn),
                            subtitle = "JSON • CSV • PDF",
                            icon = Icons.Filled.Backup,
                            accent = AccentIndigo,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppDestination.BACKUP) }
                        )
                        QuickModuleTile(
                            title = AppDestination.PROFILE.title(isBn),
                            subtitle = if (isBn) "পরিচয় ও ছবি" else "Identity & Photo",
                            icon = Icons.Filled.AccountCircle,
                            accent = AccentPurple,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppDestination.PROFILE) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickModuleTile(
                            title = AppDestination.SETTINGS.title(isBn),
                            subtitle = if (isBn) "কাস্টমাইজ ও নিরাপত্তা" else "Customize & Lock",
                            icon = Icons.Filled.Settings,
                            accent = AccentSkyBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppDestination.SETTINGS) }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge3D(icon = icon, tint = accent, contentDescription = null, size = 38.dp)
                Surface(
                    color = accent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        maxLines = 1
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickModuleTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconBadge3D(icon = icon, tint = accent, contentDescription = null, size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
