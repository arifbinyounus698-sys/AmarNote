package com.example.data.local

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BankColor
import com.example.ui.theme.BkashColor
import com.example.ui.theme.CashColor
import com.example.ui.theme.NagadColor
import com.example.ui.theme.OtherMethodColor

enum class PaymentMethod(
    val bnLabel: String,
    val enLabel: String,
    val accentColor: Color
) {
    BKASH("বিকাশ (bKash)", "bKash", BkashColor),
    NAGAD("নগদ (Nagad)", "Nagad", NagadColor),
    BANK("ব্যাংক (Bank)", "Bank", BankColor),
    CASH("ক্যাশ (Cash)", "Cash", CashColor),
    OTHER("অন্যান্য (Other)", "Other", OtherMethodColor);

    fun label(isBangla: Boolean): String = if (isBangla) bnLabel else enLabel

    companion object {
        fun fromName(name: String): PaymentMethod =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CASH
    }
}
