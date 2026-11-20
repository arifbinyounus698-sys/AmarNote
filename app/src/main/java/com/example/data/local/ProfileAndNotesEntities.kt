package com.example.data.local

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentSkyBlue
import com.example.ui.theme.AccentSoftPink
import com.example.ui.theme.AccentTeal

enum class ProfessionOption(val bnLabel: String, val enLabel: String) {
    TEACHER("শিক্ষক", "Teacher"),
    BUSINESSMAN("ব্যবসায়ী", "Businessperson"),
    EMPLOYEE("চাকরিজীবী", "Job Holder / Employee"),
    ENTREPRENEUR("উদ্যোক্তা", "Entrepreneur"),
    STUDENT("শিক্ষার্থী", "Student"),
    SCHOLAR("আলেম", "Islamic Scholar / Alim"),
    FARMER("কৃষক", "Farmer"),
    FREELANCER("ফ্রিল্যান্সার", "Freelancer"),
    OTHER("অন্যান্য", "Other");

    fun label(isBangla: Boolean): String = if (isBangla) bnLabel else enLabel
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "",
    val profilePhotoPath: String = "",
    val coverPhotoPath: String = "",
    val mobileNumber: String = "",
    val altMobileNumber: String = "",
    val email: String = "",
    val aboutMe: String = "",
    val profession: String = "",
    val organizationName: String = "",
    val designation: String = "",
    val workplaceAddress: String = "",
    val cityAndCountry: String = "",
    val dateOfBirth: String = "",
    val extraComment: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)

enum class NoteCategory(val bnLabel: String, val enLabel: String, val accentColor: Color) {
    IMPORTANT_NOTE("গুরুত্বপূর্ণ নোট", "Important Note", AccentEmerald),
    PERSONAL_MEMOIR("ব্যক্তিগত স্মরণিকা", "Personal Memoir", AccentPurple),
    TODO_LIST("কাজের তালিকা", "To-Do List", AccentOrange),
    FUTURE_PLAN("ভবিষ্যৎ পরিকল্পনা", "Future Plan", AccentSkyBlue),
    IMPORTANT_DATE("গুরুত্বপূর্ণ তারিখ", "Important Date", AccentSoftPink),
    EXTRA_COMMENT("অতিরিক্ত মন্তব্য", "Extra Note", AccentTeal);

    fun label(isBangla: Boolean): String = if (isBangla) bnLabel else enLabel

    companion object {
        fun fromName(name: String): NoteCategory =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: IMPORTANT_NOTE
    }
}

@Entity(tableName = "personal_notes")
data class PersonalNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val category: String = NoteCategory.IMPORTANT_NOTE.name,
    val year: Int,
    val month: Int,
    val day: Int,
    val isPinned: Boolean = false,
    val extraComment: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val categoryEnum: NoteCategory get() = NoteCategory.fromName(category)
}

enum class ReminderCategory(val bnLabel: String, val enLabel: String, val accentColor: Color) {
    SALARY_DATE("বেতন পাওয়ার তারিখ", "Salary Date", AccentEmerald),
    IMPORTANT_BILL("গুরুত্বপূর্ণ বিল", "Important Bill", AccentRose),
    PERSONAL_TASK("ব্যক্তিগত কাজ", "Personal Task", AccentIndigo),
    SPECIAL_DATE("বিশেষ তারিখ", "Special Date", AccentAmber),
    PERSONAL_MEMOIR("নিজের স্মরণিকা", "Personal Reminder", AccentPurple);

    fun label(isBangla: Boolean): String = if (isBangla) bnLabel else enLabel

    companion object {
        fun fromName(name: String): ReminderCategory =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PERSONAL_TASK
    }
}

@Entity(tableName = "app_reminders")
data class AppReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String = "",
    val category: String = ReminderCategory.PERSONAL_TASK.name,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val triggerAtMillis: Long,
    val isCompleted: Boolean = false,
    val notifySystem: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val categoryEnum: ReminderCategory get() = ReminderCategory.fromName(category)
}
