package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AmarNoteDatabase
import com.example.data.local.AppLanguage
import com.example.data.local.AppReminder
import com.example.data.local.AppSettings
import com.example.data.local.BackupManager
import com.example.data.local.CurrencyOption
import com.example.data.local.DateFormatStyle
import com.example.data.local.DefaultDashboardOption
import com.example.data.local.NoteCategory
import com.example.data.local.PaymentMethod
import com.example.data.local.PersonalNote
import com.example.data.local.ReminderCategory
import com.example.data.local.SalaryRecord
import com.example.data.local.SecurityManager
import com.example.data.local.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AmarNoteRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AmarNoteDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AmarNoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `json backup and restore roundtrip preserves 10-year records profile notes reminders and settings`() {
        val records = listOf(
            SalaryRecord(
                id = 1,
                amount = 45000.0,
                year = 2026,
                month = 11,
                day = 20,
                hour = 10,
                minute = 15,
                second = 30,
                dayOfWeek = Calendar.FRIDAY,
                paymentMethod = PaymentMethod.BKASH.name,
                receiverNumber = "01711000000",
                transactionId = "TRX998877",
                comment = "নভেম্বর বেতন"
            ),
            SalaryRecord(
                id = 2,
                amount = 65000.0,
                year = 2035,
                month = 5,
                day = 1,
                hour = 14,
                minute = 0,
                second = 5,
                dayOfWeek = Calendar.TUESDAY,
                paymentMethod = PaymentMethod.BANK.name,
                bankName = "Islami Bank",
                accountLastDigits = "4321"
            )
        )
        val settings = AppSettings(
            language = AppLanguage.BANGLA,
            currency = CurrencyOption.USD,
            dateFormat = DateFormatStyle.YYYY_MM_DD,
            defaultDashboard = DefaultDashboardOption.SALARY_LEDGER
        )
        val profile = UserProfile(
            fullName = "আবদুল্লাহ আল মামুন",
            mobileNumber = "01700000000",
            profession = "শিক্ষক",
            organizationName = "ঢাকা কলেজ",
            cityAndCountry = "ঢাকা, বাংলাদেশ"
        )
        val notes = listOf(
            PersonalNote(
                title = "জরুরি পরিকল্পনা",
                content = "আগামী মাসের বাজেট তৈরি",
                category = NoteCategory.FUTURE_PLAN.name,
                year = 2026,
                month = 11,
                day = 20,
                isPinned = true
            )
        )
        val reminders = listOf(
            AppReminder(
                title = "বেতন দিন",
                category = ReminderCategory.SALARY_DATE.name,
                year = 2026,
                month = 12,
                day = 1,
                hour = 9,
                minute = 0,
                triggerAtMillis = 1_800_000_000_000L
            )
        )

        val json = BackupManager.buildJsonBackup(
            records = records,
            settings = settings,
            profile = profile,
            profilePhotoBase64 = "ZmFrZV9hdmF0YXJfYnl0ZXM=",
            personalNotes = notes,
            reminders = reminders
        )
        val parsed = BackupManager.parseJsonBackup(json)

        assertEquals(2, parsed.records.size)
        assertEquals(45000.0, parsed.records[0].amount, 0.001)
        assertEquals(2035, parsed.records[1].year)
        assertEquals("Islami Bank", parsed.records[1].bankName)
        assertEquals(AppLanguage.BANGLA, parsed.settings?.language)
        assertEquals(CurrencyOption.USD, parsed.settings?.currency)
        assertEquals(DateFormatStyle.YYYY_MM_DD, parsed.settings?.dateFormat)
        assertEquals(DefaultDashboardOption.SALARY_LEDGER, parsed.settings?.defaultDashboard)
        assertEquals("আবদুল্লাহ আল মামুন", parsed.profile?.fullName)
        assertEquals("শিক্ষক", parsed.profile?.profession)
        assertEquals("ZmFrZV9hdmF0YXJfYnl0ZXM=", parsed.profilePhotoBase64)
        assertEquals(1, parsed.personalNotes.size)
        assertTrue(parsed.personalNotes[0].isPinned)
        assertEquals(1, parsed.reminders.size)
        assertEquals("বেতন দিন", parsed.reminders[0].title)
    }

    @Test
    fun `pin hashing never stores plaintext and verifies correctly`() {
        val hashed = SecurityManager.hashPin("4829")
        assertNotEquals("4829", hashed.hashBase64)
        assertTrue(hashed.saltBase64.isNotBlank())
        assertTrue(SecurityManager.verifyPin("4829", hashed.saltBase64, hashed.hashBase64))
        assertFalse(SecurityManager.verifyPin("0000", hashed.saltBase64, hashed.hashBase64))
        assertFalse(SecurityManager.verifyPin("4829", "", hashed.hashBase64))
    }

    @Test
    fun `aes256 backup encryption and decryption roundtrip and wrong passphrase failure`() {
        val sampleJson = BackupManager.buildJsonBackup(
            records = listOf(
                SalaryRecord(
                    amount = 30000.0,
                    year = 2026,
                    month = 11,
                    day = 20,
                    hour = 12,
                    minute = 0,
                    second = 0,
                    dayOfWeek = Calendar.FRIDAY,
                    paymentMethod = PaymentMethod.CASH.name
                )
            ),
            settings = AppSettings()
        )
        val encrypted = SecurityManager.encryptBackupJson(sampleJson, "secret123")
        assertTrue(SecurityManager.isEncryptedBackup(encrypted))

        val decrypted = SecurityManager.decryptBackupJson(encrypted, "secret123")
        val parsed = BackupManager.parseJsonBackup(decrypted)
        assertEquals(1, parsed.records.size)
        assertEquals(30000.0, parsed.records[0].amount, 0.001)

        var failed = false
        try {
            SecurityManager.decryptBackupJson(encrypted, "wrong_pass")
        } catch (_: Exception) {
            failed = true
        }
        assertTrue(failed)
    }

    @Test
    fun `room database persists and retrieves salary records profile notes and reminders`() = runTest {
        val dao = db.salaryDao()
        dao.insert(
            SalaryRecord(
                amount = 52000.0,
                year = 2026,
                month = 11,
                day = 20,
                hour = 9,
                minute = 21,
                second = 15,
                dayOfWeek = SalaryRecord.computeDayOfWeek(2026, 11, 20),
                paymentMethod = PaymentMethod.NAGAD.name,
                receiverNumber = "01812345678",
                comment = "বেতন"
            )
        )
        dao.insert(
            SalaryRecord(
                amount = 75000.0,
                year = 2034,
                month = 6,
                day = 10,
                hour = 16,
                minute = 0,
                second = 0,
                dayOfWeek = SalaryRecord.computeDayOfWeek(2034, 6, 10),
                paymentMethod = PaymentMethod.BANK.name,
                bankName = "Sonali Bank"
            )
        )

        val all = dao.getAllRecords().first()
        assertEquals(2, all.size)
        assertEquals(2034, all[0].year)
        assertEquals(2026, all[1].year)
        assertEquals(Calendar.FRIDAY, all[1].dayOfWeek)

        db.profileDao().upsertProfile(
            UserProfile(fullName = "রহিম উদ্দিন", profession = "চাকরিজীবী", mobileNumber = "01911111111")
        )
        val savedProfile = db.profileDao().getProfileOnce()
        assertEquals("রহিম উদ্দিন", savedProfile?.fullName)

        db.personalNoteDao().insert(
            PersonalNote(
                title = "স্মরণিকা",
                content = "ব্যক্তিগত তথ্য",
                year = 2026,
                month = 11,
                day = 20,
                isPinned = true
            )
        )
        assertEquals(1, db.personalNoteDao().getAllNotes().first().size)

        db.reminderDao().insert(
            AppReminder(
                title = "বিল পরিশোধ",
                year = 2026,
                month = 12,
                day = 5,
                hour = 10,
                minute = 30,
                triggerAtMillis = 1_800_000_000_000L
            )
        )
        assertEquals(1, db.reminderDao().getAllReminders().first().size)
    }

    @Test
    fun `migration 1 to 2 preserves existing v1 salary records`() = runTest {
        val dbName = "migration_test_amarnote.db"
        context.deleteDatabase(dbName)

        // Create a v1 SQLite database with the original v1 schema and seed 1 salary record
        val v1Helper = object : SQLiteOpenHelper(context, dbName, null, 1) {
            override fun onCreate(sqDb: SQLiteDatabase) {
                sqDb.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `salary_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `amount` REAL NOT NULL,
                        `year` INTEGER NOT NULL,
                        `month` INTEGER NOT NULL,
                        `day` INTEGER NOT NULL,
                        `hour` INTEGER NOT NULL,
                        `minute` INTEGER NOT NULL,
                        `second` INTEGER NOT NULL,
                        `dayOfWeek` INTEGER NOT NULL,
                        `paymentMethod` TEXT NOT NULL,
                        `receiverNumber` TEXT NOT NULL,
                        `senderNumber` TEXT NOT NULL,
                        `bankName` TEXT NOT NULL,
                        `accountLastDigits` TEXT NOT NULL,
                        `transactionId` TEXT NOT NULL,
                        `comment` TEXT NOT NULL,
                        `extraNote` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        `updatedAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                sqDb.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `calculator_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `expression` TEXT NOT NULL,
                        `result` TEXT NOT NULL,
                        `timestampMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                sqDb.execSQL(
                    """
                    INSERT INTO `salary_records` (
                        `amount`, `year`, `month`, `day`, `hour`, `minute`, `second`, `dayOfWeek`,
                        `paymentMethod`, `receiverNumber`, `senderNumber`, `bankName`,
                        `accountLastDigits`, `transactionId`, `comment`, `extraNote`,
                        `createdAtMillis`, `updatedAtMillis`
                    ) VALUES (
                        42000.0, 2026, 10, 1, 9, 0, 0, 5,
                        'BKASH', '01700000001', '', '', '', 'TX123', 'Old V1 Salary', '',
                        1000, 1000
                    )
                    """.trimIndent()
                )
            }

            override fun onUpgrade(sqDb: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        v1Helper.writableDatabase.close()
        v1Helper.close()

        val migratedDb = Room.databaseBuilder(context, AmarNoteDatabase::class.java, dbName)
            .addMigrations(AmarNoteDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        try {
            val records = migratedDb.salaryDao().getAllRecordsOnce()
            assertEquals(1, records.size)
            assertEquals(42000.0, records[0].amount, 0.001)
            assertEquals("Old V1 Salary", records[0].comment)

            // Verify new v2 tables work seamlessly on the migrated database
            migratedDb.profileDao().upsertProfile(UserProfile(fullName = "Migrated User"))
            assertEquals("Migrated User", migratedDb.profileDao().getProfileOnce()?.fullName)
        } finally {
            migratedDb.close()
            context.deleteDatabase(dbName)
        }
    }
}
