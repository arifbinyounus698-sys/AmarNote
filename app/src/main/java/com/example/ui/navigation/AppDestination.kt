package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val bnTitle: String,
    val enTitle: String,
    val bnShortTitle: String,
    val enShortTitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD(
        route = "dashboard",
        bnTitle = "ড্যাশবোর্ড",
        enTitle = "Dashboard",
        bnShortTitle = "হোম",
        enShortTitle = "Home",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    ),
    SALARY_LEDGER(
        route = "salary_ledger",
        bnTitle = "বেতন খাতা",
        enTitle = "Salary Ledger",
        bnShortTitle = "বেতন খাতা",
        enShortTitle = "Ledger",
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook
    ),
    MONTHLY_LEDGER(
        route = "monthly_ledger",
        bnTitle = "১২ মাস",
        enTitle = "12 Months",
        bnShortTitle = "১২ মাস",
        enShortTitle = "Months",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ),
    YEARLY_REPORT(
        route = "yearly_report",
        bnTitle = "বার্ষিক হিসাব",
        enTitle = "Yearly Report",
        bnShortTitle = "বার্ষিক",
        enShortTitle = "Yearly",
        selectedIcon = Icons.Filled.Assessment,
        unselectedIcon = Icons.Outlined.Assessment
    ),
    PERSONAL_NOTES(
        route = "personal_notes",
        bnTitle = "ব্যক্তিগত নোটবুক",
        enTitle = "Personal Notes",
        bnShortTitle = "নোট",
        enShortTitle = "Notes",
        selectedIcon = Icons.Filled.EditNote,
        unselectedIcon = Icons.Outlined.EditNote
    ),
    REMINDERS(
        route = "reminders",
        bnTitle = "রিমাইন্ডার",
        enTitle = "Reminders",
        bnShortTitle = "স্মরণিকা",
        enShortTitle = "Reminders",
        selectedIcon = Icons.Filled.NotificationsActive,
        unselectedIcon = Icons.Outlined.NotificationsActive
    ),
    PROFILE(
        route = "profile",
        bnTitle = "ব্যক্তিগত প্রোফাইল",
        enTitle = "Personal Profile",
        bnShortTitle = "প্রোফাইল",
        enShortTitle = "Profile",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    ),
    PRAYER_TIMES(
        route = "prayer_times",
        bnTitle = "নামাজের সময়",
        enTitle = "Prayer Times",
        bnShortTitle = "নামাজ",
        enShortTitle = "Prayer",
        selectedIcon = Icons.Filled.Mosque,
        unselectedIcon = Icons.Outlined.Mosque
    ),
    QIBLA(
        route = "qibla",
        bnTitle = "কিবলা কম্পাস",
        enTitle = "Qibla Compass",
        bnShortTitle = "কিবলা",
        enShortTitle = "Qibla",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore
    ),
    CALCULATOR(
        route = "calculator",
        bnTitle = "ক্যালকুলেটর",
        enTitle = "Calculator",
        bnShortTitle = "হিসাব",
        enShortTitle = "Calc",
        selectedIcon = Icons.Filled.Calculate,
        unselectedIcon = Icons.Outlined.Calculate
    ),
    BACKUP(
        route = "backup",
        bnTitle = "ব্যাকআপ ও এক্সপোর্ট",
        enTitle = "Backup & Export",
        bnShortTitle = "ব্যাকআপ",
        enShortTitle = "Backup",
        selectedIcon = Icons.Filled.Backup,
        unselectedIcon = Icons.Outlined.Backup
    ),
    SETTINGS(
        route = "settings",
        bnTitle = "সেটিংস ও নিরাপত্তা",
        enTitle = "Settings & Security",
        bnShortTitle = "সেটিংস",
        enShortTitle = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    );

    fun title(isBangla: Boolean): String = if (isBangla) bnTitle else enTitle
    fun shortTitle(isBangla: Boolean): String = if (isBangla) bnShortTitle else enShortTitle

    companion object {
        val primaryBottomTabs = listOf(DASHBOARD, SALARY_LEDGER, MONTHLY_LEDGER, PRAYER_TIMES, QIBLA)
    }
}
