package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.DefaultDashboardOption
import com.example.data.local.SalaryRecord
import com.example.data.local.ThemeMode
import com.example.ui.components.AmarNoteTopBarAndClock
import com.example.ui.components.LockScreenOverlay
import com.example.ui.components.ProfileQuickSheetDialog
import com.example.ui.components.SalaryEditorDialog
import com.example.ui.navigation.AppDestination
import com.example.ui.screens.BackupScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MonthlyLedgerScreen
import com.example.ui.screens.PersonalNotesScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SalaryLedgerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.YearlyReportScreen
import com.example.ui.theme.AmarNoteTheme
import com.example.ui.viewmodel.AmarNoteViewModel
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AmarNoteViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            AmarNoteTheme(darkTheme = darkTheme) {
                AmarNoteApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AmarNoteApp(viewModel: AmarNoteViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppDestination.DASHBOARD.route
    val currentDest = AppDestination.entries.firstOrNull { it.route == currentRoute } ?: AppDestination.DASHBOARD

    val clock by viewModel.clockState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allSalaries by viewModel.allSalaries.collectAsStateWithLifecycle()
    val filteredSalaries by viewModel.filteredSalaries.collectAsStateWithLifecycle()
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val noteSearchQuery by viewModel.noteSearchQuery.collectAsStateWithLifecycle()
    val noteCategoryFilter by viewModel.noteCategoryFilter.collectAsStateWithLifecycle()
    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()

    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterYear by viewModel.filterYear.collectAsStateWithLifecycle()
    val filterMethod by viewModel.filterMethod.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val monthSummary by viewModel.selectedMonthSummary.collectAsStateWithLifecycle()
    val yearlyReport by viewModel.yearlyReport.collectAsStateWithLifecycle()
    val dailyPrayerTimes by viewModel.dailyPrayerTimes.collectAsStateWithLifecycle()
    val nextPrayerInfo by viewModel.nextPrayerInfo.collectAsStateWithLifecycle()
    val prayerCalYear by viewModel.prayerCalendarYear.collectAsStateWithLifecycle()
    val prayerCalMonth by viewModel.prayerCalendarMonth.collectAsStateWithLifecycle()
    val monthlyPrayerCalendar by viewModel.monthlyPrayerCalendar.collectAsStateWithLifecycle()
    val qiblaData by viewModel.qiblaData.collectAsStateWithLifecycle()
    val calculatorState by viewModel.calculatorState.collectAsStateWithLifecycle()
    val calculatorHistory by viewModel.calculatorHistory.collectAsStateWithLifecycle()
    val prefillAmount by viewModel.prefillSalaryAmount.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showSalaryEditor by remember { mutableStateOf(false) }
    var editorRecord by remember { mutableStateOf<SalaryRecord?>(null) }
    var editorDuplicateMode by remember { mutableStateOf(false) }
    var showProfileQuickSheet by remember { mutableStateOf(false) }
    var hasAppliedInitialRoute by rememberSaveable { mutableStateOf(false) }

    val isBn = settings.isBangla

    // Observe Activity lifecycle for Auto-Lock timer
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> viewModel.onAppBackgrounded()
                Lifecycle.Event.ON_START -> viewModel.onAppResumed()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Apply user's preferred startup screen once on launch
    LaunchedEffect(settings.defaultDashboard) {
        if (!hasAppliedInitialRoute &&
            currentRoute == AppDestination.DASHBOARD.route &&
            settings.defaultDashboard != DefaultDashboardOption.DASHBOARD
        ) {
            hasAppliedInitialRoute = true
            navController.navigate(settings.defaultDashboard.route) {
                popUpTo(AppDestination.DASHBOARD.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    fun navigateTo(dest: AppDestination) {
        hasAppliedInitialRoute = true
        if (currentRoute != dest.route) {
            navController.navigate(dest.route) {
                popUpTo(AppDestination.DASHBOARD.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun openNewSalaryEditor(prefill: Double? = null) {
        editorRecord = null
        editorDuplicateMode = false
        viewModel.setPrefillSalaryAmount(prefill)
        showSalaryEditor = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 840.dp

            val drawerContent: @Composable () -> Unit = {
                ModalDrawerSheet {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "AmarNote",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                        Text(
                            text = if (isBn) "ব্যক্তিগত ডিজিটাল বেতন ও হিসাবের খাতা" else "Personal Digital Salary & Utility Ledger",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        AppDestination.entries.forEach { dest ->
                            val selected = currentDest == dest
                            NavigationDrawerItem(
                                selected = selected,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navigateTo(dest)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(dest.title(isBn)) },
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                                    .testTag("drawer_item_${dest.route}")
                            )
                        }
                    }
                }
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = !isExpandedScreen && !isAppLocked,
                drawerContent = drawerContent
            ) {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        AmarNoteTopBarAndClock(
                            sectionTitle = currentDest.title(isBn),
                            clock = clock,
                            settings = settings,
                            profile = userProfile,
                            showDrawerButton = !isExpandedScreen,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onQuickAddSalary = { openNewSalaryEditor() },
                            onOpenProfileSheet = { showProfileQuickSheet = true }
                        )
                    },
                    bottomBar = {
                        if (!isExpandedScreen) {
                            NavigationBar {
                                AppDestination.primaryBottomTabs.forEach { dest ->
                                    val selected = currentDest == dest
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTo(dest) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = dest.title(isBn)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = dest.shortTitle(isBn),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag("bottom_nav_${dest.route}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isExpandedScreen) {
                            NavigationRail(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                AppDestination.entries.forEach { dest ->
                                    val selected = currentDest == dest
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateTo(dest) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = dest.title(isBn)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = dest.shortTitle(isBn),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            NavHost(
                                navController = navController,
                                startDestination = AppDestination.DASHBOARD.route
                            ) {
                                composable(AppDestination.DASHBOARD.route) {
                                    DashboardScreen(
                                        clock = clock,
                                        settings = settings,
                                        profile = userProfile,
                                        allSalaries = allSalaries,
                                        notesCount = allNotes.size,
                                        upcomingRemindersCount = allReminders.count { !it.isCompleted },
                                        nextPrayer = nextPrayerInfo,
                                        onQuickAddSalary = { openNewSalaryEditor() },
                                        onOpenProfileSheet = { showProfileQuickSheet = true },
                                        onNavigate = { navigateTo(it) }
                                    )
                                }
                                composable(AppDestination.SALARY_LEDGER.route) {
                                    SalaryLedgerScreen(
                                        records = filteredSalaries,
                                        allYears = availableYears,
                                        searchQuery = searchQuery,
                                        filterYear = filterYear,
                                        filterMethod = filterMethod,
                                        settings = settings,
                                        onSearchChange = viewModel::setSearchQuery,
                                        onYearFilterChange = viewModel::setFilterYear,
                                        onMethodFilterChange = viewModel::setFilterMethod,
                                        onAddNew = { openNewSalaryEditor() },
                                        onEdit = { rec ->
                                            editorRecord = rec
                                            editorDuplicateMode = false
                                            viewModel.setPrefillSalaryAmount(null)
                                            showSalaryEditor = true
                                        },
                                        onDuplicate = { rec ->
                                            editorRecord = rec
                                            editorDuplicateMode = true
                                            viewModel.setPrefillSalaryAmount(null)
                                            showSalaryEditor = true
                                        },
                                        onDeleteConfirmed = viewModel::deleteSalaryRecord
                                    )
                                }
                                composable(AppDestination.MONTHLY_LEDGER.route) {
                                    MonthlyLedgerScreen(
                                        selectedYear = selectedYear,
                                        selectedMonth = selectedMonth,
                                        availableYears = availableYears,
                                        monthSummary = monthSummary,
                                        allSalaries = allSalaries,
                                        settings = settings,
                                        onSelectYear = viewModel::setSelectedYear,
                                        onSelectMonth = viewModel::setSelectedMonth,
                                        onEditRecord = { rec ->
                                            editorRecord = rec
                                            editorDuplicateMode = false
                                            showSalaryEditor = true
                                        },
                                        onMessage = viewModel::showStatusMessage
                                    )
                                }
                                composable(AppDestination.YEARLY_REPORT.route) {
                                    YearlyReportScreen(
                                        selectedYear = selectedYear,
                                        availableYears = availableYears,
                                        report = yearlyReport,
                                        allSalaries = allSalaries,
                                        settings = settings,
                                        onSelectYear = viewModel::setSelectedYear,
                                        onMessage = viewModel::showStatusMessage
                                    )
                                }
                                composable(AppDestination.PERSONAL_NOTES.route) {
                                    PersonalNotesScreen(
                                        notes = filteredNotes,
                                        searchQuery = noteSearchQuery,
                                        categoryFilter = noteCategoryFilter,
                                        settings = settings,
                                        clock = clock,
                                        onSearchChange = viewModel::setNoteSearchQuery,
                                        onCategoryFilterChange = viewModel::setNoteCategoryFilter,
                                        onSaveNote = viewModel::savePersonalNote,
                                        onTogglePin = viewModel::togglePinNote,
                                        onDeleteNote = viewModel::deletePersonalNote
                                    )
                                }
                                composable(AppDestination.REMINDERS.route) {
                                    RemindersScreen(
                                        reminders = allReminders,
                                        settings = settings,
                                        clock = clock,
                                        onSaveReminder = viewModel::saveReminder,
                                        onToggleComplete = viewModel::toggleReminderCompleted,
                                        onDeleteReminder = viewModel::deleteReminder,
                                        onMessage = viewModel::showStatusMessage
                                    )
                                }
                                composable(AppDestination.PROFILE.route) {
                                    ProfileScreen(
                                        profile = userProfile,
                                        settings = settings,
                                        onSaveProfile = viewModel::saveProfile,
                                        onUpdateAvatarPath = viewModel::updateProfilePhotoPath,
                                        onUpdateCoverPath = viewModel::updateCoverPhotoPath,
                                        onDeleteAvatar = viewModel::deleteProfilePhoto,
                                        onDeleteCover = viewModel::deleteCoverPhoto,
                                        onResetProfile = viewModel::resetProfileInformation,
                                        onMessage = viewModel::showStatusMessage
                                    )
                                }
                                composable(AppDestination.PRAYER_TIMES.route) {
                                    PrayerTimesScreen(
                                        clock = clock,
                                        settings = settings,
                                        dailyTimes = dailyPrayerTimes,
                                        nextPrayer = nextPrayerInfo,
                                        calendarYear = prayerCalYear,
                                        calendarMonth = prayerCalMonth,
                                        monthlyCalendar = monthlyPrayerCalendar,
                                        onChangeCalendarMonthYear = viewModel::setPrayerCalendarMonthYear,
                                        onSelectLocation = viewModel::setLocation,
                                        onSelectPrayerMethod = viewModel::setPrayerMethod,
                                        onSelectAsrMethod = viewModel::setAsrMethod,
                                        onMessage = viewModel::showStatusMessage
                                    )
                                }
                                composable(AppDestination.QIBLA.route) {
                                    QiblaScreen(
                                        settings = settings,
                                        qiblaData = qiblaData,
                                        onSelectLocation = viewModel::setLocation,
                                        onMessage = viewModel::showStatusMessage
                                    )
                                }
                                composable(AppDestination.CALCULATOR.route) {
                                    CalculatorScreen(
                                        state = calculatorState,
                                        history = calculatorHistory,
                                        settings = settings,
                                        onDigit = viewModel::onCalcDigit,
                                        onOperator = viewModel::onCalcOperator,
                                        onEquals = viewModel::onCalcEquals,
                                        onClear = viewModel::onCalcClear,
                                        onBackspace = viewModel::onCalcBackspace,
                                        onPercent = viewModel::onCalcPercent,
                                        onToggleSign = viewModel::onCalcToggleSign,
                                        onMemoryAction = viewModel::onCalcMemoryAction,
                                        onUseHistoryResult = viewModel::applyHistoryResult,
                                        onClearHistory = viewModel::clearCalculatorHistory,
                                        onAddResultToSalary = { amt -> openNewSalaryEditor(prefill = amt) }
                                    )
                                }
                                composable(AppDestination.BACKUP.route) {
                                    BackupScreen(
                                        allSalaries = allSalaries,
                                        currentYear = clock.year,
                                        currentMonth = clock.month,
                                        settings = settings,
                                        generateJsonBackup = viewModel::generateJsonBackupString,
                                        generateCsv = { viewModel.generateCsvString() },
                                        onRestoreJson = viewModel::restoreFromJson,
                                        onMessage = viewModel::showStatusMessage
                                    )
                                }
                                composable(AppDestination.SETTINGS.route) {
                                    SettingsScreen(
                                        settings = settings,
                                        profile = userProfile,
                                        onSelectLanguage = viewModel::setLanguage,
                                        onSelectTheme = viewModel::setThemeMode,
                                        onSelectNumerals = viewModel::setNumeralStyle,
                                        onSelectClockFormat = viewModel::setClockFormat,
                                        onSelectCurrency = viewModel::setCurrency,
                                        onSelectDateFormat = viewModel::setDateFormat,
                                        onSelectDefaultDashboard = viewModel::setDefaultDashboard,
                                        onUpdateProfileDisplayPrefs = viewModel::setProfileDisplayPrefs,
                                        onSetOrChangePin = viewModel::setOrChangePin,
                                        onDisablePin = viewModel::disableAppPin,
                                        onSetBiometricEnabled = viewModel::setBiometricEnabled,
                                        onSetAutoLockMinutes = viewModel::setAutoLockMinutes,
                                        onLockNow = viewModel::lockNow,
                                        onSelectPrayerMethod = viewModel::setPrayerMethod,
                                        onSelectAsrMethod = viewModel::setAsrMethod,
                                        onSelectLocation = viewModel::setLocation,
                                        onOpenBackupScreen = { navigateTo(AppDestination.BACKUP) },
                                        onOpenProfileScreen = { navigateTo(AppDestination.PROFILE) },
                                        onDeletePersonalInfo = viewModel::resetProfileInformation,
                                        onMessage = viewModel::showStatusMessage
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showSalaryEditor) {
            SalaryEditorDialog(
                initialRecord = editorRecord,
                isDuplicateMode = editorDuplicateMode,
                prefillAmount = prefillAmount,
                settings = settings,
                clock = clock,
                onDismiss = {
                    showSalaryEditor = false
                    viewModel.setPrefillSalaryAmount(null)
                },
                onSave = { record ->
                    viewModel.saveSalaryRecord(record, isDuplicate = editorDuplicateMode)
                    showSalaryEditor = false
                    viewModel.setPrefillSalaryAmount(null)
                }
            )
        }

        if (showProfileQuickSheet) {
            ProfileQuickSheetDialog(
                profile = userProfile,
                settings = settings,
                onDismiss = { showProfileQuickSheet = false },
                onEditProfile = { navigateTo(AppDestination.PROFILE) },
                onOpenAccountSettings = { navigateTo(AppDestination.SETTINGS) }
            )
        }

        if (isAppLocked) {
            LockScreenOverlay(
                settings = settings,
                onVerifyPin = viewModel::verifyAndUnlockPin,
                onBiometricUnlocked = viewModel::unlockWithBiometric
            )
        }
    }
}
