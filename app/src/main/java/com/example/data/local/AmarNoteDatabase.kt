package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SalaryRecord::class,
        CalculatorHistoryItem::class,
        UserProfile::class,
        PersonalNote::class,
        AppReminder::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AmarNoteDatabase : RoomDatabase() {
    abstract fun salaryDao(): SalaryDao
    abstract fun calculatorDao(): CalculatorDao
    abstract fun profileDao(): ProfileDao
    abstract fun personalNoteDao(): PersonalNoteDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AmarNoteDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `id` INTEGER NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `profilePhotoPath` TEXT NOT NULL,
                        `coverPhotoPath` TEXT NOT NULL,
                        `mobileNumber` TEXT NOT NULL,
                        `altMobileNumber` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `aboutMe` TEXT NOT NULL,
                        `profession` TEXT NOT NULL,
                        `organizationName` TEXT NOT NULL,
                        `designation` TEXT NOT NULL,
                        `workplaceAddress` TEXT NOT NULL,
                        `cityAndCountry` TEXT NOT NULL,
                        `dateOfBirth` TEXT NOT NULL,
                        `extraComment` TEXT NOT NULL,
                        `updatedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `personal_notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `year` INTEGER NOT NULL,
                        `month` INTEGER NOT NULL,
                        `day` INTEGER NOT NULL,
                        `isPinned` INTEGER NOT NULL,
                        `extraComment` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        `updatedAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `year` INTEGER NOT NULL,
                        `month` INTEGER NOT NULL,
                        `day` INTEGER NOT NULL,
                        `hour` INTEGER NOT NULL,
                        `minute` INTEGER NOT NULL,
                        `triggerAtMillis` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `notifySystem` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AmarNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AmarNoteDatabase::class.java,
                    "amarnote_offline_db"
                )
                    // Non-destructive migration guarantees existing 10-year salary records are preserved.
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
