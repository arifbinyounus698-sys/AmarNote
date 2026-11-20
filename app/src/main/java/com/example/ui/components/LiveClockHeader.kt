package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.AppSettings
import com.example.data.local.ClockFormat
import com.example.data.local.UserProfile
import com.example.domain.LocalizationHelper
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.LiveClockState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmarNoteTopBarAndClock(
    sectionTitle: String,
    clock: LiveClockState,
    settings: AppSettings,
    profile: UserProfile,
    showDrawerButton: Boolean,
    onOpenDrawer: () -> Unit,
    onQuickAddSalary: () -> Unit,
    onOpenProfileSheet: () -> Unit
) {
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits
    val is12Hour = settings.clockFormat == ClockFormat.HOUR_12

    val liveTimeText = LocalizationHelper.formatTime(
        hour24 = clock.hour24,
        minute = clock.minute,
        second = clock.second,
        is12Hour = is12Hour,
        isBangla = isBn,
        useBanglaDigits = useBnDigits
    )
    val gregorianDate = LocalizationHelper.formatDate(
        year = clock.year,
        month = clock.month,
        day = clock.day,
        isBangla = isBn,
        useBanglaDigits = useBnDigits,
        style = settings.dateFormat
    )
    val banglaDate = LocalizationHelper.formatBanglaCalendarDate(
        year = clock.year,
        month1To12 = clock.month,
        day = clock.day,
        isBangla = isBn,
        useBanglaDigits = useBnDigits
    )
    val weekday = LocalizationHelper.weekdayName(clock.dayOfWeek, isBn)

    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            navigationIcon = {
                if (showDrawerButton) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("drawer_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = if (isBn) "মেনু খুলুন" else "Open menu"
                        )
                    }
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = "AmarNote Logo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AmarNote",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            actions = {
                FilledTonalIconButton(
                    onClick = onQuickAddSalary,
                    modifier = Modifier.testTag("topbar_quick_add")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = if (isBn) "নতুন বেতন যোগ করুন" else "Add salary"
                    )
                }
                IconButton(
                    onClick = onOpenProfileSheet,
                    modifier = Modifier.testTag("topbar_profile_button")
                ) {
                    ProfileAvatarView(
                        photoPath = profile.profilePhotoPath,
                        fullName = profile.fullName,
                        size = 34.dp
                    )
                }
            }
        )

        // Persistent Live Clock & Calendar Strip above navigation/content
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(7.dp))
                    Column {
                        Text(
                            text = "$weekday, $gregorianDate",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = banglaDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = liveTimeText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
