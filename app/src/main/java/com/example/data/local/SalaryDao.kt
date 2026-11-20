package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryDao {
    @Query(
        "SELECT * FROM salary_records " +
            "ORDER BY year DESC, month DESC, day DESC, hour DESC, minute DESC, second DESC, id DESC"
    )
    fun getAllRecords(): Flow<List<SalaryRecord>>

    @Query(
        "SELECT * FROM salary_records " +
            "ORDER BY year DESC, month DESC, day DESC, hour DESC, minute DESC, second DESC, id DESC"
    )
    suspend fun getAllRecordsOnce(): List<SalaryRecord>

    @Query("SELECT DISTINCT year FROM salary_records ORDER BY year DESC")
    fun getRecordedYears(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SalaryRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<SalaryRecord>)

    @Update
    suspend fun update(record: SalaryRecord)

    @Delete
    suspend fun delete(record: SalaryRecord)

    @Query("DELETE FROM salary_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM salary_records")
    suspend fun deleteAll()
}

@Dao
interface CalculatorDao {
    @Query("SELECT * FROM calculator_history ORDER BY timestampMillis DESC LIMIT 50")
    fun getHistory(): Flow<List<CalculatorHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CalculatorHistoryItem)

    @Query("DELETE FROM calculator_history")
    suspend fun clearAll()
}
