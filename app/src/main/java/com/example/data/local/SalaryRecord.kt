package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.GregorianCalendar

@Entity(tableName = "salary_records")
data class SalaryRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: Double,
    val year: Int,
    val month: Int, // 1..12
    val day: Int,   // 1..31
    val hour: Int,  // 0..23
    val minute: Int, // 0..59
    val second: Int, // 0..59
    val dayOfWeek: Int, // Calendar.SUNDAY (1) .. Calendar.SATURDAY (7)
    val paymentMethod: String, // PaymentMethod.name
    val receiverNumber: String = "",
    val senderNumber: String = "",
    val bankName: String = "",
    val accountLastDigits: String = "",
    val transactionId: String = "",
    val comment: String = "",
    val extraNote: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val methodEnum: PaymentMethod
        get() = PaymentMethod.fromName(paymentMethod)

    companion object {
        fun computeDayOfWeek(year: Int, month: Int, day: Int): Int {
            val cal = GregorianCalendar(year, (month - 1).coerceIn(0, 11), day.coerceIn(1, 31))
            return cal.get(Calendar.DAY_OF_WEEK)
        }
    }
}

@Entity(tableName = "calculator_history")
data class CalculatorHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val expression: String,
    val result: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
