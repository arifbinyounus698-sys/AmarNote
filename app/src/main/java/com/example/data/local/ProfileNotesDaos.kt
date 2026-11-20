package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()
}

@Dao
interface PersonalNoteDao {
    @Query("SELECT * FROM personal_notes ORDER BY isPinned DESC, year DESC, month DESC, day DESC, updatedAtMillis DESC")
    fun getAllNotes(): Flow<List<PersonalNote>>

    @Query("SELECT * FROM personal_notes ORDER BY isPinned DESC, year DESC, month DESC, day DESC, updatedAtMillis DESC")
    suspend fun getAllNotesOnce(): List<PersonalNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: PersonalNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<PersonalNote>)

    @Update
    suspend fun update(note: PersonalNote)

    @Delete
    suspend fun delete(note: PersonalNote)

    @Query("DELETE FROM personal_notes")
    suspend fun deleteAll()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM app_reminders ORDER BY isCompleted ASC, triggerAtMillis ASC, id DESC")
    fun getAllReminders(): Flow<List<AppReminder>>

    @Query("SELECT * FROM app_reminders ORDER BY isCompleted ASC, triggerAtMillis ASC, id DESC")
    suspend fun getAllRemindersOnce(): List<AppReminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: AppReminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<AppReminder>)

    @Update
    suspend fun update(reminder: AppReminder)

    @Delete
    suspend fun delete(reminder: AppReminder)

    @Query("DELETE FROM app_reminders")
    suspend fun deleteAll()
}
